
from django.http import HttpResponse
from django.shortcuts import render, redirect

import logging


def home(request):
    return render(request, 'django_airavata/home.html', {})
