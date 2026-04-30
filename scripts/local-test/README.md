# Local-test recipe — dristi-monolith

End-to-end steps to run the monolith locally and exercise the migrated
subdomains. Currently covers `lock-svc` (`/lock-svc/v1/_set`, `/v1/_get`,
`/v1/_release`) and `case` (`/case/v1/_search` and the rest of the
`/case` controller). One Postgres DB named `pucar` hosts every
subdomain's schema; per-subdomain Flyway migrations run on boot under
the unified `public-monolith` history table.

Estimated time on a fresh machine: ~10 minutes (Docker pulls dominate).

## Prerequisites

- JDK 17 — script auto-resolves `~/.jdks/corretto-17.0.18` if `javac` isn't on PATH
- Docker daemon running
- `curl` (and `jq` recommended for response inspection)

## 1. Start Postgres

```bash
docker run -d --name dristi-pg \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=pucar \
  -p 5432:5432 \
  postgres:14

# wait until ready
until docker exec dristi-pg pg_isready -U postgres >/dev/null 2>&1; do sleep 1; done
echo "postgres ready"
```

## 2. Start WireMock (egov-individual stub)

```bash
docker run -d --name dristi-wiremock \
  -p 8081:8080 \
  -v "$(pwd)/scripts/local-test:/home/wiremock/mappings:ro" \
  wiremock/wiremock:3.6.0
```

Verify the stub:
```bash
curl -s -X POST http://localhost:8081/individual/v1/_search | jq .Individual[0].individualId
# expect: "IND-LOCAL-1234"
```

## 3. Build + boot the monolith

From the repo root:
```bash
cd dristi-monolith
JAVA_HOME=/home/mani/.jdks/corretto-17.0.18 \
  PATH=/home/mani/.jdks/corretto-17.0.18/bin:$PATH \
  mvn -B -pl dristi-app -am spring-boot:run
```

Expect to see in the log:
```
Started DristiApplication in N.NNN seconds
```

If it fails on first run with "Schema doesn't exist", make sure Postgres is
up and the `pucar` DB exists (Step 1 created it).

Verify Flyway ran every subdomain's migrations:

```bash
docker exec dristi-pg psql -U postgres -d pucar -c '\dt' | grep -E 'lock|dristi_cases'
# expect: rows for `lock` (from locksvc) and `dristi_cases` (from cases),
# plus the unified `public-monolith` history table.
```

## 4. Smoke test the lock endpoints

In another terminal:

```bash
# Health (Spring Boot actuator — no service prefix)
curl -s http://localhost:8080/actuator/health
# expect: {"status":"UP"}

# lock-svc endpoints — class-level @RequestMapping("/lock-svc") in
# LockApiController, set automatically by the per-module migration
# pipeline from the source service's server.servlet.context-path.
curl -s -X POST http://localhost:8080/lock-svc/v1/_set \
  -H 'Content-Type: application/json' \
  -d @scripts/local-test/sample-set-lock.json | jq

curl -s -X POST "http://localhost:8080/lock-svc/v1/_get?uniqueId=case-LOCAL-TEST-1&tenantId=kl" \
  -H 'Content-Type: application/json' \
  -d @scripts/local-test/sample-get-lock-request.json | jq

curl -s -X POST "http://localhost:8080/lock-svc/v1/_release?uniqueId=case-LOCAL-TEST-1&tenantId=kl" \
  -H 'Content-Type: application/json' \
  -d @scripts/local-test/sample-get-lock-request.json | jq

# case endpoints — same pattern, class-level @RequestMapping("/case").
# Liveness check: hit search with an empty body. Expect a 4xx
# (validation rejection from the controller) — that proves the route is
# mapped and the schema is queryable. A 404 means the controller didn't
# wire; a 5xx with "relation does not exist" means Flyway never ran.
curl -sw '\nstatus=%{http_code}\n' -X POST http://localhost:8080/case/v1/_search \
  -H 'Content-Type: application/json' -d '{}'
# expect: status=400 (or similar 4xx), JSON body with a validation message.

# Full sample bodies for case endpoints aren't shipped here yet — capture
# from the QA postman collection or the original case service when you
# need to exercise the happy path.
```

Expected: `_set` returns the lock with `isLocked: true`; `_get` returns
`isLocked: true` immediately after; `_release` returns `isLocked: false`.

## 5. Cleanup

```bash
docker rm -f dristi-pg dristi-wiremock
```

## Side-by-side regression vs the original lock-svc

If you have lock-svc running locally on a different port (or in dev), replay
the same sample requests against both and diff the JSON:

```bash
PORT_OLD=18080
PORT_NEW=8080

for action in _set _get _release; do
  body=scripts/local-test/sample-${action#_}-lock.json
  [ -f "$body" ] || body=scripts/local-test/sample-get-lock-request.json
  curl -s -X POST "http://localhost:${PORT_OLD}/lock-svc/v1/${action}?uniqueId=case-LOCAL-TEST-1&tenantId=kl" \
    -H 'Content-Type: application/json' -d @"$body" > /tmp/old-${action}.json
  curl -s -X POST "http://localhost:${PORT_NEW}/lock-svc/v1/${action}?uniqueId=case-LOCAL-TEST-1&tenantId=kl" \
    -H 'Content-Type: application/json' -d @"$body" > /tmp/new-${action}.json
  echo "=== ${action} ==="
  diff <(jq -S 'del(.responseInfo.ts, .responseInfo.resMsgId)' /tmp/old-${action}.json) \
       <(jq -S 'del(.responseInfo.ts, .responseInfo.resMsgId)' /tmp/new-${action}.json) \
    && echo "OK — identical (modulo timestamps)"
done
```

A clean `OK` for all three confirms the monolith is API-equivalent to the
original lock-svc microservice.

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `Connection refused` on startup | Postgres not up | re-run Step 1 |
| `Failed to determine a suitable driver class` | `application-local.yml` not on classpath | confirm `spring.profiles.active=local` (default in `application.yml`) |
| `404 /individual/v1/_search` | WireMock mapping not loaded | confirm Step 2's `-v` mount points at the directory containing `wiremock-*.json` |
| `release version 17 not supported` | maven using JRE not JDK | export `JAVA_HOME=~/.jdks/corretto-17.0.18` before running mvn |
| Spring fails on missing `@Value` for `egov.<x>.host` | property not in yml | add a stub URL in `application-local.yml` |
