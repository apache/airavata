FROM jupyter/base-notebook:latest

COPY data /tmp/default_data/
COPY poc.ipynb settings.ini /tmp/default_configs/

COPY init.sh /usr/local/bin/init.sh

USER root
RUN chmod +x /usr/local/bin/init.sh

USER $NB_USER

ENTRYPOINT ["/usr/local/bin/init.sh"]
CMD ["start-notebook.sh"]