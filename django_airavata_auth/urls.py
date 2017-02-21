
from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^login$', views.start_login, name='airavata_auth_login'),
    url(r'^logout$', views.start_logout, name='airavata_auth_logout'),
    url(r'^callback', views.callback, name='airavata_auth_callback'),
    url(r'^error', views.auth_error, name='airavata_auth_error'),
]