# Creating a Wagtail Export

You can create an initial set of Wagtail pages with theming and images and then
export them for loading into another Django portal instance. These can be used
to create starter themes or to fully develop a themed set of pages for an
Airavata Django Portal.

These steps document how to create one of these exports locally.

## Getting Started

1. Clone and setup the
   [Airavata Django Portal](https://github.com/apache/airavata-django-portal)
   locally. Follow the instructions in the README.md.
2. Edit your `django_airavata/settings_local.py` file and add the following at
   the bottom:

```python
AUTHENTICATION_BACKENDS = ['django.contrib.auth.backends.ModelBackend']
MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
    # Wagtail related middleware
    'wagtail.core.middleware.SiteMiddleware',
    'wagtail.contrib.redirects.middleware.RedirectMiddleware',
]
```

This allows you to log in locally, without needing to setup Keycloak or have an
Airavata backend running.

3. Make sure your virtual environment is activated if not already.

```
source venv/bin/activate
```

4. Create a superuser account. You'll use this to log into wagtail and edit
   pages:

```
python manage.py createsuperuser
```

5. (Optional) To start from an existing Wagtail export, run
   `python manage.py load_cms_data FILENAME`, where FILENAME is the name of one
   of the Wagtail exports in
   [fixtures](https://github.com/apache/airavata-django-portal/tree/master/django_airavata/wagtailapps/base/fixtures)
   directory. For example, you can run

```
python manage.py load_cms_data default.json
```

6. Start the Django server and log in at <http://localhost:8000/cms>

```
python manage.py runserver
```

## Creating the Wagtail export

Once you have the pages just the way you want them, you can now export them.

1. Make sure your virtual environment is activated if not already.

```
source venv/bin/activate
```

2. Run the following to export the Wagtail settings into a JSON file in the
   fixtures directory:

```bash
python manage.py dumpdata --natural-foreign --exclude auth.permission \
  --exclude contenttypes --indent 4 > django_airavata/wagtailapps/base/fixtures/myexport.json
```

Where you can change `myexport` to whatever you want to meaningfully name the
export file.

3. Commit any media files that were added as part of creating the Wagtail pages.

## Resetting your local environment

1. To start over, first remove (or rename) the database.

!!! warning

    db.sqlite3 stores all of the Wagtail changes you have made. Only remove
    this if you have already exported the wagtail changes to a file. See the
    previous section.

```
rm db.sqlite3
```

2. Make sure your virtual environment is activated if not already.

```
source venv/bin/activate
```

3. Migrate the database:

```
python manage.py migrate
```

4. Create a superuser account. You'll use this to log into wagtail and edit
   pages:

```
python manage.py createsuperuser
```

5. (Optional) To start from an existing Wagtail export, run
   `python manage.py load_cms_data FILENAME`, where FILENAME is the name of one
   of the Wagtail exports in
   [fixtures](https://github.com/apache/airavata-django-portal/tree/master/django_airavata/wagtailapps/base/fixtures)
   directory. For example, you can run

```
python manage.py load_cms_data default.json
```

6. Start the Django server and log in at <http://localhost:8000/cms>

```
python manage.py runserver
```

## Importing a Wagtail export

You can import a Wagtail export by running the following command on a newly
created Django instance.

1. Make sure your virtual environment is activated if not already.

```
source venv/bin/activate
```

2. Run

```bash
python manage.py load_cms_data myexport.json
```

where `myexport.json` should match the name that you gave the file when
exporting it.

## Replacing a Wagtail import with a different export

Use this when you have already loaded a Wagtail export into a Django instance
and you need to load a different one to overwrite the first one. The following
steps will first remove the Wagtail tables and then load the export like normal.

1. Make sure your virtual environment is activated if not already.

```bash
source venv/bin/activate
```

2. Run the following to delete all wagtail tables

```bash
python manage.py migrate wagtailimages 0001
python manage.py migrate wagtailimages zero
python manage.py migrate taggit zero
python manage.py migrate wagtailadmin zero
python manage.py migrate wagtailcore zero
python manage.py migrate wagtailusers zero
python manage.py migrate wagtailembeds zero
```

3. Migrate the database:

```bash
python manage.py migrate
```

4. Run

```bash
python manage.py load_cms_data myexport.json
```

where `myexport.json` should match the name that you gave the file when
exporting it.
