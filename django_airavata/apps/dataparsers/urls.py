
from django.conf.urls import url

from . import views

app_name = 'django_airavata_dataparsers'
urlpatterns = [
    url(r'^home$', views.home, name='home'),
    url(r'^parsers/(?P<parser_id>[^/]+)/$',
        views.parser_details, name="parser_details")
]
