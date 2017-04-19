
from . import views

from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns

urlpatterns = [
    url(r'^$', views.api_root),
    url(r'^projects/$', views.ProjectList.as_view(), name='api_project_list'),
    url(r'^projects/(?P<project_id>[^/]+)/$', views.ProjectDetail.as_view(), name='api_project_detail'),
    url(r'^projects/(?P<project_id>[^/]+)/experiments/$', views.ProjectExperimentList.as_view(), name='api_project_experiments_list'),
    url(r'^experiments/$', views.ExperimentList.as_view(), name='api_experiment_list'),
]

urlpatterns = format_suffix_patterns(urlpatterns)