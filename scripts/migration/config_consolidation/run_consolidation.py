#!/usr/bin/env python3
"""
Pipeline 5 — Config Consolidation Engine.

Reads each migrated service's `application.properties`, classifies every
key, and emits a consolidated YAML overlay structure for the monolith:

  Output (under dristi-monolith/dristi-app/src/main/resources/):
    application-shared.yml         — keys with a single value across all services
    application-<subdomain>.yml    — service-specific keys + this service's
                                     value for any conflicting keys

  Audit (under scripts/migration/config_consolidation/output/):
    config_consolidation_report.csv  — every key, value, classification
    config_conflicts.txt             — human-readable conflict report

Activation: add the per-service profile names to spring.profiles.active in
application.yml (e.g. `spring.profiles.active=local,shared,cases,locksvc`).
Profile order resolves remaining conflicts (later wins).

Service → subdomain mapping is derived from migration manifest files when
they exist; falls back to the service name (lowercased, no hyphens).

Usage:
  python3 run_consolidation.py --service case --service lock-svc \\
      [--service-subdomain case=cases] [--service-subdomain lock-svc=locksvc]
"""

from __future__ import annotations

import argparse
import csv
import json
import re
from collections import defaultdict
from pathlib import Path
from typing import Any

import yaml

REPO_ROOT = Path(__file__).resolve().parents[3]
MONOLITH_ROOT = REPO_ROOT / "dristi-monolith"
APP_RESOURCES = MONOLITH_ROOT / "dristi-app" / "src" / "main" / "resources"
OUT_DIR = Path(__file__).resolve().parent / "output"
MANIFEST_DIR = REPO_ROOT / "scripts" / "migration" / "per_module" / "output"


def domain_module_resources(target_module: str) -> Path:
    """Resources directory of the domain module a service migrates into.
    Per-service profile YML lives here so case-related config travels with
    the case-lifecycle module's code rather than at the bootstrap level."""
    return MONOLITH_ROOT / f"domain-{target_module}" / "src" / "main" / "resources"


# Keys whose values the monolith decides — drop service-specific values
# entirely (they're managed by the consolidated application.yml).
DROP_KEYS = {
    "server.port",
    "server.contextPath",
    "server.servlet.context-path",
    "spring.application.name",
    "spring.profiles.active",
    "spring.main.allow-bean-definition-overriding",
    # OpenTelemetry — single name for the whole monolith
    "otel.service.name",
    # Loki — single app name for the monolith
    "logging.loki.app",
    "logging.loki.environment",
    # Single management surface
    "management.endpoints.web.base-path",
}

# Keys where the SHAPE matters but the VALUE is owned by the monolith.
# These are dropped when seen in service properties; the monolith provides
# its own value (e.g. one DB connection for all services).
DROP_INFRA_KEYS = {
    "spring.datasource.driver-class-name",
    "spring.datasource.url",
    "spring.datasource.username",
    "spring.datasource.password",
    "spring.flyway.url",
    "spring.flyway.user",
    "spring.flyway.password",
    "spring.flyway.table",
    "spring.flyway.baseline-on-migrate",
    "spring.flyway.outOfOrder",
    "spring.flyway.out-of-order",
    "spring.flyway.enabled",
    "spring.flyway.locations",
    "spring.kafka.bootstrap-servers",
    "kafka.config.bootstrap_server_config",
    "spring.kafka.consumer.value-deserializer",
    "spring.kafka.consumer.key-deserializer",
    "spring.kafka.consumer.group-id",
    "spring.kafka.producer.key-serializer",
    "spring.kafka.producer.value-serializer",
    "spring.kafka.listener.missing-topics-fatal",
    "spring.kafka.consumer.properties.spring.json.use.type.headers",
    "spring.jpa.show-sql",
    "spring.jpa.open-in-view",
    "spring.jpa.hibernate.ddl-auto",
    "spring.jpa.hibernate.naming.physical-strategy",
    "spring.jpa.properties.hibernate.dialect",
    "spring.jpa.properties.hibernate.format_sql",
    "spring.jpa.properties.hibernate.generate_statistics",
    "spring.jpa.properties.hibernate.order_inserts",
}

