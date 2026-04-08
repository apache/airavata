import json
import logging
from typing import Optional

import grpc

log = logging.getLogger(__name__)


class AiravataClient:
    """Transport-agnostic facade over all Airavata gRPC services.

    Provides 7 module-aligned sub-clients that share a single gRPC channel
    and authentication metadata.
    """

    def __init__(self, host, port, token, gateway_id, secure=False, claims=None):
        target = f"{host}:{port}"
        if secure:
            self._channel = grpc.secure_channel(target, grpc.ssl_channel_credentials())
        else:
            self._channel = grpc.insecure_channel(target)

        self._metadata = []
        if token:
            self._metadata.append(("authorization", f"Bearer {token}"))
        if claims:
            self._metadata.append(("x-claims", json.dumps(claims)))

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

    def close(self):
        self._channel.close()

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.close()
