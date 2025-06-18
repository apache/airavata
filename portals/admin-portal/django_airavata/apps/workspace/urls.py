
from django.urls import re_path

from . import views

app_name = 'django_airavata_workspace'
urlpatterns = [
    re_path(r'^projects$', views.projects_list, name='projects'),
    re_path(r'^projects/(?P<project_id>[^/]+)/$', views.edit_project,
            name='edit_project'),
    re_path(r'^experiments/(?P<experiment_id>[^/]+)/edit$', views.edit_experiment,
            name='edit_experiment'),
    re_path(r'^experiments/(?P<experiment_id>[^/]+)/$', views.view_experiment,
            name='view_experiment'),
    re_path(r'^experiments$', views.experiments_list, name='experiments'),
    re_path(r'^applications/(?P<app_module_id>[^/]+)/create_experiment$',
            views.create_experiment, name='create_experiment'),
    re_path(r'^dashboard$', views.dashboard, name='dashboard'),
    re_path(r'^storage', views.user_storage, name='storage'),
]
