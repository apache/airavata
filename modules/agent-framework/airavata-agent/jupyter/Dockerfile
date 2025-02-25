FROM python:slim

RUN apt update
RUN apt install fuse -y
RUN pip install notebook
RUN apt install kmod -y
RUN apt install git -y
RUN pip install PyJWT
RUN pip install cybershuttle-tune==0.1.19
RUN pip install scipy
RUN pip install numpy
RUN pip install matplotlib
RUN git clone https://github.com/cyber-shuttle/jupyter-notebook-examples /home/jupyter-notebook-examples

COPY labconfig/jupyter_lab_config.py /jupyter_lab_config.py
COPY labconfig/airavata_magics.py /airavata_magics.py
COPY labconfig/__init__.py /__init__.py
COPY labconfig/bootstrap.sh /bootstrap.sh
RUN chmod +x /bootstrap.sh
# COPY fuse/client /client
EXPOSE 8888
WORKDIR /home

ENTRYPOINT ["sh", "/bootstrap.sh"]
