FROM jupyter/base-notebook:latest

COPY init.sh /usr/local/bin/init.sh

USER root
RUN chmod +x /usr/local/bin/init.sh
RUN apt-get update && apt-get install -y git

USER $NB_USER

ENTRYPOINT ["/usr/local/bin/init.sh"]
CMD ["start-notebook.sh"]