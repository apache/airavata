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

We'll use a project code generation tool called
[Cookiecutter](https://cookiecutter.readthedocs.io/) to generate all of the
necessary files and configuration. These steps will show how to use
Cookiecutter. Then we'll take a tour of the generated code.

1.  Install the Airavata Django Portal if you haven't already. See the
    [https://github.com/apache/airavata-django-portal/blob/master/README.md](README)
    for instructions.
2.  With the Airavata Django Portal virtual environment activated, install
    cookiecutter.

```
pip install -U cookiecutter
```

3.  Navigate to a separate directory outside the airavata-django-portal, where
    you'll create your custom django app. Use cookiecutter to run the Airavata
    Django app template.

```
cookiecutter https://github.com/machristie/cookiecutter-airavata-django-app.git
```

You'll need to answer some questions. The project name is the most important
one. You can name it whatever you want. For these instructions I'll name it
**Custom Django App**. For the rest of the questions, you can simply accept the
defaults:

```
project_name [My Custom Django App]: Custom Django App
project_slug [custom_django_app]:
project_short_description [Custom Django app with everything needed to be installed in the airavata-django-portal]:
app_config_class_name [CustomDjangoAppConfig]:
version [0.1.0]:
```

4. The generated code is placed in a project directory. The directory has the
   same name as the project slug. In my case that is `custom_django_app`.

```
(venv) $ cd custom_django_app/
(venv) $ find .
.
./pyproject.toml
./MANIFEST.in
./README.md
./setup.py
./.gitignore
./setup.cfg
./custom_django_app
./custom_django_app/models.py
./custom_django_app/__init__.py
./custom_django_app/apps.py
./custom_django_app/admin.py
./custom_django_app/static
./custom_django_app/static/custom_django_app
./custom_django_app/static/custom_django_app/README.md
./custom_django_app/output_views
./custom_django_app/output_views/__init__.py
./custom_django_app/templates
./custom_django_app/templates/custom_django_app
./custom_django_app/templates/custom_django_app/home.html
./custom_django_app/tests.py
./custom_django_app/urls.py
./custom_django_app/views.py
```

5. To install the custom django app in your local Airavata Django Portal
   instance, use `pip` to install it.

```
pip install -e .
```

6. (Optional) If you want to use Git source control for this project, then this
   directory (the one with setup.cfg and pyproject.toml) is where you should
   initialize the Git repository.

```
$ git init
```

Now we'll take a tour of the generated files.

In the root directory are several Python project files: pyproject.toml,
MANIFEST.in, setup.py, and setup.cfg. setup.cfg is the most important of these
since it defines Python package name, version, description, dependencies, etc.
In the custom Django app module directory (custom_django_app in the example
above) are where you will find the generate Django Python and HTML code. Here
are some things you may need to update as you develop your custom Django app:

### setup.cfg

-   **name**: you can update the name, but this really only affects the name of
    the Python project when installing it from a repository like
    <https://pypi.org>.
-   **description**: this is the Python package description. Like the name,
    really only affects package repositories like <https://pypi.org>.
-   **version**: if you push your custom Django app to pypi.org, then you'll
    want to increment the version number.
-   **install_requires**: this is where you list other Python packages on which
    your custom Django app depends. As an example, see the
    [Custom UI tutorial](../tutorial/custom_ui_tutorial.md#create-the-custom-output-viewer)
    when cclib, numpy and matplotlib are added to setup.cfg for the
    GaussianEigenvaluesViewProvider.
-   **options.entry_points**: this is metadata that describes where this custom
    Django app's AppConfig class is located so that the Airavata Django Portal
    can load it. In the Django framework, the AppConfig class is the entry point
    to the app. Normally you won't need to update this, but if you decide to
    change the Django app's Python module name or the name of the AppConfig
    class, then you'll need to update these to remain consistent.

### apps.py

This file defines the custom Django app's AppConfig class. This the main
metadata for this custom Django app. Besides the normal metadata that the Django
framework expects, this also defines a display name (`verbose_name`) and an icon
(`fa_icon_class`) to use for this custom app. See
[AppConfig settings](./new_django_app.md#integrating-with-the-django-portal) for
details on available properties here. Note that `app_order` isn't supported for
custom Django apps. Only `name`, `label` and `verbose_name` are required. See
[Django project documentation on AppConfig](https://docs.djangoproject.com/en/1.11/ref/applications/#application-configuration)
for description of these properties. Additionally, custom Django apps can
specify the following properties:

-   **url_prefix**: specify the URL prefix for all of the custom Django apps
    URLs. By default, the app's `label` property is used as the URL prefix, but
    by setting `url_prefix` you can override that. (Example:
    [SimCCS](https://github.com/SciGaP/simccs-maptool/blob/dev/simccs_maptool/apps.py#L34))

### views.py

This is where you define your Django views. Views are functions or classes that
take as input an HTTP request and generate an HTTP response. See
[part 1 of the Django tutorial](https://docs.djangoproject.com/en/2.2/intro/tutorial01/)
for more information.

In the generated views.py file there is simple view function called `home` that
simply renders the `home.html` template. There is also some commented out code
that shows how to, for example,

-   make Airavata API calls with the Airavata Python SDK
-   manage the user's data storage with the Airavata Django Portal SDK

Also note that if you want to require authentication for your view, just add the
`@login_required` decorator.

### templates (home.html)

HTML templates are added to the `templates/` folder. The standard convention in
Django is to create a directory in the `templates/` folder with the name of the
Python module and put your template in there, as a way to namespace the names of
an app's templates.From the
[Django docs](https://docs.djangoproject.com/en/1.11/topics/templates/):

> It’s possible – and preferable – to organize templates in subdirectories
> inside each directory containing templates. The convention is to make a
> subdirectory for each Django app, with subdirectories within those
> subdirectories as needed.

The generated `home.html` file uses a couple of Django template features. First
is the use of named blocks that extend a base template. By extending the
`base.html` template, your custom Django app will fit into the over Airavata
Django Portal. There's a block for adding the main content, called `content`,
and there is a block for JavaScript code called `scripts` and a block for CSS
called `css`. Also, the home.html template references a context variable,
`{{ project_name }}`, a variable that is placed in the context by the `home`
view function defined in views.py. To learn more about Django templates, see
[the Django tutorial, part 3](https://docs.djangoproject.com/en/2.2/intro/tutorial03/).

`home.html` also has some commented out example code showing how to use the
JavaScript SDK. The JavaScript SDK allows you to write frontend code that
interacts with the Airavata API via the Airavata Django Portal REST API.

### urls.py

To map your view function to URLs, you add a URL pattern to `urls.py`. The
generated `urls.py` already has a mapping for the `home` view function. As you
add additional view functions, you'll want to add a mapping to a URL pattern
here.

### static files (images, CSS, JS, etc.)

Put static folders into the `static/custom_django_app` (or whatever you named
your django app) subdirectory. You can then reference a URL to one of these
static file in a template by using the `static` template tag, like so:

```html
<script src="{% static 'custom_django_app/some_script.js' %}"></script>
```

See the README.md in the `static/` subdirectory for more information.

### output_views/

This is the folder where you can define
[output view providers](./custom_output_view_provider.md). An output view
provider is a Python class that creates a visualization of an output file by
generating an image or other appropriate data from the output file.

### Viewing the custom Django app

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
    [Custom UI tutorial](../tutorial/custom_ui_tutorial.md#tutorial-exercise-create-a-custom-django-app)
-   see the
    [index.js](https://github.com/apache/airavata-django-portal/blob/master/django_airavata/apps/api/static/django_airavata_api/js/index.js)
    file in the AiravataAPI to see what models and services are provided by the
    library

### Custom Django apps

There are now several examples of custom Django apps that can be learned from:

-   <https://github.com/SciGaP/simccs-maptool>
-   <https://github.com/InterACTWEL/interactactwel-django-app>
