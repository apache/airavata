# =============================================================================
# Combines all Airavata services into one monolithic container
# =============================================================================

FROM eclipse-temurin:17-jre-jammy

# Install necessary packages including multitail for log monitoring
RUN apt-get update && apt-get install -y \
    curl \
    netcat-openbsd \
    multitail \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r airavata && useradd -r -g airavata -d /opt/airavata airavata

# Set working directory
WORKDIR /opt/airavata

# Copy all distribution files
COPY distribution/apache-airavata-api-server-*.tar.gz ./
COPY distribution/apache-airavata-agent-service-*.tar.gz ./
COPY distribution/apache-airavata-research-service-*.tar.gz ./
COPY distribution/apache-airavata-file-server-*.tar.gz ./

# Extract all services
RUN tar -xzf apache-airavata-api-server-*.tar.gz && \
    tar -xzf apache-airavata-agent-service-*.tar.gz && \
    tar -xzf apache-airavata-research-service-*.tar.gz && \
    tar -xzf apache-airavata-file-server-*.tar.gz && \
    rm *.tar.gz

# Rename directories for consistency
RUN mv apache-airavata-api-server-* apache-airavata-api-server && \
    mv apache-airavata-agent-service-* apache-airavata-agent-service && \
    mv apache-airavata-research-service-* apache-airavata-research-service && \
    mv apache-airavata-file-server-* apache-airavata-file-server

# Create necessary directories
RUN mkdir -p apache-airavata-api-server/conf \
    apache-airavata-api-server/logs \
    apache-airavata-api-server/temp \
    apache-airavata-api-server/data \
    apache-airavata-api-server/keystores

# Set environment variables
ENV AIRAVATA_HOME=/opt/airavata/apache-airavata-api-server
ENV AIRAVATA_AGENT_HOME=/opt/airavata/apache-airavata-agent-service
ENV AIRAVATA_RESEARCH_HOME=/opt/airavata/apache-airavata-research-service
ENV AIRAVATA_FILE_HOME=/opt/airavata/apache-airavata-file-server
ENV JAVA_HOME=/usr/lib/jvm/temurin-17-jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# JVM tuning for production
ENV JAVA_OPTS="-server \
    -Xms1g \
    -Xmx4g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/opt/airavata/apache-airavata-api-server/logs \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=UTC"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8930/ || exit 1

# All Thrift services multiplexed on single port
EXPOSE 8930
# file service
EXPOSE 8050
# agent service (http + gRPC)
EXPOSE 18800
EXPOSE 19900
# research service (http + gRPC)
EXPOSE 18889
EXPOSE 19908
# monitoring (prometheus)
EXPOSE 9097

# Copy startup script
COPY dev-tools/deployment-scripts/docker-startup.sh /opt/airavata/start.sh

# Set ownership
RUN chown -R airavata:airavata /opt/airavata && \
    chmod +x /opt/airavata/start.sh

# Switch to non-root user
USER airavata

# Set entrypoint
ENTRYPOINT ["/opt/airavata/start.sh"] 
