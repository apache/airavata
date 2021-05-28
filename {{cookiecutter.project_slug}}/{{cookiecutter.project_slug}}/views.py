from django.contrib.auth.decorators import login_required
from django.http import HttpResponse, HttpResponseNotFound
from django.shortcuts import render

from airavata_django_portal_sdk import user_storage

# Create your views here.

@login_required
def home(request):

    # In your Django views, you can make calls to the user_storage module to manage a user's files in the gateway
    # user_storage.listdir(request, "")  # lists the user's home directory
    # user_storage.open_file(request, data_product_uri=...)  # open's a file for a given data_product_uri
    # user_storage.save(request, "path/in/user/storage", file)  # save a file to a path in the user's storage
    # For more information as well as other user_storage functions, see https://airavata-django-portal-sdk.readthedocs.io/en/latest/

    return render(request, "{{ cookiecutter.project_slug }}/home.html", {
        'project_name': "{{ cookiecutter.project_name }}"
    })
