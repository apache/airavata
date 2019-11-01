
from django.conf.urls import url

from . import views

app_name = 'django_airavata_auth'
urlpatterns = [
    url(r'^login$', views.start_login, name='login'),
    url(r'^login-password$', views.start_username_password_login,
        name='login_with_password'),
    url(r'^redirect_login/(\w+)/$', views.redirect_login,
        name='redirect_login'),
    url(r'^handle_login$', views.handle_login, name='handle_login'),
    url(r'^logout$', views.start_logout, name='logout'),
    url(r'^callback-error/(?P<idp_alias>\w+)/$', views.callback_error,
        name='callback-error'),
    url(r'^callback/$', views.callback, name='callback'),
    url(r'^create-account$', views.create_account, name='create_account'),
    url(r'^verify-email/(?P<code>[\w-]+)/$', views.verify_email,
        name="verify_email"),
    url(r'^resend-email-link/', views.resend_email_link,
        name="resend_email_link"),
    url(r'^forgot-password/$', views.forgot_password, name="forgot_password"),
    url(r'^reset-password/(?P<code>[\w-]+)/$', views.reset_password,
        name="reset_password"),
    url(r'^login-desktop/$', views.login_desktop, name="login_desktop"),
    url(r'^login-desktop-success/$',
        views.login_desktop_success, name="login_desktop_success"),
    url(r'^refreshed-token-desktop$', views.refreshed_token_desktop,
        name="refreshed_token_desktop"),
]
