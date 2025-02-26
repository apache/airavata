FROM cybershuttle/remote-agent-base:latest

USER root

RUN micromamba install -y -c conda-forge \
    matplotlib \
    numpy \
    pandas \
    && micromamba clean --all -y

COPY data /opt/data
