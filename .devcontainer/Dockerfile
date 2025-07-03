FROM ubuntu:22.04

# Avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    git \
    build-essential \
    pkg-config \
    libssl-dev \
    libffi-dev \
    python3-dev \
    python3-pip \
    python3-venv \
    software-properties-common \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# Install Maven
RUN wget https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz \
    && tar -xzf apache-maven-3.9.9-bin.tar.gz \
    && mv apache-maven-3.9.9 /opt/maven \
    && rm apache-maven-3.9.9-bin.tar.gz

ENV MAVEN_HOME=/opt/maven
ENV PATH=$PATH:$MAVEN_HOME/bin

# Install Python 3.10
RUN apt-get update && apt-get install -y python3.10 python3.10-dev python3.10-venv \
    && rm -rf /var/lib/apt/lists/*

# Install Go
RUN wget https://go.dev/dl/go1.23.0.linux-amd64.tar.gz \
    && tar -C /usr/local -xzf go1.23.0.linux-amd64.tar.gz \
    && rm go1.23.0.linux-amd64.tar.gz

ENV GOROOT=/usr/local/go
ENV GOPATH=$HOME/go
ENV PATH=$PATH:$GOROOT/bin:$GOPATH/bin

# Install Thrift 0.23
RUN wget https://dlcdn.apache.org/thrift/0.22.0/thrift-0.22.0.tar.gz \
    && tar -xzf thrift-0.22.0.tar.gz \
    && cd thrift-0.22.0 \
    && ./configure --without-rs --enable-libs=no --enable-tests=no \
    && make -j$(nproc) \
    && make install \
    && cd .. \
    && rm -rf thrift-0.22.0 thrift-0.22.0.tar.gz

# Create non-root user
RUN useradd -m -s /bin/bash vscode
USER vscode

# Set working directory
WORKDIR /home/vscode
