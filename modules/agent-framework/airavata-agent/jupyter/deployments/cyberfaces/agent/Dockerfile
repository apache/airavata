FROM cybershuttle/base-cyberfaces

USER root

RUN pip install flask jupyter jupyter-client
RUN mkdir -p /opt/jupyter
RUN python -m venv /opt/jupyter/venv
ADD airavata-agent-linux /opt/airavata-agent
ADD kernel.py /opt/jupyter/kernel.py