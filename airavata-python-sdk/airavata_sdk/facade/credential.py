import importlib

from airavata_sdk.transport.utils import create_credential_service_stub


class CredentialClient:
    """Credential store: SSH keys, passwords, and SSH account setup."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._stub = create_credential_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    def get_SSH_credential(self, token_id, gateway_id):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.GetCredentialSummary(
            pb2.GetCredentialSummaryRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def generate_and_register_ssh_keys(self, gateway_id, username, description=""):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.GenerateAndRegisterSSHKeys(
            pb2.GenerateAndRegisterSSHKeysRequest(gateway_id=gateway_id, username=username, description=description),
            metadata=self._metadata,
        )

    def register_pwd_credential(self, gateway_id, password_credential):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.RegisterPwdCredential(
            pb2.RegisterPwdCredentialRequest(gateway_id=gateway_id, password_credential=password_credential),
            metadata=self._metadata,
        )

    def get_credential_summary(self, token_id, gateway_id):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.GetCredentialSummary(
            pb2.GetCredentialSummaryRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_credential_summaries(self, gateway_id, type):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.GetAllCredentialSummaries(
            pb2.GetAllCredentialSummariesRequest(gateway_id=gateway_id, type=type),
            metadata=self._metadata,
        )

    def delete_ssh_pub_key(self, token_id, gateway_id):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.DeleteSSHPubKey(
            pb2.DeleteSSHPubKeyRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_pwd_credential(self, token_id, gateway_id):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.DeletePWDCredential(
            pb2.DeletePWDCredentialRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def is_ssh_setup_complete(self, compute_resource_id, gateway_id, username):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.IsSSHSetupComplete(
            pb2.IsSSHSetupCompleteRequest(compute_resource_id=compute_resource_id, gateway_id=gateway_id, username=username),
            metadata=self._metadata,
        )

    def setup_ssh_account(self, compute_resource_id, gateway_id, username):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.SetupSSHAccount(
            pb2.SetupSSHAccountRequest(compute_resource_id=compute_resource_id, gateway_id=gateway_id, username=username),
            metadata=self._metadata,
        )

    def does_user_have_ssh_account(self, compute_resource_id, gateway_id, username):
        pb2 = self._svc("credential_service_pb2")
        return self._stub.DoesUserHaveSSHAccount(
            pb2.DoesUserHaveSSHAccountRequest(compute_resource_id=compute_resource_id, gateway_id=gateway_id, username=username),
            metadata=self._metadata,
        )
