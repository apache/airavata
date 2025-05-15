FROM jupyterhub/jupyterhub:3.0

RUN pip install oauthenticator requests pyjwt dockerspawner jupyterhub-idle-culler
RUN apt-get update && \
    apt-get install -y git && \
    pip install oauthenticator requests pyjwt dockerspawner jupyterhub-idle-culler ipywidgets


COPY jupyterhub_config.py /srv/jupyterhub/jupyterhub_config.py

COPY custom_templates /srv/jupyterhub/custom_templates

RUN mkdir -p /home/jovyan/notebooks && \
    chown -R 1000:100 /home/jovyan/notebooks

ENV JUPYTERHUB_CONFIG=/srv/jupyterhub/jupyterhub_config.py
ENV PYTHONPATH=/srv/jupyterhub

EXPOSE 8000

CMD ["jupyterhub"]