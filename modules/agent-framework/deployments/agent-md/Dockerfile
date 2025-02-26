FROM cybershuttle/remote-agent-base:latest

USER root

RUN micromamba install -y -c conda-forge \
    matplotlib \
    numpy \
    pandas \
    scipy \
    cbarnett::namd \
    mdanalysis \
    && micromamba clean --all -y
