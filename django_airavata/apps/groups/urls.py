
from django.conf.urls import url

from . import views

app_name = 'django_airavata_groups'
urlpatterns = [
    url(r'^$', views.groups_manage, name="manage"),
    url(r'^create/', views.groups_create, name='create'),
    url(r'^edit/(?P<group_id>[^/]+)/$', views.edit_group, name='edit'),
]
