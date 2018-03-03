
from django.conf.urls import url

from . import views

app_name = 'django_airavata_groups'
urlpatterns = [
    url(r'^$', views.groups_manage, name="manage"),
    url(r'^create/', views.groups_create, name='create'),
    url(r'^view/', views.view_group, name='view'),
    url(r'^edit/(?P<group_id>[^/]+)/$', views.edit_group, name='edit'),
    # url(r'^delete/', views.delete_group, name='delete'),
    url(r'^leave/', views.leave_group, name='leave'),
]
