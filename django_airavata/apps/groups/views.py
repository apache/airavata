# Create your views here.
from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect
from rest_framework.renderers import JSONRenderer

from .forms import CreateForm, AddForm, RemoveForm
from django.contrib import messages
from airavata.model.sharing.ttypes import UserGroup
from airavata.model.sharing.ttypes import GroupCardinality
import datetime
import uuid

import logging

logger = logging.getLogger(__name__)


@login_required
def groups_manage(request):

    gateway_id = settings.GATEWAY_ID
    username = request.user.username

    try:
        owner_list = request.sharing_client.getGroups(gateway_id, 0, -1)
        for group in owner_list:
            if group.groupCardinality == 0:
                owner_list.remove(group)
        owner = []
        for group in owner_list:
            if group.ownerId == username:
                owner.append(group)

        member_list = request.sharing_client.getAllMemberGroupsForUser(gateway_id, username)
        member = []
        for group in member_list:
            if group.ownerId != username:
                member.append(group)

        return render(request, 'django_airavata_groups/groups_manage.html', {
            'groups_owners_data': owner
        })
       
        # return render(request, 'django_airavata_groups/groups_manage.html', {
        #     'owner': owner, 'member': member
        # })
    except:
        logger.exception("Failed to load the Manage Groups page")
        return redirect('/')

@login_required
def groups_create(request):

    gateway_id = settings.GATEWAY_ID
    username = request.user.username
    #If role = user
    group_type = 1
    #Else if role = admin
    #group_type = 2
    group_cardinality = GroupCardinality.MULTI_USER

    if request.method == 'POST':
        form = CreateForm(request.POST, request.FILES, initial={'domain_id': gateway_id, 'group_owner': username, 'group_type': group_type, 'group_cardinality': group_cardinality})
        if form.is_valid():
            group_name = form.cleaned_data.get('group_name').replace(" ","-").lower()
            group_id = group_name + str(uuid.uuid4().hex)
            group = UserGroup(groupId=group_id, domainId=gateway_id, name=form.cleaned_data.get('group_name'), description=form.cleaned_data.get('description'), ownerId=username, groupType=group_type, groupCardinality=group_cardinality, createdTime=None, updatedTime=None)
            try:
                create = request.sharing_client.createGroup(group)
                messages.success(request, 'Group '+group_id+' has been created successfully!')
                return redirect('/groups')
            except Exception as e:
                logger.exception("Failed to create the group")
                return redirect('/groups')

    else:
        form = CreateForm(initial={'domain_id': gateway_id, 'group_owner': username, 'group_type': group_type, 'group_cardinality': group_cardinality})

    return render(request, 'django_airavata_groups/groups_create.html', {
        'form': form
    })

@login_required
def view_group(request):

    gateway_id = settings.GATEWAY_ID
    group_id = request.GET.get('group_id')
    user = request.user.username

    try:
        details = request.sharing_client.getGroup(gateway_id, group_id)
        members = request.sharing_client.getGroupMembersOfTypeUser(gateway_id, group_id, 0, -1)
        c_time = datetime.datetime.fromtimestamp(details.createdTime/1000.0)
        u_time = datetime.datetime.fromtimestamp(details.updatedTime/1000.0)
        return render(request, 'django_airavata_groups/group_details.html', {
            'group': details, 'c_time': c_time, 'u_time': u_time, 'members': members, 'u_id': user
        })
    except Exception as e:
        logger.exception("Failed to load the group details")
        return redirect('/groups')

@login_required
def edit_group(request):

    gateway_id = settings.GATEWAY_ID
    group_id = request.GET.get('group_id')
    users = request.POST.getlist('users')
    members = request.POST.getlist('members')

    try:
        user_choices = request.sharing_client.getUsers(gateway_id, 0, -1)
        member_choices = request.sharing_client.getGroupMembersOfTypeUser(gateway_id, group_id, 0, -1)
        for user in user_choices:
            for member in member_choices:
                if user.userId == member.userId:
                    user_choices.remove(user)
        if request.method == 'POST':
            add_form = AddForm(request.POST, user_choices=[(user.userId, user.userId) for user in user_choices])
            remove_form = RemoveForm(request.POST, user_choices=[(member.userId, member.userId) for member in member_choices])
            if 'add' in request.POST:
                if add_form.is_valid():
                    add = request.sharing_client.addUsersToGroup(gateway_id, users, group_id)
                    messages.success(request, 'Selected members have been added successfully!')
                    return redirect('/groups')
            elif 'remove' in request.POST:
                if remove_form.is_valid():
                    remove = request.sharing_client.removeUsersFromGroup(gateway_id, members, group_id)
                    messages.success(request, 'Selected members have been removed successfully!')
                    return redirect('/groups')

        else:
            add_form = AddForm(user_choices=[(user.userId, user.userId) for user in user_choices])
            remove_form = RemoveForm(user_choices=[(member.userId, member.userId) for member in member_choices])
            group_details = request.sharing_client.getGroup(gateway_id, group_id)

    except Exception as e:
        logger.exception("Failed to edit the group")
        return redirect('/groups')

    return render(request, 'django_airavata_groups/group_edit.html', {
        'group_name': group_details.name, 'add_form': add_form, 'remove_form': remove_form
    })

@login_required
def delete_group(request):

    gateway_id = settings.GATEWAY_ID
    group_id = request.GET.get('group_id')

    try:
        delete = request.sharing_client.deleteGroup(gateway_id, group_id)
        messages.success(request, 'Group '+group_id+' has been deleted successfully!')
        return redirect('/groups')
    except Exception as e:
        logger.exception("Failed to delete the group")
        return redirect('/groups')

@login_required
def leave_group(request):

    gateway_id = settings.GATEWAY_ID
    group_id = request.GET.get('group_id')
    user = request.user.username

    try:
        leave = request.sharing_client.removeUsersFromGroup(gateway_id, [user], group_id)
        messages.success(request, 'You are no longer a member of '+group_id+'.')
        return redirect('/groups')
    except Exception as e:
        logger.exception("Failed to leave the group")
        return redirect('/groups')
