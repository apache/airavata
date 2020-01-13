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
code in the <https://github.com/machristie/test-dynamic-djangoapp> repo, which
represents a minimal custom Django app for the Django Portal.

1.  Install the Airavata Django Portal. See the
    [https://github.com/apache/airavata-django-portal/blob/master/README.md](README)
    for instructions.
2.  With the Django Portal virtual environment activated, navigate to a separate
    directory outside the airavata-django-portal, where you'll create your
    custom django app. The following instructions wil assume this directory is
    `$HOME/custom-django-app` but it could be called and placed anywhere. In
    `$HOME/custom-django-app`, run `django-admin startapp my_custom_app`, but
    instead of _my_custom_app_ specify the module name you want to use. For
    example, let's say your directory is called `custom-django-app` and you want
    to call the module name `custom_app`. Then you would run

        cd $HOME/custom-django-app
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
    above that would be in the `$HOME/custom-django-app/` directory.

```python
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
```

Change the `name` and `description` as appropriate. The necessary metadata for
letting the Airavata Django Portal know that this Python package is a custom
Django app is specified in the `[airavata.djangoapp]` section.

4.  Create the CustomAppConfig class that is referenced above. Open
    `$HOME/custom-django-app/custom_app/apps.py` and edit to match the
    following:

```python
from django.apps import AppConfig


class CustomAppConfig(AppConfig):
    name = 'custom_app'
    label = name
    verbose_name = 'My Custom App'
    fa_icon_class = 'fa-comment'
```

This the main metadata for this custom Django app. Besides the normal metadata
that the Django framework expects, this also defines a display name
(`verbose_name`) and an icon (`fa_icon_class`) to use for this custom app. See
[AppConfig settings](./new_django_app.md#integrating-with-the-django-portal) for
details on available properties here. Note that `app_order` isn't supported for
custom Django apps. Only `name`, `label` and `verbose_name` are required. See
[Django project documentation on AppConfig](https://docs.djangoproject.com/en/1.11/ref/applications/#application-configuration)
for description of these properties.

5.  Create a simple template based view in
    `$HOME/custom-django-app/custom_app/views.py`.

```python
from django.shortcuts import render
from django.contrib.auth.decorators import login_required

# Create your views here.

@login_required
def hello_world(request):
    return render(request, "custom_app/hello.html")
```

This view will render the template `custom_app/hello.html` which we'll create in
the next step. Add the `@login_required` decorator to any views that should be
authenticated.

6.  Create the hello.html template in
    `$HOME/custom-django-app/custom_app/templates/custom_app/hello.html`

```django
{% extends 'base.html' %}
{% block content %}
<div class="main-content-wrapper">
    <main class="main-content">
        <div class="container-fluid">
            <h1>Hello World</h1>
        </div>
    </main>
</div>
{% endblock content %}
```

!!! note

    Notice that we created a directory for our templates in `custom_app`
    called `templates/custom_app/hello.html`. That might look like
    redundancy, but it is intentional. The convention in Django is to create
    a separately named directory under `templates` for each Django app. From
    the [Django docs](https://docs.djangoproject.com/en/1.11/topics/templates/):

    > It’s possible – and preferable – to organize templates in
    > subdirectories inside each directory containing templates. The convention
    > is to make a subdirectory for each Django app, with subdirectories within
    > those subdirectories as needed.

7.  Create a url mapping for this view in
    `$HOME/custom-django-app/custom_app/urls.py`.

```python
from django.conf.urls import url, include

from . import views

app_name = 'custom_app'
urlpatterns = [
    url(r'^hello/', views.hello_world, name="home"),
]
```

Note that
[`app_name` specifies the namespace](https://docs.djangoproject.com/en/1.11/topics/http/urls/#url-namespaces-and-included-urlconfs)
for your app's urls and should be changed to something appropriate for your app.

8. That defines a basic Hello World Django app. To preview and develop this
   Django app further we need to install it into a locally running Django
   portal. See
   [the README.md](https://github.com/apache/airavata-django-portal/blob/master/README.md)
   for notes on getting the Django portal installed locally. Let's assume you
   install the Django portal locally in `$HOME/airavata-django-portal`. To
   install the custom Django app in that portal you would:

```bash
# First activate the Django portal's virtual environment
cd $HOME/airavata-django-portal
source venv/bin/activate
# Then change to the custom app and install it in develop mode
cd $HOME/custom-django-app
python setup.py develop
```

Now when you log into the Django portal at <http://localhost:8000> you should
see the custom app in the dropdown menu in the top of the page (the one that
defaults to **Workspace** when you login).

## Next Steps

### AiravataAPI JS library

To use the `airavata-api.js` JavaScript library to call the Django portal REST
API (which in turn calls the Airavata API), you can include it in your templates
in the `scripts` block:

```django
{% block scripts %}
<script src="{% static 'django_airavata_api/dist/airavata-api.js' %}"></script>
<script>
    const { models, services, session, utils } = AiravataAPI;

    // Your code here ...
</script>
{% endblock scripts %}
```

For more information on the AiravataAPI library:

-   see the
    [Gateways 2019 tutorial](../tutorial/gateways2019_tutorial.md#tutorial-exercise-create-a-custom-django-app)
-   see the
    [index.js](https://github.com/apache/airavata-django-portal/blob/master/django_airavata/apps/api/static/django_airavata_api/js/index.js)
    file in the AiravataAPI to see what models and services are provided by the
    library

### Custom Django apps

There are now several examples of custom Django apps that can be learned from:

-   <https://github.com/SciGaP/simccs-maptool>
-   <https://github.com/InterACTWEL/interactactwel-django-app>
