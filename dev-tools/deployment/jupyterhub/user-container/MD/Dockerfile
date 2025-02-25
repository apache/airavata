FROM jupyter/base-notebook:latest

COPY data /tmp/default_data/
COPY poc.ipynb settings.ini /tmp/default_configs/

COPY labconfig/jupyter_lab_config.py /jupyter_lab_config.py
COPY labconfig/airavata_magics.py /airavata_magics.py
COPY labconfig/__init__.py /__init__.py
COPY labconfig/device_auth.py /device_auth.py
COPY labconfig/bootstrap.sh /bootstrap.sh

COPY init.sh /usr/local/bin/init.sh

# Create IPython startup directory
RUN mkdir -p /home/jovyan/.ipython/profile_default/startup
COPY labconfig/ipython_startup.py /home/jovyan/.ipython/profile_default/startup/ipython_startup.py

USER root
RUN chmod +x /usr/local/bin/init.sh
RUN chmod +x /bootstrap.sh
RUN chown -R jovyan:users /home/jovyan/.ipython

USER $NB_USER

ENTRYPOINT ["/usr/local/bin/init.sh"]
CMD ["start-notebook.sh"]