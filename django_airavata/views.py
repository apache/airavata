from django.shortcuts import render


def home(request):
    return render(request, 'django_airavata/home.html', {})
