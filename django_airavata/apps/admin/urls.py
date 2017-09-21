
from django.conf.urls import url

from . import views

app_name = 'admin'
urlpatterns = [
    url(r'^$', views.experiments, name='admin'),
]