# --- сборка WAR ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY src src
RUN chmod +x gradlew && ./gradlew --no-daemon war

# --- WildFly (Jakarta EE 10) ---
FROM quay.io/wildfly/wildfly:34.0.1.Final-jdk17
ARG PG_VERSION=42.7.4
ADD --chmod=644 https://repo1.maven.org/maven2/org/postgresql/postgresql/${PG_VERSION}/postgresql-${PG_VERSION}.jar /tmp/postgresql.jar
COPY --chown=jboss:root docker/server.p12 /opt/jboss/wildfly/standalone/configuration/server.p12
COPY docker/configure.cli /tmp/configure.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure.cli \
 && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current
COPY --from=build --chown=jboss:root /src/build/libs/dragon-service.war /opt/jboss/wildfly/standalone/deployments/
