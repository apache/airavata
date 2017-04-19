
from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^projects/$', views.project_list),
    url(r'^projects/(?P<project_id>[^\s]+)/$', views.project_detail),
]