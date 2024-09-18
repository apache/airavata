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

RUN pip install flask jupyter jupyter-client
RUN mkdir -p /opt/jupyter
RUN python -m venv /opt/jupyter/venv
ADD airavata-agent-linux /opt/airavata-agent
ADD kernel.py /opt/jupyter/kernel.py