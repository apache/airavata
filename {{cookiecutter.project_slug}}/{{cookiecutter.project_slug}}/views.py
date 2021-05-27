from django.contrib.auth.decorators import login_required
from django.http import HttpResponse, HttpResponseNotFound
from django.shortcuts import render

# Create your views here.

@login_required
def home(request):
    return render(request, "{{ cookiecutter.project_slug }}/home.html", {
        'project_name': "{{ cookiecutter.project_name }}"
    })
