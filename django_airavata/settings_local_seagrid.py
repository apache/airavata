"""
Override default Django settings for a particular instance.

Copy this file to settings_local.py and modify as appropriate. This file will
be imported into settings.py last of all so settings in this file override any
defaults specified in settings.py.
"""

import os

from . import webpack_loader_util

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Django configuration

DEBUG = True
ALLOWED_HOSTS = ['localhost']

# Django - Email Settings
# Uncomment and specify the following for sending emails (default email backend
# just prints to the console)
# EMAIL_BACKEND = 'django.core.mail.backends.smtp.EmailBackend'
# EMAIL_HOST = 'smtp.gmail.com'
# EMAIL_PORT = 587
# EMAIL_HOST_USER = 'pga.airavata@gmail.com'
# EMAIL_HOST_PASSWORD = 'airavata12'
# EMAIL_USE_TLS = True
ADMINS = [('Marcus Christie', 'machrist@iu.edu')]
SERVER_EMAIL = 'pga.airavata@gmail.com'

# Keycloak Configuration
KEYCLOAK_CLIENT_ID = 'pga'
KEYCLOAK_CLIENT_SECRET = '5d2dc66a-f54e-4fa9-b78f-80d33aa862c1'
KEYCLOAK_AUTHORIZE_URL = 'https://iamdev.scigap.org/auth/realms/seagrid/protocol/openid-connect/auth'
KEYCLOAK_TOKEN_URL = 'https://iamdev.scigap.org/auth/realms/seagrid/protocol/openid-connect/token'
KEYCLOAK_USERINFO_URL = 'https://iamdev.scigap.org/auth/realms/seagrid/protocol/openid-connect/userinfo'
KEYCLOAK_LOGOUT_URL = 'https://iamdev.scigap.org/auth/realms/seagrid/protocol/openid-connect/logout'
KEYCLOAK_CA_CERTFILE = os.path.join(
    BASE_DIR,
    "django_airavata",
    "resources",
    "incommon_rsa_server_ca.pem")
KEYCLOAK_VERIFY_SSL = True

AUTHENTICATION_OPTIONS = {
    'password': {
        'name': 'SEAGrid'
    },
    'external': [
        {
            'idp_alias': 'oidc',
            'name': 'CILogon',
        },
        # {
        #     'idp_alias': 'iucas',
        #     'name': 'IU CAS',
        # },
    ]
}

# Airavata API Configuration
GATEWAY_ID = 'seagrid'
AIRAVATA_API_HOST = 'apidev.scigap.org'
AIRAVATA_API_PORT = 9930
AIRAVATA_API_SECURE = True
# AIRAVATA_API_HOST = 'localhost'
# AIRAVATA_API_PORT = 8930
# AIRAVATA_API_SECURE = False
GATEWAY_DATA_STORE_RESOURCE_ID = 'pgadev.scigap.org_7ddf28fd-d503-4ff8-bbc5-3279a7c3b99e'
GATEWAY_DATA_STORE_DIR = '/tmp/experiment-data-dir'
GATEWAY_DATA_STORE_HOSTNAME = 'localhost'

# Profile Service Configuration
PROFILE_SERVICE_HOST = AIRAVATA_API_HOST
PROFILE_SERVICE_PORT = 8962
PROFILE_SERVICE_SECURE = False

# Sharing API Configuration
SHARING_API_HOST = AIRAVATA_API_HOST
SHARING_API_PORT = 7878
SHARING_API_SECURE = False

# Portal settings
PORTAL_TITLE = 'Django Airavata Gateway'

# Logging configuration
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'verbose': {
            'format': '[%(asctime)s %(name)s:%(lineno)d %(levelname)s] %(message)s'
        },
    },
    'filters': {
        'require_debug_true': {
            '()': 'django.utils.log.RequireDebugTrue',
        },
    },
    'handlers': {
        'console': {
            'filters': ['require_debug_true'],
            'class': 'logging.StreamHandler',
            'formatter': 'verbose',
            'level': 'DEBUG',
        },
        # 'file': {
        #     'class': 'logging.FileHandler',
        #     'filename': 'django_airavata.log',
        #     'formatter': 'verbose',
        # },
    },
    'loggers': {
        'django': {
            'handlers': ['console'],
            'level': os.getenv('DJANGO_LOG_LEVEL', 'INFO'),
        },
        'airavata': {
            'handlers': ['console'],
            'level': 'DEBUG',
        },
        'django_airavata': {
            'handlers': ['console'],
            'level': 'DEBUG',
        },
        'django_airavata.utils': {
            'handlers': ['console'],
            'level': 'DEBUG',
        },
        'thrift_connector': {
            'handlers': ['console'],
            'level': 'DEBUG',
        },
        'simccs_maptool': {
            'handlers': ['console'],
            'level': 'DEBUG',
        },
        # 'requests_oauthlib': {
        #     'handlers': ['console'],
        #     'level': 'DEBUG',
        # },
    },
}

# WEBPACK_LOADER = webpack_loader_util.create_webpack_loader_config("/tmp")
