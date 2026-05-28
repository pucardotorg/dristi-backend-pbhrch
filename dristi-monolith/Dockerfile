# =============================================================================
# Stage 1 – Build
# Build context must be the repository root (parent of dristi-monolith/).
# Example:
#   docker build -f dristi-monolith/Dockerfile -t dristi-monolith:latest .
# =============================================================================
FROM maven:3.8.4-openjdk-17-slim AS build

WORKDIR /app

# ---------------------------------------------------------------------------
# 1. Copy the root multi-module POM so Maven can resolve the reactor order
#    and download dependencies in a cached layer before copying source code.
# ---------------------------------------------------------------------------
COPY dristi-monolith/pom.xml ./pom.xml

# Copy each module's POM (allows dependency download to be cached separately
# from source changes).
COPY dristi-monolith/dristi-common/pom.xml            ./dristi-common/pom.xml
COPY dristi-monolith/domain-case-lifecycle/pom.xml    ./domain-case-lifecycle/pom.xml
COPY dristi-monolith/domain-identity-access/pom.xml   ./domain-identity-access/pom.xml
COPY dristi-monolith/domain-integration/pom.xml       ./domain-integration/pom.xml
COPY dristi-monolith/domain-payments/pom.xml          ./domain-payments/pom.xml
COPY dristi-monolith/dristi-app/pom.xml               ./dristi-app/pom.xml

# Pre-download dependencies (best-effort; skipped when offline)
RUN mvn -B dependency:go-offline -f /app/pom.xml || true

# ---------------------------------------------------------------------------
# 2. Copy all source trees and build the full reactor.
#    `dristi-app` uses spring-boot-maven-plugin:repackage, producing the
#    runnable fat-jar at dristi-app/target/dristi-app-*.jar.
# ---------------------------------------------------------------------------
COPY dristi-monolith/dristi-common/src            ./dristi-common/src
COPY dristi-monolith/domain-case-lifecycle/src    ./domain-case-lifecycle/src
COPY dristi-monolith/domain-identity-access/src   ./domain-identity-access/src
COPY dristi-monolith/domain-integration/src       ./domain-integration/src
COPY dristi-monolith/domain-payments/src          ./domain-payments/src
COPY dristi-monolith/dristi-app/src               ./dristi-app/src

RUN mvn -B -f /app/pom.xml package -DskipTests

# =============================================================================
# Stage 2 – Runtime
# Slim JRE image; same base used by the existing microservice Dockerfiles.
# =============================================================================
FROM openjdk:24-ea-15-oraclelinux8

WORKDIR /opt/egov

# Copy the fat-jar produced by dristi-app
COPY --from=build /app/dristi-app/target/dristi-app-*.jar /opt/egov/dristi-app.jar

# Reuse the same start.sh pattern from build/maven/start.sh
COPY build/maven/start.sh /opt/egov/start.sh

RUN chmod +x /opt/egov/start.sh

# Expose the default Spring Boot port
EXPOSE 8080

CMD ["/opt/egov/start.sh"]
