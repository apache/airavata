# Airavata Django Portal SDK

The Airavata Django Portal SDK provides libraries that assist in developing
custom Django app extensions to the
[Airavata Django Portal](https://github.com/apache/airavata-django-portal).

See the documentation at https://airavata-django-portal-sdk.readthedocs.io/ for
more details.

## Getting Started

To integrate the SDK with an Airavata Django Portal custom app, add

```
"airavata-django-portal-sdk @ git+https://github.com/apache/airavata-django-portal-sdk.git@master#egg=airavata-django-portal-sdk",
```

to the `install_requires` list in your setup.py file. Then with your virtual
environment activated, reinstall your Django app with

```
pip install -e .
```

## Migrations

```
django-admin makemigrations --settings=tests.test_settings airavata_django_portal_sdk
```

## Documentation

To generate the documentation,
[create a virtual environment](https://docs.python.org/3/tutorial/venv.html) and
then:

```
pip install -r requirements-dev.txt
mkdocs serve
```
