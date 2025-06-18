from django.urls import path, re_path

from . import views

app_name = 'django_airavata_admin'
urlpatterns = [
    re_path(r'^$', views.home, name='home'),
    re_path(r'^applications/', views.app_catalog, name='app_catalog'),
    re_path(r'^credentials/', views.credential_store, name='credential_store'),
    re_path(r'^experiment-statistics/', views.experiment_statistics,
            name="experiment-statistics"),
    re_path(r'^group-resource-profiles/', views.group_resource_profile,
            name='group_resource_profile'),
    re_path(r'^gateway-resource-profile/', views.gateway_resource_profile,
            name='gateway_resource_profile'),
    re_path(r'^notices/', views.notices, name='notices'),
    re_path(r'^users/', views.users, name='users'),
    path('extended-user-profile/', views.extended_user_profile, name="extended_user_profile"),
    path('developers/', views.developers, name='developers'),
]
