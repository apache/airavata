
from django.conf.urls import url

from . import views

app_name = 'django_airavata_groups'
urlpatterns = [
    url(r'^$', views.groups_list, name='groups'),
]