"""Authenticated gRPC channel for talking to the Airavata server.

Auth is the one Python-centric concern that stays in the SDK while the rest of
the hand-written wrapper is retired in favour of the raw generated stubs. A
client interceptor injects ``authorization: Bearer <token>`` into every outbound
call, so call sites are pure ``stub.Method(request)`` with no per-call metadata
threading. The interceptor (not channel call-credentials) is used because call
credentials are rejected on plaintext channels, and the portal reaches the
in-cluster server over plaintext gRPC.
"""

from __future__ import annotations

import collections

import grpc


class _ClientCallDetails(
    collections.namedtuple(
        "_ClientCallDetails",
        ("method", "timeout", "metadata", "credentials", "wait_for_ready", "compression"),
    ),
    grpc.ClientCallDetails,
):
    pass


class BearerAuthInterceptor(
    grpc.UnaryUnaryClientInterceptor,
    grpc.UnaryStreamClientInterceptor,
    grpc.StreamUnaryClientInterceptor,
    grpc.StreamStreamClientInterceptor,
):
    """Adds ``authorization: Bearer <token>`` to every call's metadata.

    The server verifies the token and derives identity (user + gateway) from it;
    no client-asserted identity is sent.
    """

    def __init__(self, access_token: str):
        self._auth = ("authorization", f"Bearer {access_token}")

    def _augment(self, client_call_details: grpc.ClientCallDetails) -> _ClientCallDetails:
        metadata = list(client_call_details.metadata or [])
        metadata.append(self._auth)
        return _ClientCallDetails(
            client_call_details.method,
            client_call_details.timeout,
            metadata,
            client_call_details.credentials,
            client_call_details.wait_for_ready,
            client_call_details.compression,
        )

    def intercept_unary_unary(self, continuation, client_call_details, request):
        return continuation(self._augment(client_call_details), request)

    def intercept_unary_stream(self, continuation, client_call_details, request):
        return continuation(self._augment(client_call_details), request)

    def intercept_stream_unary(self, continuation, client_call_details, request_iterator):
        return continuation(self._augment(client_call_details), request_iterator)

    def intercept_stream_stream(self, continuation, client_call_details, request_iterator):
        return continuation(self._augment(client_call_details), request_iterator)


# 64KB metadata ceiling matches the server's large-error-message budget.
_DEFAULT_OPTIONS = [("grpc.max_metadata_size", 64 * 1024)]


def authenticated_channel(
    host: str,
    port: int,
    access_token: str,
    *,
    secure: bool = False,
    options: list[tuple[str, object]] | None = None,
) -> grpc.Channel:
    """Return a gRPC channel that Bearer-authenticates every call.

    Build the raw generated stubs directly from the returned channel, e.g.
    ``ProjectServiceStub(authenticated_channel(...))``; each call carries the
    token automatically.
    """
    target = f"{host}:{port}"
    opts = _DEFAULT_OPTIONS + list(options or [])
    base = (
        grpc.secure_channel(target, grpc.ssl_channel_credentials(), opts)
        if secure
        else grpc.insecure_channel(target, opts)
    )
    return grpc.intercept_channel(base, BearerAuthInterceptor(access_token))
