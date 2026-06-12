"""IAM / user-profile helpers.

Two families:

* **user-profiles** (``get_user_profile`` / ``get_all_user_profiles_in_gateway``)
  — read-only; the ``UserProfile`` proto is returned as-is (nothing
  cross-service is computed).
* **IAM users** (:class:`IAMUser` / :class:`UnverifiedEmailUser`) — the
  administrator-facing managed-account view backed by the Keycloak IAM-admin
  facade (``GetUsers`` / ``GetUser``). A genuinely composed multi-source shape
  (proto-derived scalars unioned with a sharing group list, two Django-ORM
  lookups, a gateway-admin flag, and a ``DoesUserExist`` result), so it is a
  pydantic model carrying plain JSON-able values the ViewSet supplies.
"""

from __future__ import annotations

from typing import TYPE_CHECKING, Any, Optional

from pydantic import BaseModel, ConfigDict

if TYPE_CHECKING:
    from airavata_sdk.client import AiravataClient
    from airavata_sdk.generated.org.apache.airavata.model.user import (
        user_profile_pb2,
    )


def get_user_profile(
    client: "AiravataClient",
    user_id: str,
) -> "user_profile_pb2.UserProfile":
    return client.iam.get_user_profile_by_id(user_id, client.gateway_id)


def get_all_user_profiles_in_gateway(
    client: "AiravataClient",
    *,
    offset: int = 0,
    limit: int = -1,
) -> "list[user_profile_pb2.UserProfile]":
    return list(client.iam.get_all_user_profiles_in_gateway(
        client.gateway_id, offset, limit,
    ))


# IAM user (managed Keycloak user). enabled/email_verified/email/creation_time
# are derived from the proto; the rest (DoesUserExist result, sharing group
# list, gateway-admin flag, two Django-ORM lookups) the ViewSet passes in.


class IAMUser(BaseModel):
    model_config = ConfigDict(extra="forbid")

    airavata_internal_user_id: str
    user_id: str
    gateway_id: str
    email: str
    first_name: str
    last_name: str
    enabled: bool
    email_verified: bool
    creation_time: int
    airavata_user_profile_exists: bool
    user_has_write_access: bool
    groups: list[dict[str, Any]]
    external_idp_user_info: dict[str, Any]
    user_profile_invalid_fields: list[Any]


class UnverifiedEmailUser(BaseModel):
    """A strict subset of :class:`IAMUser`: proto-derived scalars plus
    ``user_has_write_access``.
    """

    model_config = ConfigDict(extra="forbid")

    user_id: str
    gateway_id: str
    email: str
    first_name: str
    last_name: str
    enabled: bool
    email_verified: bool
    creation_time: int
    user_has_write_access: bool


def _iam_user_state_flags(state) -> tuple[bool, bool]:
    """``(enabled, email_verified)``: enabled iff ACTIVE; email_verified iff
    CONFIRMED or ACTIVE.
    """
    from airavata_sdk.generated.org.apache.airavata.model.user import (
        user_profile_pb2,
    )

    Status = user_profile_pb2.Status
    enabled = state == Status.ACTIVE
    email_verified = state in (Status.CONFIRMED, Status.ACTIVE)
    return enabled, email_verified


def get_iam_user(
    client: "AiravataClient",
    user_profile,
    *,
    airavata_user_profile_exists: bool,
    user_has_write_access: bool,
    groups: "Optional[list[dict[str, Any]]]" = None,
    external_idp_user_info: "Optional[dict[str, Any]]" = None,
    user_profile_invalid_fields: "Optional[list[Any]]" = None,
) -> IAMUser:
    """Compose an already-fetched IAM ``UserProfile`` proto into an ``IAMUser``.

    The cross-service / request-scoped / ORM parts are supplied by the caller.
    """
    enabled, email_verified = _iam_user_state_flags(user_profile.state)
    return IAMUser(
        airavata_internal_user_id=user_profile.airavata_internal_user_id,
        user_id=user_profile.user_id,
        gateway_id=user_profile.gateway_id,
        email=user_profile.emails[0] if user_profile.emails else "",
        first_name=user_profile.first_name,
        last_name=user_profile.last_name,
        enabled=enabled,
        email_verified=email_verified,
        creation_time=user_profile.creation_time,
        airavata_user_profile_exists=airavata_user_profile_exists,
        user_has_write_access=user_has_write_access,
        groups=groups or [],
        external_idp_user_info=external_idp_user_info or {},
        user_profile_invalid_fields=(
            [] if user_profile_invalid_fields is None
            else user_profile_invalid_fields),
    )


def get_unverified_email_user(
    client: "AiravataClient",
    user_profile,
    *,
    user_has_write_access: bool,
) -> UnverifiedEmailUser:
    enabled, email_verified = _iam_user_state_flags(user_profile.state)
    return UnverifiedEmailUser(
        user_id=user_profile.user_id,
        gateway_id=user_profile.gateway_id,
        email=user_profile.emails[0] if user_profile.emails else "",
        first_name=user_profile.first_name,
        last_name=user_profile.last_name,
        enabled=enabled,
        email_verified=email_verified,
        creation_time=user_profile.creation_time,
        user_has_write_access=user_has_write_access,
    )


def list_iam_users(
    client: "AiravataClient",
    *,
    offset: int = 0,
    limit: int = -1,
    search: str = "",
) -> list:
    """Raw IAM ``UserProfile`` protos (``GetUsers``); the caller composes each
    :class:`IAMUser` via :func:`get_iam_user` (per-user enrichment needs
    per-user lookups).
    """
    return client.iam.get_iam_users(offset=offset, limit=limit, search=search)
