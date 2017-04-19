
from . import views

from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns

urlpatterns = [
    url(r'^projects/$', views.project_list),
    url(r'^projects/(?P<project_id>[^\s]+)/$', views.project_detail),
]

urlpatterns = format_suffix_patterns(urlpatterns)