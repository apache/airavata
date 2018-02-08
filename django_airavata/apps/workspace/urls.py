
from django.conf.urls import url

from . import views

app_name = 'django_airavata_workspace'
urlpatterns = [
    url(r'^projects$', views.projects_list, name='projects'),
    url(r'^experiments/(?P<experiment_id>[^/]+)/$', views.view_experiment,
        name='view_experiment'),
    url(r'^experiments$', views.experiments_list, name='experiments'),
    url(r'^applications/(?P<app_module_id>[^/]+)/create_experiment$',
        views.create_experiment, name='create_experiment'),
    url(r'^dashboard$', views.dashboard, name='dashboard'),
    url(r'^upload$', views.upload_input_file, name='upload_input_file'),
]
