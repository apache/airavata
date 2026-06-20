import base64
import json
import logging

import grpc

log = logging.getLogger(__name__)


def _decode_jwt_claims(token):
    """Decode a JWT payload (no signature verification) to read identity claims for convenience.

    Used only to expose ``username``/``gateway_id`` locally; authorization is the token itself,
    which the server verifies and from which it derives identity.
    """
    try:
        payload = token.split(".")[1]
        payload += "=" * (-len(payload) % 4)
        return json.loads(base64.urlsafe_b64decode(payload))
    except Exception:
        return {}


class AiravataClient:
    """Transport-agnostic facade over all Airavata gRPC services.

    Provides 7 module-aligned sub-clients that share a single gRPC channel
    and authentication metadata.
    """

    def __init__(self, host, port, token, gateway_id, secure=False, claims=None):
        target = f"{host}:{port}"
        options = [
            ("grpc.max_metadata_size", 64 * 1024),  # 64KB for large error messages
        ]
        if secure:
            self._channel = grpc.secure_channel(target, grpc.ssl_channel_credentials(), options=options)
        else:
            self._channel = grpc.insecure_channel(target, options=options)

        # Auth is the Keycloak access token and nothing else: it is sent as a Bearer
        # credential and the server derives identity (user + gateway) from the verified
        # token. Client-asserted identity (x-claims) is no longer sent.
        self._token_claims = _decode_jwt_claims(token) if token else {}
        self.claims = self._token_claims

        self._metadata = []
        if token:
            self._metadata.append(("authorization", f"Bearer {token}"))

        self._gateway_id = gateway_id

        from airavata_sdk.facade import (
            AgentClient, ComputeClient, CredentialClient,
            IamClient, ResearchClient, SharingClient, StorageClient,
        )

        self.compute = ComputeClient(self._channel, self._metadata, self._gateway_id)
        self.storage = StorageClient(self._channel, self._metadata, self._gateway_id)
        self.credential = CredentialClient(self._channel, self._metadata, self._gateway_id)
        self.research = ResearchClient(self._channel, self._metadata, self._gateway_id)
        self.iam = IamClient(self._channel, self._metadata, self._gateway_id)
        self.sharing = SharingClient(self._channel, self._metadata, self._gateway_id)
        self.agent = AgentClient(self._channel, self._metadata, self._gateway_id)

    @property
    def username(self):
        """Return the caller's username decoded from the access token, or None."""
        return self._token_claims.get("preferred_username")

    @property
    def gateway_id(self):
        """Return the gateway ID this client was initialised with."""
        return self._gateway_id

    def close(self):
        self._channel.close()

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.close()
