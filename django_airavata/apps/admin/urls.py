from django.conf.urls import url

from . import views

app_name = 'django_airavata_admin'
urlpatterns = [
    url(r'^$', views.app_catalog, name='home'),
    url(r'^app_catalog/$', views.app_catalog, name='app_catalog'),
    url(r'^credential_store$', views.credential_store, name='credential_store'),
    url(r'^group_resource_profile$', views.group_resource_profile, name='group_resource_profile'),
]
