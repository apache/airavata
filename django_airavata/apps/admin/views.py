from django.shortcuts import render

# Create your views here.

from django.contrib.auth.decorators import login_required
from django.shortcuts import render

@login_required
def app_catalog(request):
    request.active_nav_item = 'app_catalog'
    return render(request, 'admin/app_catalog.html')

@login_required
def credential_store(request):
    request.active_nav_item = 'credential_store'
    return render(request, 'admin/credential_store.html')

@login_required
def compute_resource(request):
    return render(request, 'admin/compute_resource.html')

@login_required
def group_resource_profile(request):
    request.active_nav_item = 'group_resource_profile'
    return render(request, 'admin/group_resource_profile.html')
