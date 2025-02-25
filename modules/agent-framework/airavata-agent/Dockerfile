FROM python:3.12-slim

RUN pip install flask jupyter jupyter-client
RUN mkdir -p /opt/jupyter
RUN python -m venv /opt/jupyter/venv
ADD airavata-agent /opt/airavata-agent
ADD jupyter/kernel.py /opt/jupyter/kernel.py
