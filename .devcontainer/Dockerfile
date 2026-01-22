FROM ubuntu:22.04 as deps

ARG TARGETARCH

# Avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies, python 3.10, and java 25
RUN apt-get update && \
    apt-get install -y \
    curl wget git build-essential pkg-config libssl-dev libffi-dev software-properties-common \
    python3.10 python3.10-dev \
    openjdk-25-jdk \
    && rm -rf /var/lib/apt/lists/*

# Install maven 3.9.12
RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.12/binaries/apache-maven-3.9.12-bin.tar.gz \
    && tar -xzf apache-maven-3.9.12-bin.tar.gz \
    && mv apache-maven-3.9.12 /opt/maven \
    && rm apache-maven-3.9.12-bin.tar.gz

# Install go 1.23.0
RUN wget https://go.dev/dl/go1.23.0.linux-amd64.tar.gz \
    && tar -C /usr/local -xzf go1.23.0.linux-amd64.tar.gz \
    && rm go1.23.0.linux-amd64.tar.gz

# Install Thrift 0.22
RUN wget https://dlcdn.apache.org/thrift/0.22.0/thrift-0.22.0.tar.gz \
    && tar -xzf thrift-0.22.0.tar.gz \
    && cd thrift-0.22.0 \
    && ./configure --without-rs --enable-libs=no --enable-tests=no \
    && make -j$(nproc) \
    && make install \
    && cd .. \
    && rm -rf thrift-0.22.0 thrift-0.22.0.tar.gz

# Create non-root user (developer)
RUN useradd -m -s /bin/bash developer
USER developer
WORKDIR /home/developer

# Set environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-25-openjdk-$TARGETARCH
ENV MAVEN_HOME=/opt/maven
ENV GOROOT=/usr/local/go
ENV GOPATH=/home/developer/go
ENV PATH=$PATH:$JAVA_HOME/bin:$MAVEN_HOME/bin:$GOROOT/bin:$GOPATH/bin

FROM deps AS compiler

COPY --chown=developer:developer .. /home/developer/workspace
WORKDIR /home/developer/workspace
RUN mvn clean install -DskipTests


FROM eclipse-temurin:25-jdk AS runner
WORKDIR /app

COPY --from=compiler /home/developer/workspace/distribution/*.tar.gz ./

COPY ../dev-tools/deployment-scripts/*.sh ./
COPY ../conf ./conf

RUN chmod +x ./distribution_update.sh ./services_up.sh

# Thrift Server (8930)
EXPOSE 8930
# HTTP Server - Airavata API, File API, Agent API, Research API (8080)
EXPOSE 8080
# gRPC API (9090)
EXPOSE 9090
# Dapr Sidecar (50001)
EXPOSE 50001

CMD ["sh", "-c", "./distribution_update.sh && ./services_up.sh && sleep infinity"]
