FROM jupyterhub/jupyterhub:latest

RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*
RUN pip install oauthenticator requests pyjwt dockerspawner jupyterhub-idle-culler ipywidgets

COPY jupyterhub_config.py /srv/jupyterhub/jupyterhub_config.py
COPY custom_templates /srv/jupyterhub/custom_templates

ENV JUPYTERHUB_CONFIG=/srv/jupyterhub/jupyterhub_config.py
ENV PYTHONPATH=/srv/jupyterhub

EXPOSE 20000

CMD ["jupyterhub"]