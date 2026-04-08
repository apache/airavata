#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import json
import logging
from typing import Optional

import grpc

from airavata_sdk import Settings
from airavata_sdk.transport.utils import create_credential_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class CredentialStoreClient:
    """Client for the Airavata Credential Service (gRPC).

    Replaces the old Thrift CredentialStoreService client.
    All methods accept and return protobuf message types.
    """

    def __init__(self, access_token: Optional[str] = None, claims: Optional[dict] = None):
        self.settings = Settings()
        host = self.settings.API_SERVER_HOSTNAME
        port = self.settings.API_SERVER_PORT
        secure = self.settings.API_SERVER_SECURE

        target = f"{host}:{port}"
        if secure:
            self.channel = grpc.secure_channel(target, grpc.ssl_channel_credentials())
        else:
            self.channel = grpc.insecure_channel(target)

        self._metadata: list[tuple[str, str]] = []
        if access_token:
            self._metadata.append(("authorization", f"Bearer {access_token}"))
        if claims:
            self._metadata.append(("x-claims", json.dumps(claims)))

        self._stub = create_credential_service_stub(self.channel)

    def close(self):
        self.channel.close()

    def get_SSH_credential(self, token_id, gateway_id):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.GetCredentialSummary(
            pb2.GetCredentialSummaryRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def generate_and_register_ssh_keys(self, gateway_id, username, description=""):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.GenerateAndRegisterSSHKeys(
            pb2.GenerateAndRegisterSSHKeysRequest(gateway_id=gateway_id, username=username, description=description),
            metadata=self._metadata,
        )

    def register_pwd_credential(self, gateway_id, password_credential):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.RegisterPwdCredential(
            pb2.RegisterPwdCredentialRequest(gateway_id=gateway_id, password_credential=password_credential),
            metadata=self._metadata,
        )

    def get_credential_summary(self, token_id, gateway_id):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.GetCredentialSummary(
            pb2.GetCredentialSummaryRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_credential_summaries(self, gateway_id, type):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.GetAllCredentialSummaries(
            pb2.GetAllCredentialSummariesRequest(gateway_id=gateway_id, type=type),
            metadata=self._metadata,
        )

    def delete_ssh_pub_key(self, token_id, gateway_id):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.DeleteSSHPubKey(
            pb2.DeleteSSHPubKeyRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_pwd_credential(self, token_id, gateway_id):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.DeletePWDCredential(
            pb2.DeletePWDCredentialRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def is_ssh_setup_complete(self, compute_resource_id, gateway_id, username):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.IsSSHSetupComplete(
            pb2.IsSSHSetupCompleteRequest(compute_resource_id=compute_resource_id, gateway_id=gateway_id, username=username),
            metadata=self._metadata,
        )

    def setup_ssh_account(self, compute_resource_id, gateway_id, username):
        from airavata_sdk.generated.services import credential_service_pb2 as pb2
        return self._stub.SetupSSHAccount(
            pb2.SetupSSHAccountRequest(compute_resource_id=compute_resource_id, gateway_id=gateway_id, username=username),
            metadata=self._metadata,
        )
