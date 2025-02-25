FROM cybershuttle/base-cyberfaces

USER root

COPY labconfig/jupyter_lab_config.py /jupyter_lab_config.py
COPY labconfig/airavata_magics.py /airavata_magics.py
COPY labconfig/__init__.py /__init__.py
COPY labconfig/device_auth.py /device_auth.py
COPY labconfig/bootstrap.sh /bootstrap.sh
RUN chmod +x /bootstrap.sh

USER ${NB_UID}

CMD ["start-notebook.sh","--NotebookApp.iopub_data_rate_limit=1e10"]
