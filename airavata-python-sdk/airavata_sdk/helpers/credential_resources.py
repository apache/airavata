"""Credential-domain helpers. A ``CredentialSummary`` is returned in a
:class:`~airavata_sdk.helpers._envelope.WithAccess`: ``is_owner`` is always
``False`` (a credential has no owner) and ``user_has_write_access`` is a chained
``sharing.user_has_access`` WRITE lookup keyed on the credential ``token``.
"""

from __future__ import annotations

from typing import TYPE_CHECKING

from airavata_sdk.helpers._envelope import WithAccess

if TYPE_CHECKING:
    from airavata_sdk.client import AiravataClient


def _has_write(client: "AiravataClient", token: str) -> bool:
    return client.sharing.user_has_access(
        resource_id=token,
        user_id=client.username,
        permission_type="WRITE",
    )


def get_credential_summary(
    client: "AiravataClient",
    token_id: str,
) -> "WithAccess":
    c = client.credential.get_credential_summary(token_id, client.gateway_id)
    return WithAccess(
        message=c,
        is_owner=False,
        user_has_write_access=_has_write(client, c.token),
    )


def get_all_credential_summaries(
    client: "AiravataClient",
    *,
    summary_type=None,
) -> "list[WithAccess]":
    """When *summary_type* is ``None`` the SSH and PASSWD summaries are
    concatenated (the portal list endpoint); otherwise only that type is fetched.
    """
    from airavata_sdk.generated.org.apache.airavata.model.credential.store import (  # noqa: E501
        credential_store_pb2,
    )

    if summary_type is None:
        summaries = (
            client.credential.get_all_credential_summaries(
                client.gateway_id, credential_store_pb2.SummaryType.SSH)
            + client.credential.get_all_credential_summaries(
                client.gateway_id, credential_store_pb2.SummaryType.PASSWD)
        )
    else:
        summaries = client.credential.get_all_credential_summaries(
            client.gateway_id, summary_type)
    return [
        WithAccess(
            message=c,
            is_owner=False,
            user_has_write_access=_has_write(client, c.token),
        )
        for c in summaries
    ]


def create_ssh_credential(
    client: "AiravataClient",
    data: dict,
) -> "WithAccess":
    """``gateway_id`` / ``username`` come from the client context; only
    ``description`` is read from *data*. Re-fetched so the shape matches the read
    path.
    """
    token_id = client.credential.generate_and_register_ssh_keys(
        client.gateway_id,
        client.username,
        data.get("description") or "",
    )
    return get_credential_summary(client, token_id)


def create_password_credential(
    client: "AiravataClient",
    data: dict,
) -> "WithAccess":
    """``gateway_id`` / ``portal_user_name`` come from the client context;
    ``login_user_name`` (wire ``username``) / ``password`` / ``description`` from
    *data*. Re-fetched so the shape matches the read path.
    """
    from airavata_sdk.generated.org.apache.airavata.model.credential.store import (  # noqa: E501
        credential_store_pb2,
    )

    password_credential = credential_store_pb2.PasswordCredential(
        gateway_id=client.gateway_id or "",
        portal_user_name=client.username or "",
        login_user_name=data.get("username") or "",
        password=data.get("password") or "",
        description=data.get("description") or "",
    )
    token_id = client.credential.register_pwd_credential(
        client.gateway_id, password_credential)
    return get_credential_summary(client, token_id)


def delete_credential_summary(client: "AiravataClient", summary) -> None:
    """Dispatch on *summary*'s ``SummaryType``: SSH via ``DeleteSSHPubKey``,
    PASSWD via ``DeletePWDCredential``.
    """
    from airavata_sdk.generated.org.apache.airavata.model.credential.store import (  # noqa: E501
        credential_store_pb2,
    )

    if summary.type == credential_store_pb2.SummaryType.SSH:
        client.credential.delete_ssh_pub_key(summary.token, client.gateway_id)
    elif summary.type == credential_store_pb2.SummaryType.PASSWD:
        client.credential.delete_pwd_credential(summary.token, client.gateway_id)