ALL_DROPPED = DROP_KEYS | DROP_INFRA_KEYS

# Per-service dead keys: properties present in a service's upstream
# application.properties but with no Java reader remaining in the
# migrated subdomain because intra-DRISTI REST calls were rewritten as
# direct *Api calls (PIPELINE_RULES.md rule 32 + rule 37). Without this
# map, Pipeline 5 mechanically carries the dead config into the
# subdomain overlay; the next migration's regen then re-introduces
# whatever the prior REST→direct cleanup commit deleted by hand.
#
# Conventions:
# - Outer key = source service name passed via --service (matches the
#   directory under dristi-services/ or integration-services/, not the
#   subdomain).
# - Each entry is the set of property keys to drop FOR THIS SERVICE ONLY.
#   Other services that legitimately read the same key are unaffected.
# - Grow the map when a service's REST→direct conversion lands; cite
#   the cleanup commit that originally removed the keys so future
#   readers can verify the keys are still unread.
SERVICE_DEAD_KEYS: dict[str, set[str]] = {
    # d18c5b552 refactor(ab-diary): drop dead CaseUtil ...
    # ab-diary now reads CaseApi directly; no @Value("${dristi.case...")
    # consumers remain in caselifecycle/abdiary/internal/.
    "ab-diary": {
        "dristi.case.host",
        "dristi.case.search.path",
    },
    # a60c86086 refactor(payment-calculator): REST→direct via CaseApi ...
    # payment-calculator uses CaseApi directly; no @Value("${egov.case...")
    # consumers remain in payments/calculator/internal/.
    "payment-calculator-svc": {
        "egov.case.host",
        "egov.case.path",
        "egov.case.search.path",
    },
    # e1d23287d chore(advocate): drop dead REST config ...
    # cases + order switched to AdvocateApi (Rule 32) during the advocate
    # migration; no @Value("${egov.advocate...") consumers remain in
    # caselifecycle/cases or caselifecycle/order internal/. Pipeline 5
    # would otherwise re-add these dead keys on every subsequent regen.
    "case": {
        "egov.advocate.host",
        "egov.advocate.path",
        "egov.advocate.clerk.path",
        # 8e1e2c2c3 (advocate-office-management) deleted cases/AdvocateOfficeUtil
        # — the REST helper that read these. No @Value consumer remains in
        # caselifecycle/cases/internal/ for `dristi.advocate.office.*`.
        "dristi.advocate.office.host",
        "dristi.advocate.office.search.member.endpoint",
        # treasury-backend C2: cases EtreasuryUtil deleted; cases now uses
        # TreasuryApi direct calls. No @Value("${egov.etreasury...") /
        # ${etreasury.*}") consumer remains in caselifecycle/cases/internal/.
        "egov.etreasury.host",
        "egov.etreasury.demand.create.endpoint",
        "etreasury.payment.receipt.endpoint",
        "etreasury.head.breakup.calculation.endpoint",
        # 927ebb6c5 fix(hearing): Rule 31/32/36/37/38/40 compliance — PR #57
        # deleted cases/HearingUtil; cases now reads HearingApi directly.
        # No @Value("${egov.hearing.{host,path,search.path}}") consumer
        # remains in caselifecycle/cases/internal/. (`egov.hearing.path`
        # was the `update_transcript_additional_attendees` endpoint;
        # `egov.hearing.search.path` was the search endpoint — both
        # subsumed by HearingApi.search.)
        "egov.hearing.host",
        "egov.hearing.path",
        "egov.hearing.search.path",
        # ea4cbf89e refactor(evidence): cases/EvidenceValidator REST→EvidenceApi
        # direct calls (Rule 32). No @Value("${egov.evidence.search.path}")
        # consumer remains in caselifecycle/cases/internal/. `egov.evidence.{host,
        # create.path}` stay live — cases/EvidenceUtil.createEvidence is the
        # cross-subdomain write shim deferred per Rule 35.
        "egov.evidence.search.path",
    },
    "order": {
        "egov.advocate.host",
        "egov.advocate.path",
        # bcf520653 refactor(ctc,order): REST→HearingApi direct calls — this
        # PR (fix/hearing-pipeline-compliance) deleted order/HearingUtil and
        # rewired OrderRegistrationService + SmsNotificationService to
        # HearingApi (Rule 32). No @Value("${egov.hearing...") consumer
        # remains in caselifecycle/order/internal/.
        "egov.hearing.host",
        "egov.hearing.search.path",
    },
    # bcf520653 refactor(ctc,order): REST→HearingApi direct calls — this PR
    # dropped @Value("${egov.hearing.host}") + @Value("${egov.hearing.search.path}")
    # from caselifecycle/ctc/internal/config/Configuration.java. They were
    # already dead pre-cutover (no code in caselifecycle/ctc/ ever read them);
    # adding to SERVICE_DEAD_KEYS so Pipeline 5 doesn't re-add them on regen.
    "ctc": {
        "egov.hearing.host",
        "egov.hearing.search.path",
    },
    # bbc2f3bf2 refactor(hearing): CaseUtil REST→CaseApi direct calls — this
    # PR rewired hearing's CaseUtil to consume CaseApi directly (the symmetric
    # cutover that PR #57 left dangling: cases→hearing was converted by
    # 927ebb6c5, but hearing→cases stayed on REST). The three @Value
    # bindings for `egov.case.{host,path,search.path}` were dropped from
    # hearing/Configuration.java; SERVICE_DEAD_KEYS guards them on regen.
    "hearing": {
        "egov.case.host",
        "egov.case.path",
        "egov.case.search.path",
    },
    # 8e1e2c2c3 refactor(advocate-office-management): uplift AdvocateUtil
    # REST→AdvocateApi direct calls. advocateoffice now reads AdvocateApi
    # directly; no @Value("${dristi.advocate...") consumers remain in
    # identityaccess/advocateoffice/internal/. Pipeline 5 would otherwise
    # re-add these dead keys on every subsequent regen.
    "advocate-office-management": {
        "dristi.advocate.host",
        "dristi.advocate.search.endpoint",
        "dristi.advocate.clerk.search.endpoint",
    },
    # casemanagement C2: full Rule 32 sweep — every outgoing REST helper
    # whose target subdomain is in the monolith was converted to *Api
    # direct calls. casemanagement now reads CaseApi (CaseBundleService +
    # CaseBundleIndexBuilderService convert the typed CaseListResponse
    # back to Map<String,Object> for legacy downstream code),
    # TaskmanagementApi, EvidenceApi, OrderApi (OrderSearchService maps
    # the VC-entity referenceId onto OrderCriteria.id), ApplicationApi
    # (exposed in this PR — first cross-subdomain caller), TaskApi
    # (re-introduced; safe because casemanagement has no incoming edges
    # so the cases↔task↔order cycle from a89087936 cannot re-form),
    # CtcApi (read-side only; updates stay on REST per Rule 35), and
    # DigitalizeddocumentsApi (exposed in this PR). The only outgoing
    # REST left is CtcUtil.updateCtcApplication (Rule 35) plus
    # MdmsV2Util and SummonsOrderPdfUtil (egov platform services per
    # Rule 17). Pipeline 5 would otherwise re-add the dead keys on
    # every regen.
    "casemanagement": {
        "dristi.taskmanagement.host",
        "dristi.taskmanagement.search.endpoint",
        "dristi.evidence.host",
        "dristi.evidence.search.endpoint",
        "dristi.application.host",
        "dristi.application.search.endpoint",
        "dristi.case.host",
        "dristi.case.search.url",
        "dristi.order.host",
        "dristi.order.search.url",
        "dristi.task.host",
        "dristi.task.search.url",
        "dristi.digitalized.documents.host",
        "dristi.digitalized.documents.search.endpoint",
        "dristi.ctc.search.endpoint",
    },
}

