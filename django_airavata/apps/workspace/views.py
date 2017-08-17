
from django_airavata.apps.api.views import ProjectList
from rest_framework.renderers import JSONRenderer
from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect

import logging

logger = logging.getLogger(__name__)


@login_required
def projects_list(request):

    response = ProjectList().get(request)
    projects_json = JSONRenderer().render(response.data)

    return render(request, 'django_airavata_workspace/projects_list.html', {
        'projects_data': projects_json
    })
