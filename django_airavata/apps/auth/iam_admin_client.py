"""
Wrapper around the IAM Admin Services client.
"""

import logging

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