# Two-source-into-one-subdomain (e-sign-svc + esign-interceptor): the
# absorbed service is filtered via ABSORBED_SERVICES below; see its
# docstring for rationale.
ABSORBED_SERVICES = {
    "esign-interceptor": (
        "absorbed into e-sign-svc's `esign` subdomain via hand-port at "
        "commit 87b8aafba; configuration is hand-curated in "
        "domain-integration/src/main/resources/application-esign.yml "
        "(see the `# HAND-CURATED` block there for which interceptor "
        "keys are kept vs. dropped post-Rule-32)."
    ),
}

# Naming: subdomain prefix for the per-service yml file (matches the
# Spring profile name).
DEFAULT_SUBDOMAIN_OVERRIDES = {
    "lock-svc": "locksvc",
    "advocate-office-management": "advocateoffice",
    "hearing-management": "hearingmanagement",
    "order-management": "ordermanagement",
    "task-management": "taskmanagement",
    "scheduler-svc": "scheduler",
    "payment-calculator-svc": "calculator",
    "summons-svc": "summons",
    "treasury-backend": "treasury",
    "njdg-transformer": "njdg",
    "icops_integration-kerala": "icops",
    "e-sign-svc": "esign",
    "esign-interceptor": "esign",
    "epost-tracker": "epost",
    "bank-details": "bank",
    "digitalized-documents": "digitalizeddocuments",
    "bail-bond": "bailbond",
}


