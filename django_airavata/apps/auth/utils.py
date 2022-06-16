"""Auth utilities."""

import time

from airavata.model.security.ttypes import AuthzToken
from django.conf import settings
from django.contrib.auth import authenticate
from django.core.mail import EmailMessage
from django.http.request import split_domain_port
from django.template import Context, Template
from oauthlib.oauth2 import BackendApplicationClient
from requests_oauthlib import OAuth2Session

from . import models


def get_authz_token(request, user=None, access_token=None):
    """Construct AuthzToken instance from session; refresh token if needed."""
    if access_token is not None:
        return _create_authz_token(request, user=user, access_token=access_token)
    elif is_request_access_token(request):
        return _create_authz_token(request, user=user)
    elif is_session_access_token(request) and not is_session_access_token_expired(request, user=user):
        return _create_authz_token(request, user=user, access_token=access_token)
    elif not is_refresh_token_expired(request):
        # Have backend reauthenticate the user with the refresh token
        user = authenticate(request)
        if user:
            return _create_authz_token(request, user=user)
    return None


def get_service_account_authz_token():
    client_id = settings.KEYCLOAK_CLIENT_ID
    client_secret = settings.KEYCLOAK_CLIENT_SECRET
    token_url = settings.KEYCLOAK_TOKEN_URL
    verify_ssl = settings.KEYCLOAK_VERIFY_SSL

    client = BackendApplicationClient(client_id=client_id)
    oauth = OAuth2Session(client=client)
    verify = verify_ssl
    if verify_ssl and hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
        verify = settings.KEYCLOAK_CA_CERTFILE
    token = oauth.fetch_token(
        token_url=token_url,
        client_id=client_id,
        client_secret=client_secret,
        verify=verify)

    access_token = token.get('access_token')
    return AuthzToken(
        accessToken=access_token,
        # This is a service account, so leaving out userName for now
        claimsMap={'gatewayID': settings.GATEWAY_ID})


def _create_authz_token(request, user=None, access_token=None):
    if access_token is None:
        access_token = _get_access_token(request)
    if user is None:
        user = request.user
    username = user.username
    gateway_id = settings.GATEWAY_ID
    return AuthzToken(accessToken=access_token,
                      claimsMap={'gatewayID': gateway_id,
                                 'userName': username})


def _get_access_token_source(request):
    if hasattr(request, 'auth') and request.auth is not None:
        return 'request'
    elif 'ACCESS_TOKEN' in request.session:
        return 'session'
    else:
        return None


def _get_access_token(request):
    source = _get_access_token_source(request)
    if source == 'request':
        return request.auth
    elif source == 'session':
        return request.session['ACCESS_TOKEN']
    else:
        return None


def is_session_access_token(request):
    """Return True if access token is stored in the user's session."""
    return _get_access_token_source(request) == 'session'


def is_request_access_token(request):
    """Return True if access token passed in request, e.g., a Bearer token."""
    return _get_access_token_source(request) == 'request'


def is_session_access_token_expired(request, user=None):
    """Return True if session access_token is not available or is expired."""
    user = user if user is not None else request.user
    now = time.time()
    return not user.is_authenticated \
        or 'ACCESS_TOKEN' not in request.session \
        or 'ACCESS_TOKEN_EXPIRES_AT' not in request.session \
        or request.session['ACCESS_TOKEN_EXPIRES_AT'] < now


def is_refresh_token_expired(request):
    """Return True if refresh_token is not available or is expired."""
    now = time.time()
    return 'REFRESH_TOKEN' not in request.session \
        or 'REFRESH_TOKEN_EXPIRES_AT' not in request.session \
        or request.session['REFRESH_TOKEN_EXPIRES_AT'] < now


def send_new_user_email(request, username, email, first_name, last_name):
    """Send new user email notification to portal admins."""
    new_user_email_template = models.EmailTemplate.objects.get(
        pk=models.NEW_USER_EMAIL_TEMPLATE)
    domain, port = split_domain_port(request.get_host())
    context = Context({
        "username": username,
        "email": email,
        "first_name": first_name,
        "last_name": last_name,
        "portal_title": settings.PORTAL_TITLE,
        "gateway_id": settings.GATEWAY_ID,
        "http_host": domain,
    })
    subject = Template(new_user_email_template.subject).render(context)
    body = Template(new_user_email_template.body).render(context)
    send_email_to_admins(subject, body)


def send_admin_alert_about_uninitialized_username(request, username, email, first_name, last_name):
    domain, port = split_domain_port(request.get_host())
    context = Context({
        "username": username,
        "email": email,
        "first_name": first_name,
        "last_name": last_name,
        "portal_title": settings.PORTAL_TITLE,
        "gateway_id": settings.GATEWAY_ID,
        "http_host": domain,
    })
    subject = Template("Please fix username: a user of {{portal_title}} ({{http_host}}) has been assigned an auto-generated username ({{username}})").render(context)
    body = Template("""
    <p>
    Dear Admin,
    </p>

    <p>
    The following user has an auto-generated username because the system could
    not determine a proper username:
    </p>

    <p>Username: {{username}}</p>
    <p>Name: {{first_name}} {{last_name}}</p>
    <p>Email: {{email}}</p>

    <p>
    This likely happened because there was no appropriate user attribute
    (typically email address) to use for the user's username when the user
    logged in through an external identity provider.  Please update the username
    to the user's email address or some other appropriate value in the <a
    href="https://{{http_host}}/admin/users/">Manage Users</a> view in the
    portal.
    </p>
    """.strip()).render(context)
    send_email_to_admins(subject, body)


def send_admin_user_completed_profile(request, user_profile):
    domain, port = split_domain_port(request.get_host())
    user = user_profile.user
    extended_profile_values = user_profile.extended_profile_values.filter(
        ext_user_profile_field__deleted=False).order_by("ext_user_profile_field__order").all()
    context = Context({
        "username": user.username,
        "email": user.email,
        "first_name": user.first_name,
        "last_name": user.last_name,
        "portal_title": settings.PORTAL_TITLE,
        "gateway_id": settings.GATEWAY_ID,
        "http_host": domain,
        "extended_profile_values": extended_profile_values
    })

    user_profile_completed_template = models.EmailTemplate.objects.get(
        pk=models.USER_PROFILE_COMPLETED_TEMPLATE)
    subject = Template(user_profile_completed_template.subject).render(context)
    body = Template(user_profile_completed_template.body).render(context)
    send_email_to_admins(subject, body)


def send_email_to_admins(subject, body):
    msg = EmailMessage(subject=subject,
                       body=body,
                       from_email=f'"{settings.PORTAL_TITLE}" <{settings.SERVER_EMAIL}>',
                       to=[f'"{a[0]}" <{a[1]}>' for a in getattr(settings,
                                                                 'PORTAL_ADMINS',
                                                                 settings.ADMINS)])
    msg.content_subtype = 'html'
    msg.send()


def send_email_to_user(template_id, context):
    email_template = models.EmailTemplate.objects.get(pk=template_id)
    subject = Template(email_template.subject).render(context)
    body = Template(email_template.body).render(context)
    msg = EmailMessage(
        subject=subject,
        body=body,
        from_email="\"{}\" <{}>".format(settings.PORTAL_TITLE,
                                        settings.SERVER_EMAIL),
        to=["\"{} {}\" <{}>".format(context['first_name'],
                                    context['last_name'],
                                    context['email'])],
        reply_to=[f"\"{a[0]}\" <{a[1]}>" for a in getattr(settings,
                                                          'PORTAL_ADMINS',
                                                          settings.ADMINS)]
    )
    msg.content_subtype = 'html'
    msg.send()
