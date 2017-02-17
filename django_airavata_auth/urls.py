
from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^login$', views.login, name='airavata_auth_login'),
    url(r'^logout$', views.logout, name='airavata_auth_logout'),
    url(r'^callback', views.callback, name='airavata_auth_callback'),
]