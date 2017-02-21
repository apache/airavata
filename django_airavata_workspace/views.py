
from . import utils as client_utils

from django_airavata_auth import utils

from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.shortcuts import render

import logging

logger = logging.getLogger(__name__)


@login_required
def projects_list(request):

    authz_token = utils.get_authz_token(request)
    gateway_id = settings.GATEWAY_ID
    username = request.user.username

    airavataClient = client_utils.get_airavata_client()
    try:
        projects = airavataClient.getUserProjects(authz_token, gateway_id, username, -1, 0)
        return render(request, 'django_airavata_workspace/projects_list.html', {
            'projects': projects
        })
    except Exception as e:
        logger.exception("Failed to load projects")
        return redirect('/')
