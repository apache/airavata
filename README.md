# Airavata Django Portal Output View Provider Cookiecutter

## Quickstart

Install the latest Cookiecutter if you haven't installed it yet:

    pip install --user -U cookiecutter

This cookiecutter assumes that you are running it inside the root directory of a
custom Airavata Django app. If you don't yet have a custom Airavata Django app,
there is a cookiecutter you can use to create one:
https://github.com/machristie/cookiecutter-airavata-django-app.

Once you have your custom Airavata Django app, go into the root directory and
run this cookiecutter there. For example, if you created an Airavata Django app
with the name `test_django_app`, then change to that directory (this is the
directory that contains the `setup.cfg` file)

    cd path/to/test_django_app

Then run the following:

    cookiecutter https://github.com/machristie/cookiecutter-airavata-django-output-view.git -f

`-f` is needed because `output_views/` directory should already exist.

When prompted for the name of the `custom_django_app_module_name`, give it the
name of your Airavata Django app. Keeping with the example, you would supply
`test_django_app`.

## Converting output_views.py to a module directory

If your Django app has an output_views.py file you'll get an error since this
cookiecutter assumes that `output_views` is a module directory. To convert it to
module directory:

1. Create an `output_views/` directory in the same directory where your
   output_views.py file lives.
2. Either, rename your `output_views.py` file to `output_views/__init__.py`
3. Or, create an empty `output_views/__init__.py` file and move/rename as
   appropriate your `output_views.py` file to a file in the `output_views/`
   directory. You'll need to update the entry_points in setup.cfg.
