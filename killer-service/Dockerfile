# Тег образа Payara (jdk17+: Spring 6 не работает на Java 11)
ARG PAYARA_TAG=6.2025.1-jdk17

# --- сборка WAR ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY src src
RUN --mount=type=cache,target=/root/.gradle chmod +x gradlew && ./gradlew --no-daemon war

# --- Payara ---
FROM payara/server-full:${PAYARA_TAG}
# truststore с сертификатом сервиса 1 (его server.crt лежит в docker/)
COPY --chown=payara:payara docker/server.crt /tmp/server.crt
RUN keytool -importcert -noprompt -alias soa -file /tmp/server.crt \
    -keystore /opt/payara/truststore.p12 -storetype PKCS12 -storepass changeit
# HTTPS самого Payara: наш сертификат вместо дефолтного s1as в keystore домена
COPY --chown=payara:payara docker/server.p12 /tmp/server.p12
RUN KS=/opt/payara/appserver/glassfish/domains/domain1/config/keystore.p12 \
 && keytool -delete -alias s1as -keystore $KS -storepass changeit \
 && keytool -importkeystore -srckeystore /tmp/server.p12 -srcstoretype PKCS12 -srcstorepass changeit -srcalias soa \
      -destkeystore $KS -deststoretype PKCS12 -deststorepass changeit -destalias s1as \
 && rm /tmp/server.p12
ENV SERVICE1_TRUSTSTORE_PATH=/opt/payara/truststore.p12 \
    SERVICE1_TRUSTSTORE_PASSWORD=changeit
COPY --from=build --chown=payara:payara /src/build/libs/killer-service.war /opt/payara/deployments/
