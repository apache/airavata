FROM jupyterhub/jupyterhub:3.0

WORKDIR /home/jovyan

RUN pip install oauthenticator requests pyjwt dockerspawner jupyterhub-idle-culler

COPY jupyterhub_config.py /srv/jupyterhub/jupyterhub_config.py
ENV JUPYTERHUB_CONFIG=/srv/jupyterhub/jupyterhub_config.py
ENV PYTHONPATH=/srv/jupyterhub

EXPOSE 8000

CMD ["jupyterhub"]