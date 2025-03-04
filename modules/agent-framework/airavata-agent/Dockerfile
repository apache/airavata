FROM mambaorg/micromamba:latest

USER root

RUN micromamba install -y -n base -c conda-forge \
    "python<3.12" \
    pip \
    ipykernel \
    git \
    flask \
    jupyter_client \
    && micromamba clean --all -y

COPY airavata-agent /opt/airavata-agent
COPY kernel.py /opt/jupyter/kernel.py
