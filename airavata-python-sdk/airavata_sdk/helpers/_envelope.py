"""Typed containers that union a proto message with chained-call scalars.

The proto-direct architecture returns the gRPC message as-is whenever that is
sufficient. Some resources need a few extra fields the owning service cannot
compute without breaking service isolation — most commonly the sharing-access
flags, which live in the SHARING service while the resource is owned elsewhere.
Rather than couple the two services, the SDK makes a chained follow-up call and
carries the result alongside the proto here. The proto is never re-modelled: it
flows through under ``.message``; the portal renderer flattens each container to
``MessageToDict(message)`` merged with the extra scalars.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Generic, TypeVar

M = TypeVar("M")
G = TypeVar("G")


@dataclass
class WithAccess(Generic[M]):
    """A proto message unioned with the caller's sharing-access flags.

    ``is_owner`` is SDK-trivial for protos carrying an ``owner`` field (else
    always ``False``). ``user_has_write_access`` is the chained
    ``sharing.user_has_access`` lookup the proto is wrapped for.
    """

    message: M
    is_owner: bool
    user_has_write_access: bool


@dataclass
class WithGroupAccess(Generic[G]):
    """A ``GroupModel`` proto unioned with the caller's six group-access flags.

    Same pattern as :class:`WithAccess`, but a group needs six cross-context
    booleans (a GroupManager admin lookup, the acting user's ``user@gateway`` id,
    and the gateway-group identity ids) the ``GroupModel`` proto cannot compute
    without coupling GroupManager to the gateway-groups catalog.
    """

    message: G
    is_admin: bool
    is_owner: bool
    is_member: bool
    is_gateway_admins_group: bool
    is_read_only_gateway_admins_group: bool
    is_default_gateway_users_group: bool
