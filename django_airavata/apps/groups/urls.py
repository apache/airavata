
from django.conf.urls import url

from . import views

app_name = 'django_airavata_groups'
urlpatterns = [
    url(r'^$', views.groups_manage, name="manage"),
    url(r'^list/', views.groups_list, name='list'),
    url(r'^create/', views.groups_create, name='create'),
    url(r'^add/', views.add_members, name='add'),
]
