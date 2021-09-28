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
airavata-django-portal-sdk
```

to the `install_requires` list in your setup.cfg or setup.py file. Then
reinstall the Django app with

```
pip install -e .
```

(see your Airavata Django custom app's README for details)

You can also just install the library with:

```
pip install airavata-django-portal-sdk
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

### Making a new release

1. Update the version in setup.py
2. Tag the repo with the same version, with the format `v${version_number}`. For
   example, if the version number in setup.py is "1.2" then tag the repo with
   "v1.2".

   ```
   git tag -m VERSION VERSION git push --follow-tags
   ```

3. In a clean checkout

   ```
   cd /tmp/
   git clone /path/to/airavata-django-portal-sdk/ -b VERSION
   cd airavata-django-portal-sdk
   python3 -m venv venv
   source venv/bin/activate
   python3 -m pip install --upgrade build
   python3 -m build
   ```

4. Push to pypi.org. Optionally can push to test.pypi.org. See
   <https://packaging.python.org/tutorials/packaging-projects/> for more info.

   ```
   python3 -m pip install --upgrade twine
   python3 -m twine upload dist/*
   ```
