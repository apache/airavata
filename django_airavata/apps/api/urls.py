
from . import views

from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns

urlpatterns = [
    url(r'^$', views.api_root),
    url(r'^projects/$', views.ProjectList.as_view(), name='api_project_list'),
    # More specific, longer URLs should come before less specific, shorter ones
    # since the regular expression for project_id allows any character, even '/'
    url(r'^projects/(?P<project_id>.+)/experiments/$', views.ProjectExperimentList.as_view(), name='api_project_experiments_list'),
    url(r'^projects/(?P<project_id>.+)/$', views.ProjectDetail.as_view(), name='api_project_detail'),
    url(r'^experiments/$', views.ExperimentList.as_view(), name='api_experiment_list'),
]

urlpatterns = format_suffix_patterns(urlpatterns)