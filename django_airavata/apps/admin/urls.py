from django.conf.urls import url

from . import views

app_name = 'django_airavata_admin'
urlpatterns = [
    url(r'^$', views.home, name='home'),
    url(r'^applications/', views.app_catalog, name='app_catalog'),
    url(r'^credentials/', views.credential_store, name='credential_store'),
    url(r'^experiment-statistics/', views.experiment_statistics,
        name="experiment-statistics"),
    url(r'^group-resource-profiles/', views.group_resource_profile,
        name='group_resource_profile'),
    url(r'^gateway-resource-profile/', views.gateway_resource_profile,
        name='gateway_resource_profile'),
    url(r'^notices/', views.notices, name='notices'),
    url(r'^users/', views.users, name='users'),
]
