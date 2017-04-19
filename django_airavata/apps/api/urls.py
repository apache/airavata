
from . import views

from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns

urlpatterns = [
    url(r'^projects/$', views.ProjectList.as_view()),
    url(r'^projects/(?P<project_id>[^\s]+)/$', views.ProjectDetail.as_view()),
]

urlpatterns = format_suffix_patterns(urlpatterns)