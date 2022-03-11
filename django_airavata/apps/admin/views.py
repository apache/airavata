from django.contrib.auth.decorators import login_required
from django.shortcuts import redirect, render
from django.urls import reverse


@login_required
def home(request):
    if request.is_gateway_admin or request.is_read_only_gateway_admin:
        return redirect(reverse('django_airavata_admin:app_catalog'))
    else:
        return redirect(
            reverse('django_airavata_admin:group_resource_profile'))


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


@login_required
def notices(request):
    request.active_nav_item = 'notices'
    return render(request, 'admin/admin_base.html')


@login_required
def users(request):
    request.active_nav_item = 'users'
    return render(request, 'admin/admin_base.html')


@login_required
def extended_user_profile(request):
    request.active_nav_item = 'users'
    return render(request, 'admin/admin_base.html')


@login_required
def experiment_statistics(request):
    request.active_nav_item = 'experiment-statistics'
    return render(request, 'admin/admin_base.html')


@login_required
def developers(request):
    request.active_nav_item = 'developers'
    return render(request, 'admin/admin_base.html')
