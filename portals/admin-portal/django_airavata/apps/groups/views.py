# Create your views here.

from django.contrib.auth.decorators import login_required
from django.shortcuts import render


@login_required
def groups_manage(request):
    request.active_nav_item = "manage"

    return render(request, 'django_airavata_groups/base.html', {
        'bundle_name': 'group-list'
    })


@login_required
def groups_create(request):
    request.active_nav_item = "manage"

    return render(request, 'django_airavata_groups/base.html', {
        'bundle_name': 'group-create',
        'next': request.GET.get('next'),
    })


@login_required
def edit_group(request, group_id):
    request.active_nav_item = "manage"

    return render(request, 'django_airavata_groups/group_edit.html', {
        'bundle_name': 'group-edit',
        'group_id': group_id,
        'next': request.GET.get('next'),
    })
