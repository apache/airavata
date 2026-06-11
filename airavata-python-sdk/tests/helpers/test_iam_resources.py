"""Unit tests for airavata_sdk.helpers.iam_resources.

The user-profiles family is **proto-direct**: ``get_user_profile`` returns the
raw ``UserProfile`` proto and ``get_all_user_profiles_in_gateway`` returns a
``list[UserProfile]`` — the family computes nothing cross-service, so there is
no dict transform and no envelope.  The tests assert the protos flow through
the raw facade unchanged (identity, not value re-mapping).

The IAM-user family is **proto-direct (composed)**: ``get_iam_user`` returns a
pydantic ``IAMUser`` and ``get_unverified_email_user`` an ``UnverifiedEmailUser``
— the proto-derived scalars unioned with the cross-service / request-scoped /
ORM parts the ViewSet supplies.  ``list_iam_users`` is a raw pass-through of the
IAM-admin ``UserProfile`` protos.

Orchestration functions are tested via lightweight stub clients that record the
calls made.
"""

from airavata_sdk.generated.org.apache.airavata.model.user import (
    user_profile_pb2 as pb2,
)
from airavata_sdk.helpers.iam_resources import (
    IAMUser,
    UnverifiedEmailUser,
    get_all_user_profiles_in_gateway,
    get_iam_user,
    get_unverified_email_user,
    get_user_profile,
    list_iam_users,
)


# ---------------------------------------------------------------------------
# Representative proto
# ---------------------------------------------------------------------------

def _full_profile():
    return pb2.UserProfile(
        user_model_version="1.0",
        airavata_internal_user_id="internal-1",
        user_id="alice@gw",
        gateway_id="default",
        emails=["a@x.com", "b@x.com"],
        first_name="Alice",
        last_name="Liddell",
        middle_name="M",
        name_prefix="Dr",
        name_suffix="Jr",
        orcid_id="0000-0001",
        phones=["555-1234"],
        country="US",
        nationality=["American"],
        home_organization="GT",
        origination_affiliation="ARTISAN",
        creation_time=1705320000000,
        last_access_time=1705320005000,
        valid_until=1705320009000,
        state=pb2.Status.ACTIVE,
        comments="hello",
        labeled_uri=["http://a", "http://b"],
        gpg_key="gpg123",
        time_zone="UTC",
    )


# ---------------------------------------------------------------------------
# Orchestration functions — stub client (proto-direct returns)
# ---------------------------------------------------------------------------

class _FakeIam:
    def __init__(self, profile, profiles=None):
        self._profile = profile
        self._profiles = profiles if profiles is not None else [profile]
        self.by_id_calls = []
        self.list_calls = []

    def get_user_profile_by_id(self, user_id, gateway_id):
        self.by_id_calls.append((user_id, gateway_id))
        return self._profile

    def get_all_user_profiles_in_gateway(self, gateway_id, offset, limit):
        self.list_calls.append((gateway_id, offset, limit))
        return self._profiles


class _FakeClient:
    def __init__(self, profile, profiles=None, gateway_id="gw1",
                 username="alice"):
        self.iam = _FakeIam(profile, profiles)
        self.gateway_id = gateway_id
        self.username = username


class TestGetUserProfile:
    def test_returns_the_raw_proto(self):
        """Proto-direct: the facade's ``UserProfile`` flows through unchanged
        (no transform, no envelope) — the same object identity is returned."""
        profile = _full_profile()
        client = _FakeClient(profile)
        result = get_user_profile(client, "alice@gw")
        assert result is profile
        assert isinstance(result, pb2.UserProfile)
        # Proto fields are preserved verbatim; no '' -> None remapping, no
        # field renames, the enum stays the proto enum value.
        assert result.user_id == "alice@gw"
        assert result.state == pb2.Status.ACTIVE
        assert result.origination_affiliation == "ARTISAN"

    def test_lookup_uses_client_gateway(self):
        client = _FakeClient(_full_profile())
        get_user_profile(client, "alice@gw")
        assert client.iam.by_id_calls == [("alice@gw", "gw1")]


class TestGetAllUserProfilesInGateway:
    def test_returns_list_of_protos(self):
        p1 = _full_profile()
        p2 = pb2.UserProfile(user_id="bob@gw")
        client = _FakeClient(p1, profiles=[p1, p2])
        result = get_all_user_profiles_in_gateway(client)
        assert all(isinstance(u, pb2.UserProfile) for u in result)
        assert [u.user_id for u in result] == ["alice@gw", "bob@gw"]
        # The protos themselves flow through (identity), only re-listed.
        assert result[0] is p1
        assert result[1] is p2

    def test_default_offset_and_limit(self):
        client = _FakeClient(_full_profile())
        get_all_user_profiles_in_gateway(client)
        assert client.iam.list_calls == [("gw1", 0, -1)]

    def test_custom_offset_and_limit(self):
        client = _FakeClient(_full_profile())
        get_all_user_profiles_in_gateway(client, offset=5, limit=10)
        assert client.iam.list_calls == [("gw1", 5, 10)]


# ===========================================================================
# IAM-user family (composed pydantic: IAMUser / UnverifiedEmailUser)
# ===========================================================================

def _iam_profile(state=pb2.Status.ACTIVE, **overrides):
    base = dict(
        airavata_internal_user_id="alice@gw_internal",
        user_id="alice@gw",
        gateway_id="default",
        emails=["alice@x.com", "alt@x.com"],
        first_name="Alice",
        last_name="Liddell",
        creation_time=1705320000000,
        state=state,
    )
    base.update(overrides)
    return pb2.UserProfile(**base)


