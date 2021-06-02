from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.http import HttpResponse, HttpResponseNotFound
from django.shortcuts import render

from airavata_django_portal_sdk import user_storage

# Create your views here.

@login_required
def home(request):

    # Example code: Airavata API client
    # Make calls to the Airavata API from your view, for example:
    #
    # experiments = request.airavata_client.searchExperiments(
    #        request.authz_token, settings.GATEWAY_ID, request.user.username, filters={},
    #        limit=20, offset=0)
    #
    # The authorization token is always the first argument of Airavata API calls
    # and is available as 'request.authz_token'. Some API methods require a
    # 'gatewayID' argument and that is available on the Django settings object
    # as 'settings.GATEWAY_ID'.
    # For documentation on other Airavata API methods, see
    # https://docs.airavata.org/en/master/technical-documentation/airavata-api/.
    # The Airavata Django Portal uses the Airavata Python Client SDK:
    # https://github.com/apache/airavata/tree/master/airavata-api/airavata-client-sdks/airavata-python-sdk


    # Example code: user_storage module
    # In your Django views, you can make calls to the user_storage module to manage a user's files in the gateway
    #
    # user_storage.listdir(request, "")  # lists the user's home directory
    # user_storage.open_file(request, data_product_uri=...)  # open's a file for a given data_product_uri
    # user_storage.save(request, "path/in/user/storage", file)  # save a file to a path in the user's storage
    #
    # For more information as well as other user_storage functions, see https://airavata-django-portal-sdk.readthedocs.io/en/latest/

    return render(request, "{{ cookiecutter.project_slug }}/home.html", {
        'project_name': "{{ cookiecutter.project_name }}"
    })
