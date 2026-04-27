#!/usr/bin/env python3
"""
Pipeline 1 / Phase 1 — Generate Parent POM with dependency union from all services.

Scans all 37 service POMs under dristi-services/ and integration-services/,
collects unique dependencies (highest version wins on conflict), and emits
dristi-monolith/pom.xml.

De-scoped services are excluded per implementation_plan_apr_27.md:
  - ocr-service
  - sunbirdrc-credential-service
  - artifacts (no POM anyway)
"""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from collections import defaultdict
from pathlib import Path

NS = "http://maven.apache.org/POM/4.0.0"
ET.register_namespace("", NS)

REPO_ROOT = Path(__file__).resolve().parents[3]
TARGET_POM = REPO_ROOT / "dristi-monolith" / "pom.xml"

DESCOPED = {"ocr-service", "sunbirdrc-credential-service", "artifacts"}

MODULES = [
    "dristi-common",
    "domain-case-lifecycle",
    "domain-identity-access",
    "domain-integration",
    "domain-payments",
    "dristi-app",
]

SPRING_BOOT_VERSION = "3.2.2"
JAVA_VERSION = "17"
JACOCO_VERSION = "0.8.9"

# (groupId, artifactId) pairs that are hardcoded in the parent POM template;
# de-duped from the union to avoid double declarations.
HARDCODED_DM = {
    ("io.opentelemetry", "opentelemetry-bom"),
    ("io.opentelemetry.instrumentation", "opentelemetry-instrumentation-bom-alpha"),
}


def q(tag: str) -> str:
    return f"{{{NS}}}{tag}"


def text_of(elem: ET.Element | None, tag: str) -> str | None:
    if elem is None:
        return None
    child = elem.find(q(tag))
    return child.text.strip() if child is not None and child.text else None


def find_service_poms(repo_root: Path) -> list[Path]:
    poms: list[Path] = []
    for parent in ("dristi-services", "integration-services"):
        for pom in (repo_root / parent).glob("*/pom.xml"):
            if pom.parent.name in DESCOPED:
                continue
            poms.append(pom)
    poms.sort()
    return poms


def parse_dependencies(pom_path: Path) -> list[tuple[str, str, str | None, str | None, str | None]]:
    """Return list of (groupId, artifactId, version, scope, optional)."""
    try:
        tree = ET.parse(pom_path)
    except ET.ParseError as exc:
        print(f"WARN: failed to parse {pom_path}: {exc}", file=sys.stderr)
        return []
    root = tree.getroot()
    deps: list[tuple[str, str, str | None, str | None, str | None]] = []
    # Only direct child <dependencies>, not those inside <dependencyManagement>
    for deps_block in root.findall(q("dependencies")):
        for dep in deps_block.findall(q("dependency")):
            group = text_of(dep, "groupId")
            artifact = text_of(dep, "artifactId")
            if not group or not artifact:
                continue
            version = text_of(dep, "version")
            scope = text_of(dep, "scope")
            optional = text_of(dep, "optional")
            deps.append((group, artifact, version, scope, optional))
    return deps


def parse_dep_management(pom_path: Path) -> list[tuple[str, str, str | None, str | None, str | None]]:
    try:
        tree = ET.parse(pom_path)
    except ET.ParseError:
        return []
    root = tree.getroot()
    items: list[tuple[str, str, str | None, str | None, str | None]] = []
    dm = root.find(q("dependencyManagement"))
    if dm is None:
        return items
    deps_block = dm.find(q("dependencies"))
    if deps_block is None:
        return items
    for dep in deps_block.findall(q("dependency")):
        group = text_of(dep, "groupId")
        artifact = text_of(dep, "artifactId")
        if not group or not artifact:
            continue
        version = text_of(dep, "version")
        scope = text_of(dep, "scope")
        dtype = text_of(dep, "type")
        items.append((group, artifact, version, scope, dtype))
    return items


def version_key(v: str | None) -> tuple:
    if not v:
        return ()
    parts: list[tuple[int, str]] = []
    for chunk in v.replace("-", ".").split("."):
        try:
            parts.append((0, f"{int(chunk):010d}"))
        except ValueError:
            parts.append((1, chunk))
    return tuple(parts)


def merge_dependencies(
    poms: list[Path],
) -> tuple[dict[tuple[str, str], dict], dict[tuple[str, str], dict]]:
    """Return (deps_union, dep_management_union).
    Each value is a dict with merged version/scope/optional and a 'sources' list.
    """
    deps_union: dict[tuple[str, str], dict] = defaultdict(
        lambda: {"version": None, "scope": None, "optional": None, "sources": set()}
    )
    dm_union: dict[tuple[str, str], dict] = defaultdict(
        lambda: {"version": None, "scope": None, "type": None, "sources": set()}
    )
    for pom in poms:
        svc = pom.parent.name
        for group, artifact, version, scope, optional in parse_dependencies(pom):
            entry = deps_union[(group, artifact)]
            entry["sources"].add(svc)
            if version and version_key(version) > version_key(entry["version"]):
                entry["version"] = version
            if scope and not entry["scope"]:
                entry["scope"] = scope
            if optional and not entry["optional"]:
                entry["optional"] = optional
        for group, artifact, version, scope, dtype in parse_dep_management(pom):
            entry = dm_union[(group, artifact)]
            entry["sources"].add(svc)
            if version and version_key(version) > version_key(entry["version"]):
                entry["version"] = version
            if scope and not entry["scope"]:
                entry["scope"] = scope
            if dtype and not entry["type"]:
                entry["type"] = dtype
    return deps_union, dm_union


# --- POM emission --------------------------------------------------------------

