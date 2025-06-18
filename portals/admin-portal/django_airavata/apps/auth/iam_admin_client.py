"""
Wrapper around the IAM Admin Services client.
"""

import logging
from urllib.parse import urlparse

import requests
from django.conf import settings

from django_airavata.utils import iamadmin_client_pool

from . import utils

logger = logging.getLogger(__name__)


def is_username_available(username):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.isUsernameAvailable(authz_token, username)


def register_user(username, email_address, first_name, last_name, password):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.registerUser(
        authz_token,
        username,
        email_address,
        first_name,
        last_name,
        password)


def is_user_enabled(username):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.isUserEnabled(authz_token, username)


def enable_user(username):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.enableUser(authz_token, username)


def delete_user(username):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.deleteUser(authz_token, username)


def is_user_exist(username):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.isUserExist(authz_token, username)


def get_user(username):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.getUser(authz_token, username)


def get_users(offset, limit, search=None):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.getUsers(authz_token, offset, limit, search)


def reset_user_password(username, new_password):
    authz_token = utils.get_service_account_authz_token()
    return iamadmin_client_pool.resetUserPassword(
        authz_token, username, new_password)


def update_username(username, new_username):
    # make sure that new_username is available
    if not is_username_available(new_username):
        raise Exception(f"Can't change username of {username} to {new_username} because it is not available")
    # fetch user representation
    authz_token = utils.get_service_account_authz_token()
    headers = {'Authorization': f'Bearer {authz_token.accessToken}'}
    parsed = urlparse(settings.KEYCLOAK_AUTHORIZE_URL)
    r = requests.get(f"{parsed.scheme}://{parsed.netloc}/auth/admin/realms/{settings.GATEWAY_ID}/users",
                     params={'username': username},
                     headers=headers)
    r.raise_for_status()
    user_list = r.json()
    user = None
    # The users search finds partial matches. Loop to find the exact match.
    for u in user_list:
        if u['username'] == username:
            user = u
            break
    if user is None:
        raise Exception(f"Could not find user {username}")

    # update username
    user['username'] = new_username
    r = requests.put(f"{parsed.scheme}://{parsed.netloc}/auth/admin/realms/{settings.GATEWAY_ID}/users/{user['id']}",
                     json=user,
                     headers=headers)
    r.raise_for_status()


def update_user(username, first_name=None, last_name=None, email=None):
    # fetch user representation
    authz_token = utils.get_service_account_authz_token()
    headers = {'Authorization': f'Bearer {authz_token.accessToken}'}
    parsed = urlparse(settings.KEYCLOAK_AUTHORIZE_URL)
    r = requests.get(f"{parsed.scheme}://{parsed.netloc}/auth/admin/realms/{settings.GATEWAY_ID}/users",
                     params={'username': username},
                     headers=headers)
    r.raise_for_status()
    user_list = r.json()
    user = None
    # The users search finds partial matches. Loop to find the exact match.
    for u in user_list:
        if u['username'] == username:
            user = u
            break
    if user is None:
        raise Exception(f"Could not find user {username}")

    # update user
    if first_name is not None:
        user['firstName'] = first_name
    if last_name is not None:
        user['lastName'] = last_name
    if email is not None:
        user['email'] = email
    r = requests.put(f"{parsed.scheme}://{parsed.netloc}/auth/admin/realms/{settings.GATEWAY_ID}/users/{user['id']}",
                     json=user,
                     headers=headers)
    r.raise_for_status()
