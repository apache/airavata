"""
Wrapper around the IAM Admin Services client.
"""

import logging

from django_airavata.utils import get_iam_admin_client

from . import utils

logger = logging.getLogger(__name__)


def is_username_available(username):
    with get_iam_admin_client() as iam_admin_client:
        authz_token = utils.get_service_account_authz_token()
        return iam_admin_client.isUsernameAvailable(authz_token, username)


def register_user(username, email_address, first_name, last_name, password):
    with get_iam_admin_client() as iam_admin_client:
        authz_token = utils.get_service_account_authz_token()
        return iam_admin_client.registerUser(
            authz_token,
            username,
            email_address,
            first_name,
            last_name,
            password)
