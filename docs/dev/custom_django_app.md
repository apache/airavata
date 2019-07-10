# Custom Django Apps

The functionality of the Airavata Django Portal can be extended by adding custom
Django apps that can provide completely custom user interfaces while leveraging
the Airavata Django Portal REST API. This page documents how to get started with
creating a custom Django app that works with the Airavata Django Portal and how
to make use of functionality provided.

## Getting Started

Creating a custom Django app requires creating an installable Python package
with metadata describing that it is a Django app meant to be automatically
installed into the Airavata Django Portal when it is loaded into the virtual
environment.

We'll go through the minimal setup code needed. This will follow along with the
code in the [https://github.com/machristie/test-dynamic-djangoapp]() repo, which
represents a minimal custom Django app for the Django Portal.

1.  Install the Airavata Django Portal. See the
    [https://github.com/apache/airavata-django-portal/blob/master/README.md](README)
    for instructions.
2.  With the Django Portal virtual environment activated, navigate to a separate
    directory outside the airavata-django-portal, where you'll create your
    custom django app. In that directory, run
    `django-admin startapp my_custom_app`, but instead of _my_custom_app_
    specify the module name you want to use. For example, let's say your
    directory is called `custom-django-app` and you want to call the module name
    `custom_app`. Then you would run

        cd ../custom-django-app
        django-admin startapp custom_app

    This will result in the following files:

        custom-django-app
        custom-django-app/custom_app
        custom-django-app/custom_app/migrations
        custom-django-app/custom_app/migrations/__init__.py
        custom-django-app/custom_app/models.py
        custom-django-app/custom_app/__init__.py
        custom-django-app/custom_app/apps.py
        custom-django-app/custom_app/admin.py
        custom-django-app/custom_app/tests.py
        custom-django-app/custom_app/views.py

3.  Create a `setup.py` file in your custom apps root directory. In the example
    above that would be in the `custom-django-app/` directory.

        import setuptools

        setuptools.setup(
            name="my-custom-django-app",
            version="0.0.1",
            description="... description ...",
            packages=setuptools.find_packages(),
            install_requires=[
                'django>=1.11.16'
            ],
            entry_points="""
        [airavata.djangoapp]
        custom_app = custom_app.apps:CustomAppConfig
        """,
        )

    Change the `name` and `description` as appropriate. The necessary metadata
    for letting the Airavata Django Portal know that this Python package is a
    custom Django app is specified in the `[airavata.djangoapp]` section.

4.  To be completed ...
