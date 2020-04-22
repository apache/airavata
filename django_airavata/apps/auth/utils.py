"""Auth utilities."""

import time

from django.conf import settings
from django.contrib.auth import authenticate
from django.core.mail import EmailMessage
from django.http.request import split_domain_port
from django.template import Context, Template
from oauthlib.oauth2 import BackendApplicationClient
from requests_oauthlib import OAuth2Session

from airavata.model.security.ttypes import AuthzToken

from . import models


def get_authz_token(request):
    """Construct AuthzToken instance from session; refresh token if needed."""
    if not is_access_token_expired(request):
        return _create_authz_token(request)
    elif not is_refresh_token_expired(request):
        # Have backend reauthenticate the user with the refresh token
        user = authenticate(request)
        if user:
            return _create_authz_token(request)
    return None


def get_service_account_authz_token():
    client_id = settings.KEYCLOAK_CLIENT_ID
    client_secret = settings.KEYCLOAK_CLIENT_SECRET
    token_url = settings.KEYCLOAK_TOKEN_URL
    verify_ssl = settings.KEYCLOAK_VERIFY_SSL

    client = BackendApplicationClient(client_id=client_id)
    oauth = OAuth2Session(client=client)
    if hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
        oauth.verify = settings.KEYCLOAK_CA_CERTFILE
    token = oauth.fetch_token(
        token_url=token_url,
        client_id=client_id,
        client_secret=client_secret,
        verify=verify_ssl)

    access_token = token.get('access_token')
    return AuthzToken(
        accessToken=access_token,
        # This is a service account, so leaving out userName for now
        claimsMap={'gatewayID': settings.GATEWAY_ID})


def _create_authz_token(request):
    access_token = request.session['ACCESS_TOKEN']
    username = request.user.username
    gateway_id = settings.GATEWAY_ID
    return AuthzToken(accessToken=access_token,
                      claimsMap={'gatewayID': gateway_id,
                                 'userName': username})


def is_access_token_expired(request):
    """Return True if access_token is not available or is expired."""
    now = time.time()
    return not request.user.is_authenticated \
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
    msg = EmailMessage(subject=subject,
                       body=body,
                       from_email="{} <{}>".format(
                           settings.PORTAL_TITLE,
                           settings.SERVER_EMAIL),
                       to=[a[1] for a in settings.PORTAL_ADMINS])
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
        reply_to=[f"\"{a[0]}\" <{a[1]}>" for a in settings.PORTAL_ADMINS]
    )
    msg.content_subtype = 'html'
    msg.send()
