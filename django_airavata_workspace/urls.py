
from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^projects$', views.projects_list, name='airavata_projects_list'),
]