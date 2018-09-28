
from django.conf.urls import url

from . import views

app_name = 'django_airavata_auth'
urlpatterns = [
    url(r'^login$', views.start_login, name='login'),
    url(r'^redirect_login/(\w+)/$', views.redirect_login, name='redirect_login'),
    url(r'^handle_login$', views.handle_login, name='handle_login'),
    url(r'^logout$', views.start_logout, name='logout'),
    url(r'^callback', views.callback, name='callback'),
    url(r'^error', views.auth_error, name='error'),
]
