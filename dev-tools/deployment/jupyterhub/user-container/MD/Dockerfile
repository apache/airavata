FROM jupyter/base-notebook:latest

COPY data /home/jovyan/data/
COPY poc.ipynb settings.ini /home/jovyan/

RUN chown -R jovyan:users /home/jovyan/data /home/jovyan/settings.ini