INDENT = "    "


def fmt_dep_management(dm_union: dict[tuple[str, str], dict]) -> str:
    lines: list[str] = []
    for (group, artifact), info in sorted(dm_union.items()):
        if (group, artifact) in HARDCODED_DM:
            continue
        lines.append(f"{INDENT*3}<dependency>")
        lines.append(f"{INDENT*4}<groupId>{group}</groupId>")
        lines.append(f"{INDENT*4}<artifactId>{artifact}</artifactId>")
        if info["version"]:
            lines.append(f"{INDENT*4}<version>{info['version']}</version>")
        if info["type"]:
            lines.append(f"{INDENT*4}<type>{info['type']}</type>")
        if info["scope"]:
            lines.append(f"{INDENT*4}<scope>{info['scope']}</scope>")
        lines.append(f"{INDENT*3}</dependency>")
    return "\n".join(lines)


def fmt_dependencies(deps_union: dict[tuple[str, str], dict]) -> str:
    """Emit unique direct dependencies that carry explicit versions.

    Versionless entries are skipped — they inherit version management from
    spring-boot-starter-parent or other BOMs declared above. Adding them here
    without a version would clobber that inheritance.
    """
    lines: list[str] = []
    for (group, artifact), info in sorted(deps_union.items()):
        if (group, artifact) in HARDCODED_DM:
            continue
        if not info["version"]:
            continue  # let upstream BOM manage the version
        lines.append(f"{INDENT*3}<dependency>")
        lines.append(f"{INDENT*4}<groupId>{group}</groupId>")
        lines.append(f"{INDENT*4}<artifactId>{artifact}</artifactId>")
        lines.append(f"{INDENT*4}<version>{info['version']}</version>")
        if info["scope"]:
            lines.append(f"{INDENT*4}<scope>{info['scope']}</scope>")
        if info["optional"]:
            lines.append(f"{INDENT*4}<optional>{info['optional']}</optional>")
        lines.append(f"{INDENT*3}</dependency>")
    return "\n".join(lines)


def fmt_modules() -> str:
    return "\n".join(f"{INDENT*2}<module>{m}</module>" for m in MODULES)


def emit_parent_pom(deps_union, dm_union) -> str:
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<!--
  AUTO-GENERATED by scripts/migration/scaffold/01_generate_parent_pom.py
  DO NOT EDIT BY HAND. Re-run the script to regenerate.
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>{SPRING_BOOT_VERSION}</version>
        <relativePath/>
    </parent>

    <groupId>org.pucar.dristi</groupId>
    <artifactId>dristi-monolith</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <name>dristi-monolith</name>
    <description>DRISTI modular monolith — parent project.</description>

    <properties>
        <java.version>{JAVA_VERSION}</java.version>
        <maven.compiler.source>${{java.version}}</maven.compiler.source>
        <maven.compiler.target>${{java.version}}</maven.compiler.target>
        <jacoco.version>{JACOCO_VERSION}</jacoco.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <modules>
{fmt_modules()}
    </modules>

    <dependencyManagement>
        <dependencies>
            <!-- BOM imports observed across services -->
            <dependency>
                <groupId>io.opentelemetry</groupId>
                <artifactId>opentelemetry-bom</artifactId>
                <version>1.35.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>io.opentelemetry.instrumentation</groupId>
                <artifactId>opentelemetry-instrumentation-bom-alpha</artifactId>
                <version>2.1.0-alpha</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Service-declared dependencyManagement entries (union) -->
{fmt_dep_management(dm_union)}

            <!-- Direct dependencies observed across services (union) -->
{fmt_dependencies(deps_union)}
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                </plugin>
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <version>${{jacoco.version}}</version>
                    <executions>
                        <execution>
                            <id>jacoco-initialize</id>
                            <goals><goal>prepare-agent</goal></goals>
                        </execution>
                        <execution>
                            <id>jacoco-report</id>
                            <phase>verify</phase>
                            <goals><goal>report</goal></goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <repositories>
        <repository>
            <id>repo.egovernments.org</id>
            <name>eGov ERP Releases Repository</name>
            <url>https://nexus-repo.egovernments.org/nexus/content/repositories/releases/</url>
        </repository>
        <repository>
            <id>repo.egovernments.org.snapshots</id>
            <name>eGov ERP Snapshots Repository</name>
            <url>https://nexus-repo.egovernments.org/nexus/content/repositories/snapshots/</url>
        </repository>
        <repository>
            <id>repo.egovernments.org.public</id>
            <name>eGov Public Repository Group</name>
            <url>https://nexus-repo.egovernments.org/nexus/content/groups/public/</url>
        </repository>
        <repository>
            <id>repo.digit.org</id>
            <name>eGov DIGIT Snapshots Repository</name>
            <url>https://nexus-repo.digit.org/nexus/content/repositories/snapshots/</url>
        </repository>
    </repositories>
</project>
"""


def main() -> int:
    poms = find_service_poms(REPO_ROOT)
    if not poms:
        print("ERROR: no service POMs found", file=sys.stderr)
        return 1
    deps_union, dm_union = merge_dependencies(poms)

    TARGET_POM.parent.mkdir(parents=True, exist_ok=True)
    TARGET_POM.write_text(emit_parent_pom(deps_union, dm_union), encoding="utf-8")

    summary_lines = [
        f"Scanned {len(poms)} service POMs",
        f"Unique direct dependencies: {len(deps_union)}",
        f"Unique dependencyManagement entries: {len(dm_union)}",
        f"Wrote: {TARGET_POM.relative_to(REPO_ROOT)}",
    ]
    print("\n".join(summary_lines))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
