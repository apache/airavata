
from django.urls import re_path

from . import views

app_name = 'django_airavata_groups'
urlpatterns = [
    re_path(r'^$', views.groups_manage, name="manage"),
    re_path(r'^create/', views.groups_create, name='create'),
    re_path(r'^edit/(?P<group_id>[^/]+)/$', views.edit_group, name='edit'),
]
