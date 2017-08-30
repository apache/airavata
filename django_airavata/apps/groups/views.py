# Create your views here.
from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect

import logging

logger = logging.getLogger(__name__)


@login_required
def groups_list(request):

    gateway_id = settings.GATEWAY_ID

    try:
        groups = request.sharing_client.getGroups(gateway_id, 0, -1)
        return render(request, 'django_airavata_groups/groups_list.html', {
            'groups': groups
        })
    except Exception as e:
        logger.exception("Failed to load groups")
        return redirect('/')
