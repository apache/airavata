FROM rkalyana/iguide-singleuser-cvmfs:nbfetch

USER root

RUN pip install pandas
RUN pip install geopandas
RUN pip install pygeos
RUN pip install matplotlib
RUN pip install "numpy<2"
RUN apt update
RUN apt install -y gdal-bin
RUN pip install -U libpysal
RUN pip install -U esda
RUN pip3 install contextily

COPY labconfig/jupyter_lab_config.py /jupyter_lab_config.py
COPY labconfig/airavata_magics.py /airavata_magics.py
COPY labconfig/__init__.py /__init__.py
COPY labconfig/device_auth.py /device_auth.py
COPY labconfig/bootstrap.sh /bootstrap.sh
RUN chmod +x /bootstrap.sh

COPY deployments/cyberfaces/data.tar.gz /home/jovyan/data.tar.gz
COPY deployments/cyberfaces/analysis.ipynb /home/jovyan/analysis.ipynb

USER ${NB_UID}

CMD ["start-notebook.sh","--NotebookApp.iopub_data_rate_limit=1e10"]
