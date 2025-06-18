
from django.urls import re_path

from . import views

app_name = 'django_airavata_dataparsers'
urlpatterns = [
    re_path(r'^$', views.home, name='home'),
    re_path(r'^parsers/(?P<parser_id>[^/]+)/$',
            views.parser_details, name="parser_details"),
    re_path(r'^edit/(?P<parser_id>[^/]+)/$',
            views.edit_parser, name='edit_parser'),

]
