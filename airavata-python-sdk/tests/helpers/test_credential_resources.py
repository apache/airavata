"""Unit tests for airavata_sdk.helpers.credential_resources (proto-direct).

The credential helpers return OBJECTS, not dicts: a
:class:`airavata_sdk.helpers._envelope.WithAccess` wrapping the raw
``credential_store_pb2.CredentialSummary`` proto plus the caller's access
flags.  ``is_owner`` is always ``False`` (a credential has no owner);
``user_has_write_access`` is the CHAINED ``sharing.user_has_access`` WRITE
lookup keyed on the credential ``token``.

These tests assert:

* the return is a ``WithAccess`` whose ``.message`` is the raw proto (no field
  copied out, ``type`` left as the proto enum, not a string);
* ``is_owner`` is ``False`` and ``user_has_write_access`` reflects the chained
  sharing call;
* the chained ``sharing.user_has_access`` call is keyed on the ``token`` with
  ``permission_type="WRITE"``;
* the create/delete orchestration still drives the raw facade correctly.

Snake_case JSON rendering (enum NAMES, ``persisted_time`` epoch-millis string)
is the portal renderer's responsibility and is pinned by the portal contract
snapshot test, not here.
"""

from airavata_sdk.generated.org.apache.airavata.model.credential.store import (
    credential_store_pb2 as pb2,
)
from airavata_sdk.helpers._envelope import WithAccess
from airavata_sdk.helpers.credential_resources import (
    create_password_credential,
    create_ssh_credential,
    delete_credential_summary,
    get_all_credential_summaries,
    get_credential_summary,
)


# ---------------------------------------------------------------------------
# Helpers / stubs
# ---------------------------------------------------------------------------

def _make_summary(**kwargs):
    return pb2.CredentialSummary(**kwargs)


class _FakeCredential:
    def __init__(self, summary):
        self._summary = summary
        self.generated = None
        self.registered = None
        self.deleted_ssh = None
        self.deleted_pwd = None
        self.list_calls = []

    def get_credential_summary(self, token_id, gateway_id):
        return self._summary

    def get_all_credential_summaries(self, gateway_id, type):
        self.list_calls.append((gateway_id, type))
        return [self._summary]

    def generate_and_register_ssh_keys(self, gateway_id, username, description):
        self.generated = (gateway_id, username, description)
        return "new-ssh-token"

    def register_pwd_credential(self, gateway_id, password_credential):
        self.registered = (gateway_id, password_credential)
        return "new-pwd-token"

    def delete_ssh_pub_key(self, token, gateway_id):
        self.deleted_ssh = (token, gateway_id)

    def delete_pwd_credential(self, token, gateway_id):
        self.deleted_pwd = (token, gateway_id)


class _FakeSharing:
    def __init__(self, has_access=True):
        self._has_access = has_access
        self.calls = []

    def user_has_access(self, resource_id, user_id, permission_type):
        self.calls.append((resource_id, user_id, permission_type))
        return self._has_access


class _FakeClient:
    def __init__(self, summary, gateway_id="gw1", username="alice",
                 sharing_has_access=True):
        self.credential = _FakeCredential(summary)
        self.sharing = _FakeSharing(sharing_has_access)
        self.gateway_id = gateway_id
        self.username = username


def _summary(type=pb2.SummaryType.SSH, token="tok-1"):
    return _make_summary(
        type=type, gateway_id="gw1", username="alice",
        persisted_time=1705320000000, token=token, description="d")


# ---------------------------------------------------------------------------
# get_credential_summary — WithAccess[CredentialSummary]
# ---------------------------------------------------------------------------

class TestGetCredentialSummary:
    def test_returns_with_access(self):
        client = _FakeClient(_summary())
        result = get_credential_summary(client, "tok-1")
        assert isinstance(result, WithAccess)

    def test_message_is_the_raw_proto(self):
        summary = _summary(token="tok-1")
        client = _FakeClient(summary)
        result = get_credential_summary(client, "tok-1")
        assert result.message is summary
        # The proto flows through wholesale — type stays the proto enum, not a
        # rendered string.
        assert result.message.type == pb2.SummaryType.SSH
        assert result.message.token == "tok-1"

    def test_is_owner_always_false(self):
        client = _FakeClient(_summary())
        result = get_credential_summary(client, "tok-1")
        assert result.is_owner is False

    def test_has_write_keyed_on_token(self):
        client = _FakeClient(_summary(token="tok-xyz"))
        get_credential_summary(client, "tok-xyz")
        assert client.sharing.calls == [("tok-xyz", "alice", "WRITE")]

    def test_user_has_write_access_true(self):
        client = _FakeClient(_summary(), sharing_has_access=True)
        result = get_credential_summary(client, "tok-1")
        assert result.user_has_write_access is True

    def test_user_has_write_access_false(self):
        client = _FakeClient(_summary(), sharing_has_access=False)
        result = get_credential_summary(client, "tok-1")
        assert result.user_has_write_access is False