# --- helpers -----------------------------------------------------------------


def find_service_props(service: str) -> Path | None:
    for parent in ("dristi-services", "integration-services"):
        candidate = REPO_ROOT / parent / service / "src" / "main" / "resources" / "application.properties"
        if candidate.exists():
            return candidate
    return None


def parse_properties(path: Path) -> dict[str, str]:
    """Naive .properties parser. Handles comments and `key = value` lines."""
    result: dict[str, str] = {}
    text = path.read_text(encoding="utf-8")
    for raw_line in text.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or line.startswith("!"):
            continue
        if "=" not in line:
            continue
        key, _, value = line.partition("=")
        key = key.strip()
        value = value.strip()
        result[key] = value
    return result


def read_manifest(service: str) -> dict[str, str]:
    manifest = MANIFEST_DIR / f"{service}_manifest.json"
    if manifest.exists():
        try:
            return json.loads(manifest.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError):
            return {}
    return {}


def derive_subdomain(service: str, manifest: dict[str, str], override: str | None) -> str:
    if override:
        return override
    sub = manifest.get("target_subdomain")
    if sub:
        return sub
    if service in DEFAULT_SUBDOMAIN_OVERRIDES:
        return DEFAULT_SUBDOMAIN_OVERRIDES[service]
    return re.sub(r"[^a-z0-9]", "", service.lower())


def derive_target_module(manifest: dict[str, str]) -> str | None:
    return manifest.get("target_module")


def flat_to_nested(flat: dict[str, str]) -> dict[str, Any]:
    """Convert dot-keyed flat properties into a nested dict for YAML output.
    Spring also accepts dot-keys at YAML leaves, so we fall back to the flat
    form whenever an intermediate node is already a leaf string."""
    root: dict[str, Any] = {}
    leaks: list[tuple[str, str]] = []
    for key, value in flat.items():
        parts = key.split(".")
        cur: Any = root
        ok = True
        for i, segment in enumerate(parts[:-1]):
            existing = cur.get(segment)
            if existing is None:
                cur[segment] = {}
                cur = cur[segment]
            elif isinstance(existing, dict):
                cur = existing
            else:
                # Conflict: would-be intermediate is already a string. Fall
                # back to a flat key at the current level to preserve both
                # values without erroring.
                leaks.append((key, value))
                ok = False
                break
        if ok:
            cur[parts[-1]] = _coerce_yaml_value(value)
    # Reapply leaked flats as their full dot-keys at the top level (Spring
    # parses `a.b.c: x` literally).
    for key, value in leaks:
        root[key] = _coerce_yaml_value(value)
    return root


