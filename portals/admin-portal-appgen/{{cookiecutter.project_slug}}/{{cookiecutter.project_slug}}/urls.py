
from django.urls import path

from . import views

app_name = '{{ cookiecutter.project_slug }}'
urlpatterns = [
    path('home/', views.home, name='home'),
]
