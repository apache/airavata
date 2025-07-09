FROM ubuntu:22.04 as deps

ARG TARGETARCH

# Avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies, python 3.10, and java 17
RUN apt-get update && \
    apt-get install -y \
    curl wget git build-essential pkg-config libssl-dev libffi-dev software-properties-common \
    python3.10 python3.10-dev \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# Install maven 3.9.10
RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.tar.gz \
    && tar -xzf apache-maven-3.9.10-bin.tar.gz \
    && mv apache-maven-3.9.10 /opt/maven \
    && rm apache-maven-3.9.10-bin.tar.gz

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
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-$TARGETARCH
ENV MAVEN_HOME=/opt/maven
ENV GOROOT=/usr/local/go
ENV GOPATH=/home/developer/go
ENV PATH=$PATH:$JAVA_HOME/bin:$MAVEN_HOME/bin:$GOROOT/bin:$GOPATH/bin

EXPOSE 7878 8000 17000 8050 8930 9930 8960 8962 8970 18800 19900 18889 19908

FROM deps AS compiler

COPY --chown=developer:developer .. /home/developer/workspace
WORKDIR /home/developer/workspace
RUN mvn clean install -DskipTests


FROM eclipse-temurin:17-jdk AS runner
WORKDIR /app

COPY --from=compiler /home/developer/workspace/distribution/*.tar.gz ./

COPY ../dev-tools/deployment-scripts/*.sh ./
COPY ../vault ./vault

RUN chmod +x ./distribution_update.sh ./services_up.sh

EXPOSE 7878 8000 17000 8050 8930 9930 8960 8962 8970 18800 19900 18889 19908

CMD ["sh", "-c", "./distribution_update.sh && ./services_up.sh && sleep infinity"]
