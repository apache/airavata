# =============================================================================
# Unified Airavata Server container
# =============================================================================

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update && apt-get install -y \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r airavata && useradd -r -g airavata -d /opt/airavata airavata

WORKDIR /opt/airavata

# Copy the unified server fat JAR
COPY airavata-server/target/airavata-server-0.21-SNAPSHOT.jar ./airavata-server.jar

# Create directories
RUN mkdir -p logs keystores

# Environment
ENV JAVA_HOME=/usr/lib/jvm/temurin-17-jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# JVM tuning for production
ENV JAVA_OPTS="-server \
    -Xms1g \
    -Xmx4g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/opt/airavata/logs \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=UTC"

# Health check via monitoring endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9090/internal/actuator/health || exit 1

# Armeria (gRPC + REST + Docs + Actuator)
EXPOSE 9090
# Monitoring (Prometheus + health)
EXPOSE 9097

RUN chown -R airavata:airavata /opt/airavata
USER airavata

# Import the devstack mkcert CA into a writable truststore copy at container start.
# The CA file is mounted at /certs/rootCA.pem (see compose.yml). The import is
# idempotent: skip if the alias mkcert-airavata already exists.
ENTRYPOINT ["/bin/sh", "-c", "\
  JH=$(dirname $(dirname $(readlink -f $(command -v java)))); \
  cp \"$JH/lib/security/cacerts\" /tmp/airavata-truststore.p12; \
  keytool -list -alias mkcert-airavata -keystore /tmp/airavata-truststore.p12 -storepass changeit >/dev/null 2>&1 \
    || keytool -importcert -noprompt -alias mkcert-airavata -file /certs/rootCA.pem \
       -keystore /tmp/airavata-truststore.p12 -storepass changeit \
    || echo 'WARN: mkcert CA import failed; https to Keycloak may not validate'; \
  exec java $JAVA_OPTS -Djavax.net.ssl.trustStore=/tmp/airavata-truststore.p12 \
       -Djavax.net.ssl.trustStorePassword=changeit \
       -jar airavata-server.jar\
"]