class _FakeIamUsers:
    def __init__(self, users):
        self._users = users
        self.list_calls = []

    def get_iam_users(self, offset, limit, search):
        self.list_calls.append((offset, limit, search))
        return list(self._users)


class _FakeIamUserClient:
    def __init__(self, users, gateway_id="gw1", username="alice"):
        self.iam = _FakeIamUsers(users)
        self.gateway_id = gateway_id
        self.username = username


def _call_get_iam_user(profile, **overrides):
    kwargs = dict(
        airavata_user_profile_exists=True,
        user_has_write_access=True,
        groups=[{"id": "g1", "name": "Group One"}],
        external_idp_user_info={"idp_alias": "cilogon"},
        user_profile_invalid_fields=["phone"],
    )
    kwargs.update(overrides)
    return get_iam_user(_FakeIamUserClient([]), profile, **kwargs)


class TestGetIamUser:

    def test_returns_iam_user_model(self):
        u = _call_get_iam_user(_iam_profile())
        assert isinstance(u, IAMUser)

    def test_scalar_and_derived_fields(self):
        u = _call_get_iam_user(_iam_profile())
        assert u.airavata_internal_user_id == "alice@gw_internal"
        assert u.user_id == "alice@gw"
        assert u.gateway_id == "default"
        # email is the FIRST element of the repeated emails field
        assert u.email == "alice@x.com"
        assert u.first_name == "Alice"
        assert u.last_name == "Liddell"
        assert u.airavata_user_profile_exists is True

    def test_composed_fields_passed_through(self):
        u = _call_get_iam_user(_iam_profile())
        assert u.user_has_write_access is True
        assert u.groups == [{"id": "g1", "name": "Group One"}]
        assert u.external_idp_user_info == {"idp_alias": "cilogon"}
        assert u.user_profile_invalid_fields == ["phone"]

    def test_composed_fields_default_empty(self):
        u = get_iam_user(
            _FakeIamUserClient([]), _iam_profile(),
            airavata_user_profile_exists=False,
            user_has_write_access=False)
        assert u.groups == []
        assert u.external_idp_user_info == {}
        assert u.user_profile_invalid_fields == []

    def test_enabled_and_email_verified_active(self):
        u = _call_get_iam_user(_iam_profile(state=pb2.Status.ACTIVE))
        assert u.enabled is True
        assert u.email_verified is True

    def test_email_verified_confirmed_but_not_enabled(self):
        u = _call_get_iam_user(_iam_profile(state=pb2.Status.CONFIRMED))
        assert u.enabled is False
        assert u.email_verified is True

    def test_neither_enabled_nor_verified_for_other_state(self):
        u = _call_get_iam_user(_iam_profile(state=pb2.Status.PENDING))
        assert u.enabled is False
        assert u.email_verified is False

    def test_creation_time_is_epoch_millis_string(self):
        u = _call_get_iam_user(_iam_profile())
        assert u.creation_time == "1705320000000"

    def test_creation_time_zero_is_string_zero(self):
        """proto-direct int64 contract: 0 -> "0", never None / never epoch ISO."""
        u = _call_get_iam_user(_iam_profile(creation_time=0))
        assert u.creation_time == "0"

    def test_no_emails_renders_empty_email(self):
        prof = pb2.UserProfile(user_id="bob@gw", state=pb2.Status.ACTIVE)
        u = _call_get_iam_user(prof)
        assert u.email == ""

    def test_no_url_field(self):
        """The hyperlink is dropped; the frontend builds it from user_id."""
        u = _call_get_iam_user(_iam_profile())
        assert not hasattr(u, "url")

    def test_expected_field_set(self):
        u = _call_get_iam_user(_iam_profile())
        assert set(u.model_dump().keys()) == {
            "airavata_internal_user_id", "user_id", "gateway_id", "email",
            "first_name", "last_name", "enabled", "email_verified",
            "creation_time", "airavata_user_profile_exists",
            "user_has_write_access", "groups", "external_idp_user_info",
            "user_profile_invalid_fields",
        }


class TestGetUnverifiedEmailUser:

    def test_returns_unverified_model(self):
        u = get_unverified_email_user(
            _FakeIamUserClient([]), _iam_profile(state=pb2.Status.CONFIRMED),
            user_has_write_access=False)
        assert isinstance(u, UnverifiedEmailUser)

    def test_subset_field_set(self):
        u = get_unverified_email_user(
            _FakeIamUserClient([]), _iam_profile(state=pb2.Status.CONFIRMED),
            user_has_write_access=True)
        assert set(u.model_dump().keys()) == {
            "user_id", "gateway_id", "email", "first_name", "last_name",
            "enabled", "email_verified", "creation_time",
            "user_has_write_access",
        }

    def test_derived_flags(self):
        u = get_unverified_email_user(
            _FakeIamUserClient([]), _iam_profile(state=pb2.Status.CONFIRMED),
            user_has_write_access=False)
        assert u.enabled is False
        assert u.email_verified is True
        assert u.email == "alice@x.com"
        assert u.user_has_write_access is False

    def test_creation_time_is_epoch_millis_string(self):
        u = get_unverified_email_user(
            _FakeIamUserClient([]), _iam_profile(),
            user_has_write_access=False)
        assert u.creation_time == "1705320000000"


class TestListIamUsers:
    def test_passes_through_protos(self):
        p = _iam_profile()
        client = _FakeIamUserClient([p])
        result = list_iam_users(client, offset=2, limit=5, search="ali")
        assert result == [p]
        assert client.iam.list_calls == [(2, 5, "ali")]

    def test_defaults(self):
        client = _FakeIamUserClient([])
        list_iam_users(client)
        assert client.iam.list_calls == [(0, -1, "")]
