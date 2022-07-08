
from django.conf.urls import include
from django.urls import path, re_path
from rest_framework import routers

from . import views

router = routers.DefaultRouter()
router.register(r'users', views.UserViewSet, basename='user')
router.register(r'extended-user-profile-fields', views.ExtendedUserProfileFieldViewset, basename='extended-user-profile-field')
router.register(r'extended-user-profile-values', views.ExtendedUserProfileValueViewset, basename='extended-user-profile-value')
app_name = 'django_airavata_auth'
urlpatterns = [
    re_path(r'^', include(router.urls)),
    re_path(r'^login$', views.start_login, name='login'),
    re_path(r'^login-password$', views.start_username_password_login,
            name='login_with_password'),
    re_path(r'^redirect_login/(\w+)/$', views.redirect_login,
            name='redirect_login'),
    re_path(r'^handle_login$', views.handle_login, name='handle_login'),
    re_path(r'^logout$', views.start_logout, name='logout'),
    re_path(r'^callback-error/(?P<idp_alias>\w+)/$', views.callback_error,
            name='callback-error'),
    re_path(r'^callback/$', views.callback, name='callback'),
    re_path(r'^create-account$', views.create_account, name='create_account'),
    re_path(r'^verify-email/(?P<code>[\w-]+)/$', views.verify_email,
            name="verify_email"),
    re_path(r'^resend-email-link/', views.resend_email_link,
            name="resend_email_link"),
    re_path(r'^forgot-password/$', views.forgot_password, name="forgot_password"),
    re_path(r'^reset-password/(?P<code>[\w-]+)/$', views.reset_password,
            name="reset_password"),
    re_path(r'^login-desktop/$', views.login_desktop, name="login_desktop"),
    re_path(r'^login-desktop-success/$',
            views.login_desktop_success, name="login_desktop_success"),
    re_path(r'^refreshed-token-desktop$', views.refreshed_token_desktop,
            name="refreshed_token_desktop"),
    re_path(r'^access-token-redirect$', views.access_token_redirect, name="access_token_redirect"),
    re_path(r'^user-profile/', views.user_profile, name="user_profile"),
    path('settings-local/', views.download_settings_local, name="download_settings_local"),
]
