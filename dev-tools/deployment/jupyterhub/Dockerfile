FROM jupyterhub/jupyterhub:3.0

RUN pip install oauthenticator requests pyjwt dockerspawner jupyterhub-idle-culler

COPY jupyterhub_config.py /srv/jupyterhub/jupyterhub_config.py

RUN mkdir -p /home/jovyan/notebooks && \
    chown -R 1000:100 /home/jovyan/notebooks

ENV JUPYTERHUB_CONFIG=/srv/jupyterhub/jupyterhub_config.py
ENV PYTHONPATH=/srv/jupyterhub

EXPOSE 8000

CMD ["jupyterhub"]