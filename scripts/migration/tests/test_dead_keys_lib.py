#!/usr/bin/env python3
"""Regression tests for `dead_keys_lib`.

Run with: `python3 scripts/migration/tests/test_dead_keys_lib.py`
(no pytest dependency — the file is self-contained).
"""

from __future__ import annotations

import sys
from pathlib import Path

# Allow `from dead_keys_lib import ...` against the sibling module.
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from dead_keys_lib import (  # noqa: E402
    parse_all_key_references,
    parse_value_keys,
)


# ── @Value (the original case Rule 42 targets) ───────────────────────


def test_value_single_line():
    assert parse_value_keys('@Value("${egov.foo.host}") String x;') == {"egov.foo.host"}


def test_value_with_default():
    assert parse_value_keys('@Value("${egov.foo.host:default}") String x;') == {
        "egov.foo.host"
    }


def test_value_with_null_default():
    assert parse_value_keys('@Value("${egov.foo.host:#{null}}") String x;') == {
        "egov.foo.host"
    }


def test_value_multiple_in_one_file():
    text = """
    @Value("${egov.advocate.host}")
    private String advocateHost;

    @Value("${egov.case.search.path}")
    private String casePath;
    """
    assert parse_value_keys(text) == {"egov.advocate.host", "egov.case.search.path"}


# ── @KafkaListener (PR #111 regression on PR #101) ───────────────────


def test_kafkalistener_topic_keeps_key_alive():
    """The casemanagement bug: `mdms.kafka.save.topic` was bound via @Value
    AND consumed by a @KafkaListener. Stripping the @Value alone made the
    detector think the key was dead; the consolidation regen then yanked
    it out of the yml, breaking the listener at startup."""
    cur_text = """
    @KafkaListener(topics = "${mdms.kafka.save.topic}", containerFactory = "f")
    public void onSave(ConsumerRecord<String, Object> r) {}
    """
    # The key is alive via the listener even though no @Value binds it.
    assert "mdms.kafka.save.topic" in parse_all_key_references(cur_text)
    # And it's NOT in the @Value-only view (which is correct).
    assert parse_value_keys(cur_text) == set()


def test_kafkalistener_multi_topic_array():
    """`topics = {"${X}", "${Y}"}` form — both keys must be detected."""
    cur_text = """
    @KafkaListener(
        topics = {"${kafka.foo}", "${kafka.bar}"},
        groupId = "g"
    )
    public void onAny(Object r) {}
    """
    keys = parse_all_key_references(cur_text)
    assert "kafka.foo" in keys, keys
    assert "kafka.bar" in keys, keys


def test_kafkalistener_topicpattern():
    cur_text = '@KafkaListener(topicPattern = "${kafka.foo.pattern}") void on(Object r) {}'
    assert "kafka.foo.pattern" in parse_all_key_references(cur_text)


# ── @Scheduled ───────────────────────────────────────────────────────


def test_scheduled_cron():
    cur_text = '@Scheduled(cron = "${dristi.scheduler.cron}") public void tick() {}'
    assert "dristi.scheduler.cron" in parse_all_key_references(cur_text)


def test_scheduled_fixed_delay_string():
    cur_text = (
        '@Scheduled(fixedDelayString = "${dristi.poll.delay}") public void tick() {}'
    )
    assert "dristi.poll.delay" in parse_all_key_references(cur_text)


# ── @ConditionalOnProperty (bare strings, no ${}) ────────────────────


def test_conditional_on_property_name():
    cur_text = '@ConditionalOnProperty(name = "feature.x.enabled") class X {}'
    assert "feature.x.enabled" in parse_all_key_references(cur_text)


def test_conditional_on_property_multiline():
    cur_text = """
    @ConditionalOnProperty(
        prefix = "feature.x",
        name = "enabled",
        havingValue = "true"
    )
    public class X {}
    """
    keys = parse_all_key_references(cur_text)
    # Both prefix and name match the regex; harmless overcapture but
    # the gate is conservative — better to flag a live key than miss one.
    assert "feature.x" in keys, keys
    assert "enabled" in keys, keys


# ── env.getProperty ──────────────────────────────────────────────────


def test_env_get_property():
    cur_text = 'String v = environment.getRequiredProperty("egov.runtime.flag");'
    assert "egov.runtime.flag" in parse_all_key_references(cur_text)


def test_env_get_property_short_alias():
    cur_text = 'String v = env.getProperty("egov.runtime.flag");'
    assert "egov.runtime.flag" in parse_all_key_references(cur_text)


# ── Sanity: parse_all is a superset of parse_value ───────────────────


def test_parse_all_is_superset_of_parse_value():
    text = """
    @Value("${egov.foo.host}") String h;
    @KafkaListener(topics = "${kafka.bar.topic}") void on(Object r) {}
    @Scheduled(cron = "${cron.expr}") void tick() {}
    """
    v = parse_value_keys(text)
    a = parse_all_key_references(text)
    assert v <= a, (v, a)
    assert {"kafka.bar.topic", "cron.expr"} <= a - v


# ── Driver ──────────────────────────────────────────────────────────


def main() -> int:
    tests = [
        v for k, v in globals().items()
        if k.startswith("test_") and callable(v)
    ]
    failures: list[tuple[str, Exception]] = []
    for t in tests:
        try:
            t()
        except AssertionError as e:
            failures.append((t.__name__, e))
        except Exception as e:
            failures.append((t.__name__, e))
    if failures:
        print(f"FAIL: {len(failures)}/{len(tests)}")
        for name, err in failures:
            print(f"  {name}: {err}")
        return 1
    print(f"PASS: {len(tests)} test(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
