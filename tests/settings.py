import os

from django_airavata.settings import *  # noqa

# Above imports the common settings, can override them below as needed

BASEDIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Use a SQLite database for testing
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'TEST': {
            'ENGINE': 'django.db.backends.sqlite3',
            'NAME': os.path.join(BASEDIR, 'test-db.sqlite3'),
        }
    }
}

# Settings that are expected to be defined in settings_local.py
AIRAVATA_API_HOST = 'localhost'
AIRAVATA_API_PORT = 8930
AIRAVATA_API_SECURE = False

PROFILE_SERVICE_HOST = AIRAVATA_API_HOST
PROFILE_SERVICE_PORT = 8962
PROFILE_SERVICE_SECURE = False

PORTAL_TITLE = 'Django Airavata Gateway'
