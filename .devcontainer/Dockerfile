FROM mcr.microsoft.com/devcontainers/base:debian

USER vscode

RUN curl https://mise.run | sh
RUN ~/.local/bin/mise install -y java@openjdk-17 maven@3.9.9 python@3.10 golang@1.23; \
  ~/.local/bin/mise use -g java@openjdk-17 maven@3.9.9 python@3.10 golang@1.23; \
  ~/.local/bin/mise env >> ~/.bashrc
