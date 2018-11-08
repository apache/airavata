from django.contrib.auth.decorators import login_required
from django.shortcuts import redirect, render
from django.urls import reverse


@login_required
def home(request):
    return redirect(reverse('django_airavata_admin:app_catalog'))


@login_required
def app_catalog(request):
    request.active_nav_item = 'app_catalog'
    return render(request, 'admin/admin_base.html')


@login_required
def credential_store(request):
    request.active_nav_item = 'credential_store'
    return render(request, 'admin/admin_base.html')


@login_required
def compute_resource(request):
    return render(request, 'admin/compute_resource.html')


@login_required
def group_resource_profile(request):
    request.active_nav_item = 'group_resource_profile'
    return render(request, 'admin/admin_base.html')


@login_required
def gateway_resource_profile(request):
    request.active_nav_item = 'gateway_resource_profile'
    return render(request, 'admin/admin_base.html')
