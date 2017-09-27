
from django.conf.urls import url

from . import views

app_name = 'django_airavata_workspace'
urlpatterns = [
    url(r'^projects$', views.projects_list, name='projects'),
    url(r'^experiments$', views.experiments_list, name='experiments'),
    url(r'^dashboard$', views.dashboard, name='dashboard'),
]