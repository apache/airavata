"""Tests for the username property on AiravataClient."""
from airavata_sdk.client import AiravataClient


def _client_with_claims(claims):
    """Construct an AiravataClient shell without opening a gRPC channel."""
    c = AiravataClient.__new__(AiravataClient)
    c.claims = claims
    return c


def test_username_returns_value_from_claims():
    c = _client_with_claims({"gatewayID": "default", "userName": "default-admin"})
    assert AiravataClient.username.fget(c) == "default-admin"


def test_username_none_when_key_missing():
    c = _client_with_claims({"gatewayID": "default"})
    assert AiravataClient.username.fget(c) is None


def test_username_none_when_claims_none():
    c = _client_with_claims(None)
    assert AiravataClient.username.fget(c) is None


def test_username_none_when_claims_empty():
    c = _client_with_claims({})
    assert AiravataClient.username.fget(c) is None
