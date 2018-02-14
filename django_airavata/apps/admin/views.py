from django.shortcuts import render

# Create your views here.

from django.contrib.auth.decorators import login_required
from django.shortcuts import render

@login_required
def admin_home(request):
    return render(request, 'admin/admin.html')

@login_required
def credential_store(request):
    return render(request, 'admin/credential_store.html')

@login_required
def compute_resource(request):
    return render(request, 'admin/compute_resource.html')
