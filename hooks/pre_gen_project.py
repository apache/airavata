import os
import sys


# make sure setup.cfg is one directory up
cwd = os.getcwd()
django_app_dir = os.path.dirname(cwd)
if not os.path.isfile(os.path.join(django_app_dir, "setup.cfg")):
    print(f"ERROR: Could not find setup.cfg file in {django_app_dir}", file=sys.stderr)
    print(f"Are you sure you are running this cookiecutter in an Airavata Django app directory? The setup.cfg should be in the current directory.", file=sys.stderr)
    sys.exit(1)

# make sure {{cookiecutter.custom_django_app_module_name}} looks like django app directory
django_app_module_dir = os.path.join(django_app_dir, "{{cookiecutter.custom_django_app_module_name}}")
if not os.path.isfile(os.path.join(django_app_module_dir, "apps.py")):
    # Try to find the django app module
    directories = [d for d in os.listdir(django_app_dir) if os.path.isdir(os.path.join(django_app_dir, d))]
    candidate = None
    for directory in directories:
        if os.path.isfile(os.path.join(django_app_dir, directory, 'apps.py')):
            candidate = directory
            break
    print(f"ERROR: {{cookiecutter.custom_django_app_module_name}} doesn't look like a Django app module", file=sys.stderr)
    if candidate is not None:
        print(f"For custom_django_app_module_name, did you mean '{candidate}' instead of '{{cookiecutter.custom_django_app_module_name}}'?", file=sys.stderr)
    sys.exit(1)

# Make sure that there isn't an output_views isn't a file
if os.path.isfile(os.path.join(django_app_module_dir, "{{cookiecutter.output_views_directory_name}}.py")):
    print(f"ERROR: {os.path.join(django_app_module_dir, '{{cookiecutter.output_views_directory_name}}.py')} is a file. Convert to a module directory. See the README: https://github.com/machristie/cookiecutter-airavata-django-output-view.")
    sys.exit(1)