_COERCE_TRUE = {"true", "True", "TRUE"}
_COERCE_FALSE = {"false", "False", "FALSE"}


def _coerce_yaml_value(raw: str) -> Any:
    """Preserve booleans / ints; everything else stays a string."""
    if raw in _COERCE_TRUE:
        return True
    if raw in _COERCE_FALSE:
        return False
    if re.fullmatch(r"-?\d+", raw):
        try:
            return int(raw)
        except ValueError:
            return raw
    return raw


def write_yaml(path: Path, data: dict[str, Any], header: list[str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as fh:
        for line in header:
            fh.write(f"# {line}\n")
        fh.write("\n")
        if data:
            yaml.safe_dump(data, fh, sort_keys=False, default_flow_style=False)
        else:
            fh.write("# (no service-specific keys)\n")


# --- main --------------------------------------------------------------------


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--service", action="append", default=[],
                        help="Source service name; pass multiple --service flags")
    parser.add_argument("--service-subdomain", action="append", default=[],
                        help="Override of the service→subdomain mapping (foo=bar)")
    args = parser.parse_args()

    for svc in args.service:
        if svc in ABSORBED_SERVICES:
            print(f"SKIP: {svc} — {ABSORBED_SERVICES[svc]}")
    args.service = [s for s in args.service if s not in ABSORBED_SERVICES]

    if not args.service:
        print("ERROR: at least one --service is required")
        return 1

    overrides: dict[str, str] = {}
    for entry in args.service_subdomain:
        if "=" not in entry:
            continue
        k, _, v = entry.partition("=")
        overrides[k.strip()] = v.strip()

    # service -> {subdomain, target_module}
    manifest_by_service: dict[str, dict[str, str]] = {}
    subdomain_of: dict[str, str] = {}
    target_module_of: dict[str, str | None] = {}
    for svc in args.service:
        manifest = read_manifest(svc)
        manifest_by_service[svc] = manifest
        subdomain_of[svc] = derive_subdomain(svc, manifest, overrides.get(svc))
        target_module_of[svc] = derive_target_module(manifest)
        if not target_module_of[svc]:
            print(f"WARN: no target_module in manifest for {svc}; per-service yml "
                  f"will go to dristi-app instead of a domain submodule")

    # service -> {key: value}
    by_service: dict[str, dict[str, str]] = {}
    for svc in args.service:
        props_path = find_service_props(svc)
        if props_path is None:
            print(f"WARN: no application.properties found for service '{svc}' — skipping")
            continue
        by_service[svc] = parse_properties(props_path)

    # all keys → {service: value}
    key_values: dict[str, dict[str, str]] = defaultdict(dict)
    for svc, props in by_service.items():
        dead_for_svc = SERVICE_DEAD_KEYS.get(svc, set())
        for key, value in props.items():
            if key in ALL_DROPPED:
                continue
            if key in dead_for_svc:
                # No reader in migrated subdomain (REST→direct cleanup).
                continue
            key_values[key][svc] = value

    # Classify
    shared: dict[str, str] = {}
    per_service: dict[str, dict[str, str]] = {svc: {} for svc in by_service}
    conflicts: list[dict[str, str]] = []
    report_rows: list[dict[str, str]] = []

    for key, values_by_svc in key_values.items():
        unique_values = set(values_by_svc.values())
        if len(values_by_svc) == len(by_service) and len(unique_values) == 1:
            shared[key] = next(iter(unique_values))
            for svc, val in values_by_svc.items():
                report_rows.append({"key": key, "service": svc, "value": val, "classification": "SHARED"})
        elif len(values_by_svc) == 1:
            (svc, val), = values_by_svc.items()
            per_service[svc][key] = val
            report_rows.append({"key": key, "service": svc, "value": val, "classification": "SERVICE-SPECIFIC"})
        else:
            for svc, val in values_by_svc.items():
                per_service[svc][key] = val
                report_rows.append({"key": key, "service": svc, "value": val, "classification": "CONFLICT"})
            conflicts.append({
                "key": key,
                "values": " | ".join(f"{s}={v}" for s, v in sorted(values_by_svc.items())),
            })

    # Stabilise per_service[svc] iteration order against the source service's
    # own properties order. Without this, a CONFLICT key's position in the
    # generated YAML depends on which service was first in the --service list
    # (key_values iteration order is first-declarer-wins) — adding a new
    # service to the consolidation reshuffles unrelated services' YAMLs.
    for svc, picked in list(per_service.items()):
        if not picked:
            continue
        per_service[svc] = {
            key: picked[key] for key in by_service[svc] if key in picked
        }

    # Emit
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    write_yaml(
        APP_RESOURCES / "application-shared.yml",
        flat_to_nested(shared),
        [
            "AUTO-GENERATED by scripts/migration/config_consolidation/run_consolidation.py",
            "Properties with the same value across every consolidated service.",
            f"Sourced from: {', '.join(sorted(by_service))}.",
            "Activate via spring.profiles.active=...,shared,...",
        ],
    )

    # Per-service profile YML lives in the OWNING domain module's resources
    # so each module ships its own config. Spring Boot loads
    # `application-<profile>.yml` from anywhere on the classpath.
    per_service_locations: dict[str, Path] = {}
    for svc, props in per_service.items():
        sub = subdomain_of[svc]
        target_module = target_module_of.get(svc)
        if target_module:
            base = domain_module_resources(target_module)
            location_note = f"domain-{target_module}/src/main/resources/"
        else:
            base = APP_RESOURCES
            location_note = "dristi-app/src/main/resources/ (no target_module)"
        path = base / f"application-{sub}.yml"
        per_service_locations[svc] = path
        write_yaml(
            path,
            flat_to_nested(props),
            [
                "AUTO-GENERATED by scripts/migration/config_consolidation/run_consolidation.py",
                f"Per-subdomain overlay for {svc} (subdomain={sub}).",
                f"Lives in {location_note} so its config travels with the",
                f"domain-{target_module or '<none>'} module.",
                "Includes service-unique keys + this service's value for any "
                "key that conflicts across services.",
                f"Activate via spring.profiles.active=...,{sub},...",
            ],
        )

    with (OUT_DIR / "config_consolidation_report.csv").open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=["key", "service", "value", "classification"])
        writer.writeheader()
        writer.writerows(sorted(report_rows, key=lambda r: (r["classification"], r["key"], r["service"])))

    with (OUT_DIR / "config_conflicts.txt").open("w", encoding="utf-8") as fh:
        if not conflicts:
            fh.write("# No conflicts.\n")
        else:
            fh.write(f"# {len(conflicts)} keys with different values across services\n")
            fh.write("# Per-service profile YAML resolves them via Spring's profile-overlay rules.\n\n")
            for c in sorted(conflicts, key=lambda c: c["key"]):
                fh.write(f"{c['key']}\n  {c['values']}\n\n")

    # Print summary
    total_keys = len(key_values)
    print(f"Consolidated {total_keys} keys across {len(by_service)} services:")
    print(f"  SHARED:           {len(shared)}")
    print(f"  CONFLICT:         {len(conflicts)}")
    print(f"  SERVICE-SPECIFIC: {sum(len(v) for v in per_service.values()) - sum(1 for k in shared) - len(conflicts) * len(by_service)}")
    print()
    print("Wrote:")
    print(f"  {(APP_RESOURCES / 'application-shared.yml').relative_to(REPO_ROOT)}")
    for svc, path in per_service_locations.items():
        sub = subdomain_of[svc]
        print(f"  {path.relative_to(REPO_ROOT)} ({svc} -> profile {sub})")
    print(f"  {(OUT_DIR / 'config_consolidation_report.csv').relative_to(REPO_ROOT)}")
    if conflicts:
        print(f"  {(OUT_DIR / 'config_conflicts.txt').relative_to(REPO_ROOT)} ({len(conflicts)} conflicts)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
