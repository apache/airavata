from oauthenticator.generic import GenericOAuthenticator
import os

# Authenticator Configuration
c.JupyterHub.authenticator_class = GenericOAuthenticator
c.GenericOAuthenticator.client_id = os.getenv('OAUTH_CLIENT_ID')
c.GenericOAuthenticator.client_secret = os.getenv('OAUTH_CLIENT_SECRET')
c.GenericOAuthenticator.oauth_callback_url = 'http://3.147.153.73:8000/hub/oauth_callback'
c.GenericOAuthenticator.authorize_url = 'https://auth.cybershuttle.org/realms/10000000/protocol/openid-connect/auth'
c.GenericOAuthenticator.token_url = 'https://auth.cybershuttle.org/realms/10000000/protocol/openid-connect/token'
c.GenericOAuthenticator.userdata_url = 'https://auth.cybershuttle.org/realms/10000000/protocol/openid-connect/userinfo'
c.GenericOAuthenticator.scope = ['openid', 'profile', 'email']
c.GenericOAuthenticator.username_claim = 'email'

# User Permissions
c.Authenticator.enable_auth_state = True
c.Authenticator.allowed_users = set()
# c.Authenticator.admin_users = {'user@airavata'} TODO Add admin users

# Spawner Configuration
c.JupyterHub.spawner_class = 'dockerspawner.DockerSpawner'
c.DockerSpawner.container_image = 'jupyter/base-notebook:latest'
c.DockerSpawner.notebook_dir = '/home/jovyan/work'
c.DockerSpawner.volumes = {
    'jupyterhub-user-{username}': '/home/jovyan/work',
}
c.DockerSpawner.environment = {
    'CHOWN_HOME': 'yes',
    'CHOWN_HOME_OPTS': '-R',
}
c.DockerSpawner.extra_create_kwargs = {'user': 'root'}
c.DockerSpawner.use_internal_ip = True
c.DockerSpawner.network_name = os.getenv('DOCKER_NETWORK_NAME', 'jupyterhub_network')

# Hub Configuration
c.JupyterHub.hub_ip = '0.0.0.0'
c.JupyterHub.hub_port = 8081
c.JupyterHub.hub_connect_ip = 'jupyterhub'

# Logging
c.JupyterHub.log_level = 'DEBUG'
