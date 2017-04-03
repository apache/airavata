
from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect

import logging

logger = logging.getLogger(__name__)


@login_required
def projects_list(request):

    gateway_id = settings.GATEWAY_ID
    username = request.user.username

    try:
        projects = request.airavata_client.getUserProjects(request.authz_token, gateway_id, username, -1, 0)
        return render(request, 'django_airavata_workspace/projects_list.html', {
            'projects': projects
        })
    except Exception as e:
        logger.exception("Failed to load projects")
        return redirect('/')
