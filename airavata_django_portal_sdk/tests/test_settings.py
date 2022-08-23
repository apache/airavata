import os

BASEDIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

SECRET_KEY = "abc123"
INSTALLED_APPS = [
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'airavata_django_portal_sdk',
]
GATEWAY_DATA_STORE_DIR = "/tmp"
GATEWAY_DATA_STORE_RESOURCE_ID = "resourceId"
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': os.path.join(BASEDIR, 'db.sqlite3'),
    }
}
