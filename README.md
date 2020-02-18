# Apache Airavata Django Portal

[![Build Status](https://travis-ci.org/apache/airavata-django-portal.svg?branch=master)](https://travis-ci.org/apache/airavata-django-portal)
[![Build Status](https://readthedocs.org/projects/apache-airavata-django-portal/badge/?version=latest)](https://apache-airavata-django-portal.readthedocs.io/en/latest/?badge=latest)

The Airavata Django Portal is a web interface to the
[Apache Airavata](http://airavata.apache.org/) API implemented using the Django
web framework. The intention is that the Airavata Django Portal can be used as
is for a full featured web based science gateway but it can also be customized
through various plugins to add more domain specific functionality as needed.

## Getting Started

The following steps will help you quickly get started with running the Airavata
Django Portal locally. This will allow you to try it out and can also be used as
a development environment. If you just want to run the Airavata Django Portal
locally, see the Docker instructions below for a more simplified approach.

The Airavata Django Portal is developed with Python 3.6 but should also work
with 3.4 and 3.5. You'll need one of these versions installed locally.

You'll also need Node.js and yarn to build the JavaScript frontend code. Please
install
[the most recent LTS version of Node.js](https://nodejs.org/en/download/) and
[the Yarn package manager](https://yarnpkg.com).

1.  Checkout this project and create a virtual environment.

    ```
    git clone https://github.com/apache/airavata-django-portal.git
    cd airavata-django-portal
    python3 -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt
    ```

    - **macOS note**: to install the MySQL dependencies you need to have the
      MySQL development headers and libraries installed. Also, on macOS you need
      to have openssl installed. See the
      [mysqlclient-python installation notes](https://github.com/PyMySQL/mysqlclient-python#install)
      for more details.

2.  Create a local settings file. Copy
    `django_airavata/settings_local.py.sample` to
    `django_airavata/settings_local.py` and edit the contents to match your
    Keycloak and Airavata server deployments.

    ```
    cp django_airavata/settings_local.py.sample django_airavata/settings_local.py
    ```

3.  Run Django migrations

    ```
    python manage.py migrate
    ```

4.  Build the JavaScript sources. There are a few JavaScript packages in the
    source tree, colocated with the Django apps in which they are used. The
    `build_js.sh` script will build them all.

    ```
    ./build_js.sh
    ```

5.  Load the default Wagtail CMS pages.

    ```
    python manage.py load_default_gateway
    ```

6.  Run the server

    ```
    python manage.py runserver
    ```

    - Note: if you want to use OpenID Connect authentication from the Django
      Portal when running it locally, you'll need to first set the following
      environment to allow OAuth over insecure HTTP:

          ```
          export OAUTHLIB_INSECURE_TRANSPORT=1
          ```

7.  Point your browser to http://localhost:8000.

## Docker instructions

To run the Django Portal as a Docker container, you need a `settings_local.py`
file which you can create from the `settings_local.py.sample` file. Then run the
following:

1. Build the Docker image.

   ```
   docker build -t airavata-django-portal .
   ```

2. Run the Docker container.

   ```
   docker run -d \
     -v /path/to/my/settings_local.py:/code/django_airavata/settings_local.py \
     -p 8000:8000 airavata-django-portal
   ```

3. Load an initial set of Wagtail pages (theme). You only need to do this when
   starting the container for the first time.

   ```
   docker exec CONTAINER_ID python manage.py load_cms_data default
   ```

4. Point your browser to http://localhost:8000.

## Documentation

Documentation currently is available at
https://apache-airavata-django-portal.readthedocs.io/en/latest/ (built from the
'docs' directory).

To build the documentation locally, first
[set up a development environment](#setting-up-development-environment), then
run the following in the root of the project:

```
mkdocs serve
```

## Feedback

Please send feedback to the mailing list at <dev@airavata.apache.org>. If you
encounter bugs or would like to request a new feature you can do so in the
[Airavata Jira project](https://issues.apache.org/jira/projects/AIRAVATA) (just
select the _Django Portal_ component when you make your issue).

## Contributing

For general information on how to contribute, please see the
[Get Involved](http://airavata.apache.org/get-involved.html) section of the
Apache Airavata website.

### Setting up development environment

Run `pip install -r requirements-dev.txt` to install development and testing
libraries.

Use a code editor that integrates with editorconfig and flake8. I also recommend
autopep8 for automatically formatting code to follow the PEP8 guidelines.

## License

The Apache Airavata Django Portal is licensed under the Apache 2.0 license. For
more information see the [LICENSE](LICENSE) file.