# ---------------------------------------------------------------------------
# get_all_credential_summaries — list[WithAccess]
# ---------------------------------------------------------------------------

class TestGetAllCredentialSummaries:
    def test_concatenates_ssh_and_passwd_when_no_type(self):
        client = _FakeClient(_summary())
        get_all_credential_summaries(client)
        types = [t for (_, t) in client.credential.list_calls]
        assert pb2.SummaryType.SSH in types
        assert pb2.SummaryType.PASSWD in types

    def test_single_type_when_specified(self):
        client = _FakeClient(_summary())
        get_all_credential_summaries(
            client, summary_type=pb2.SummaryType.SSH)
        assert client.credential.list_calls == [("gw1", pb2.SummaryType.SSH)]

    def test_returns_list_of_with_access(self):
        client = _FakeClient(_summary())
        result = get_all_credential_summaries(
            client, summary_type=pb2.SummaryType.SSH)
        assert isinstance(result, list)
        assert isinstance(result[0], WithAccess)
        assert result[0].message.token == "tok-1"
        assert result[0].message.type == pb2.SummaryType.SSH
        assert result[0].is_owner is False

    def test_per_summary_write_lookup_keyed_on_token(self):
        client = _FakeClient(_summary(token="tok-keyed"))
        get_all_credential_summaries(
            client, summary_type=pb2.SummaryType.SSH)
        assert client.sharing.calls == [("tok-keyed", "alice", "WRITE")]

    def test_user_has_write_access_reflects_sharing(self):
        client = _FakeClient(_summary(), sharing_has_access=False)
        result = get_all_credential_summaries(
            client, summary_type=pb2.SummaryType.SSH)
        assert result[0].user_has_write_access is False


class TestCreateSshCredential:
    def test_generates_with_context_and_description(self):
        client = _FakeClient(_summary(token="new-ssh-token"))
        create_ssh_credential(client, {"description": "key desc"})
        assert client.credential.generated == ("gw1", "alice", "key desc")

    def test_returns_with_access_for_new_token(self):
        client = _FakeClient(_summary(token="new-ssh-token"))
        result = create_ssh_credential(client, {"description": "key desc"})
        assert isinstance(result, WithAccess)
        assert result.message.token == "new-ssh-token"
        assert result.is_owner is False


class TestCreatePasswordCredential:
    def test_builds_password_credential_proto(self):
        client = _FakeClient(_summary(type=pb2.SummaryType.PASSWD,
                                      token="new-pwd-token"))
        create_password_credential(client, {
            "username": "loginuser",
            "password": "secret",
            "description": "pw desc",
        })
        gw, pc = client.credential.registered
        assert gw == "gw1"
        assert pc.gateway_id == "gw1"
        assert pc.portal_user_name == "alice"
        assert pc.login_user_name == "loginuser"
        assert pc.password == "secret"
        assert pc.description == "pw desc"

    def test_returns_with_access_for_new_token(self):
        client = _FakeClient(_summary(type=pb2.SummaryType.PASSWD,
                                      token="new-pwd-token"))
        result = create_password_credential(client, {
            "username": "u", "password": "p", "description": "d"})
        assert isinstance(result, WithAccess)
        assert result.message.token == "new-pwd-token"
        assert result.message.type == pb2.SummaryType.PASSWD


class TestDeleteCredentialSummary:
    def test_ssh_dispatches_to_delete_ssh_pub_key(self):
        summary = _summary(type=pb2.SummaryType.SSH, token="ssh-tok")
        client = _FakeClient(summary)
        delete_credential_summary(client, summary)
        assert client.credential.deleted_ssh == ("ssh-tok", "gw1")
        assert client.credential.deleted_pwd is None

    def test_passwd_dispatches_to_delete_pwd_credential(self):
        summary = _summary(type=pb2.SummaryType.PASSWD, token="pwd-tok")
        client = _FakeClient(summary)
        delete_credential_summary(client, summary)
        assert client.credential.deleted_pwd == ("pwd-tok", "gw1")
        assert client.credential.deleted_ssh is None
