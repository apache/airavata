
from django.conf.urls import url

from . import views

app_name = 'admin'
urlpatterns = [
    url(r'^$', views.admin_home, name='admin'),
    url(r'^credential/store$', views.credential_store, name='credential_store'),
]
