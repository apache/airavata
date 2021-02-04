# Airavata Django Portal SDK

[![Build Status](https://travis-ci.com/apache/airavata-django-portal-sdk.svg?branch=master)](https://travis-ci.com/apache/airavata-django-portal-sdk)

The Airavata Django Portal SDK provides libraries that assist in developing
custom Django app extensions to the
[Airavata Django Portal](https://github.com/apache/airavata-django-portal).

See the documentation at https://airavata-django-portal-sdk.readthedocs.io/ for
more details.

## Getting Started

To integrate the SDK with an Airavata Django Portal custom app, add

```
"airavata-django-portal-sdk",
```

to the `install_requires` list in your setup.py file. Then with your virtual
environment activated, either install the SDK directly:

```
pip install -e "git+https://github.com/apache/airavata-django-portal-sdk.git@master#egg=airavata-django-portal-sdk"
```

Or add the dependency to your requirements.txt file:

```
-e "git+https://github.com/apache/airavata-django-portal-sdk.git@master#egg=airavata-django-portal-sdk"
```

then run `pip install -r requirements.txt`

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

## Developing

### Setting up dev environment

```
source venv/bin/activate
pip install -r requirements-dev.txt
```

### Running tests

```
./runtests.py
```

### Running flake8

```
flake8 .
```

### Automatically formatting Python code

```
autopep8 -i -aaa -r .
isort .
```
