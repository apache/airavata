"""
Wrapper around the IAM Admin Services client.
"""

from django_airavata.utils import get_iam_admin_client

from . import utils


def is_username_available(username):
    with get_iam_admin_client() as iam_admin_client:
        authz_token = utils.get_service_account_authz_token()
        return iam_admin_client.isUsernameAvailable(authz_token, username)
