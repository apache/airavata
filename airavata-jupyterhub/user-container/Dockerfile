FROM quay.io/jupyter/base-notebook:latest

COPY init.sh /usr/local/bin/init.sh

USER root
RUN chmod +x /usr/local/bin/init.sh
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

USER $NB_USER
RUN pip install ipywidgets jupyterlab-git

CMD ["start-notebook.py"]