FROM python:slim

RUN apt update
RUN apt install fuse -y
RUN pip install notebook
RUN apt install kmod -y

COPY labconfig/jupyter_lab_config.py /jupyter_lab_config.py
COPY fuse/client /client
EXPOSE 8888
WORKDIR /home

ENTRYPOINT ["jupyter", "lab", "--config=/jupyter_lab_config.py", "--ip=0.0.0.0", "--port=8888", "--no-browser", "--allow-root", "--NotebookApp.token=''"]
