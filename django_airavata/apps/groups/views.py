# Create your views here.
from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect
from .forms import CreateForm
from django.contrib import messages
from apache.airavata.model.sharing.ttypes import UserGroup
from apache.airavata.model.sharing.ttypes import GroupCardinality
import datetime
import uuid

import logging

logger = logging.getLogger(__name__)

@login_required
def groups_manage(request):

    gateway_id = settings.GATEWAY_ID
    username = request.user.username

    try:
        group_list = request.sharing_client.getGroups(gateway_id, 0, -1)
        owner = []
        for group in group_list:
            if group.ownerId == username:
                owner.append(group)
        member = request.sharing_client.getAllMemberGroupsForUser(gateway_id, username)
        return render(request, 'django_airavata_groups/groups_manage.html', {
            'owner': owner
        }, {
            'member': member
        })
    except:
        logger.exception("Failed to load manage")
        return redirect('/')

@login_required
def groups_list(request):

    gateway_id = settings.GATEWAY_ID

    try:
        group_list = request.sharing_client.getGroups(gateway_id, 0, -1)
        for group in group_list:
            if group.groupCardinality == 0:
                group_list.remove(group)
        return render(request, 'django_airavata_groups/groups_list.html', {
            'list': group_list
        })
    except Exception as e:
        logger.exception("Failed to load groups")
        return redirect('/')

@login_required
def groups_create(request):

    gateway_id = settings.GATEWAY_ID
    username = request.user.username
    #Replace group_type assignment with user role
    group_type = 1
    group_cardinality = GroupCardinality.MULTI_USER

    if request.method == 'POST':
        form = CreateForm(request.POST, request.FILES, initial={'domain_id': gateway_id, 'group_owner': username, 'group_type': group_type, 'group_cardinality': group_cardinality})
        logger.info(form.errors)
        if form.is_valid():
            group_id = str(uuid.uuid4().hex) + form.cleaned_data.get('group_name')
            created_time = int(datetime.datetime.now().timestamp())
            updated_time = int(datetime.datetime.now().timestamp())
            group = UserGroup(groupId=group_id, domainId=gateway_id, name=form.cleaned_data.get('group_name'), description=form.cleaned_data.get('description'), ownerId=username, groupType=group_type, groupCardinality=group_cardinality, createdTime=created_time, updatedTime=updated_time)
            try:
                create = request.sharing_client.createGroup(group)
                return redirect('/')
            except Exception as e:
                logger.exception("Failed to create group")
                return redirect('/')

    else:
        form = CreateForm(initial={'domain_id': gateway_id, 'group_owner': username, 'group_type': group_type, 'group_cardinality': group_cardinality})

    return render(request, 'django_airavata_groups/groups_create.html', {
        'form': form
    })
