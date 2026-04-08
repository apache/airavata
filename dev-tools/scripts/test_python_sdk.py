#!/usr/bin/env python3
"""
Integration test for Python SDK against running Armeria gRPC server.
Prerequisites: server running on localhost:9090, Keycloak at localhost:18080

Usage:
    python3 scripts/test_python_sdk.py [--host localhost] [--port 9090]
"""
import sys
import os
import argparse

# Add SDK to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'airavata-python-sdk'))

def test_imports():
    """Test all SDK modules can be imported."""
    print("Testing imports...")
    from airavata_sdk.clients.api_server_client import APIServerClient
    from airavata_sdk.clients.credential_store_client import CredentialStoreClient
    from airavata_sdk.clients.sharing_registry_client import SharingRegistryClient
    from airavata_sdk.clients.group_manager_client import GroupManagerClient
    from airavata_sdk.clients.iam_admin_client import IAMAdminClient
    from airavata_sdk.clients.user_profile_client import UserProfileClient
    from airavata_sdk.clients.tenant_profile_client import TenantProfileClient
    from airavata_sdk.clients.keycloak_token_fetcher import Authenticator
    print("  All imports OK")

def test_channel_creation(host, port):
    """Test gRPC channel can be created."""
    print("Testing gRPC channel creation...")
    import grpc
    channel = grpc.insecure_channel(f"{host}:{port}")

    # Try to get channel state
    state = channel.check_connectivity_state(True)
    print(f"  Channel state: {state}")
    channel.close()
    print("  Channel OK")

def test_api_client(host, port, token=None):
    """Test APIServerClient methods."""
    print("Testing APIServerClient...")
    os.environ['API_SERVER_HOSTNAME'] = host
    os.environ['API_SERVER_PORT'] = str(port)

    from airavata_sdk.clients.api_server_client import APIServerClient

    client = APIServerClient(access_token=token or "test-token")

    # Test gateway operations (should work or return auth error)
    try:
        gateways = client.get_all_gateways()
        print(f"  get_all_gateways: {len(gateways)} gateways")
    except Exception as e:
        print(f"  get_all_gateways: {type(e).__name__}: {e}")

    # Test project listing
    try:
        projects = client.get_user_projects("default", "admin", 10, 0)
        print(f"  get_user_projects: {len(projects)} projects")
    except Exception as e:
        print(f"  get_user_projects: {type(e).__name__}: {e}")

    # Test experiment search
    try:
        experiments = client.search_experiments("default", "admin", {}, 10, 0)
        print(f"  search_experiments: {len(experiments)} experiments")
    except Exception as e:
        print(f"  search_experiments: {type(e).__name__}: {e}")

    print("  APIServerClient tests done")

def test_sharing_client(host, port, token=None):
    """Test SharingRegistryClient."""
    print("Testing SharingRegistryClient...")
    os.environ['API_SERVER_HOSTNAME'] = host
    os.environ['API_SERVER_PORT'] = str(port)

    from airavata_sdk.clients.sharing_registry_client import SharingRegistryClient

    client = SharingRegistryClient(access_token=token or "test-token")

    try:
        domains = client.get_domains(0, -1)
        print(f"  get_domains: {len(domains)} domains")
    except Exception as e:
        print(f"  get_domains: {type(e).__name__}: {e}")

    print("  SharingRegistryClient tests done")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=9090)
    parser.add_argument("--token", default=None, help="OAuth2 access token")
    args = parser.parse_args()

    print(f"=== Python SDK Integration Tests (server: {args.host}:{args.port}) ===\n")

    test_imports()
    test_channel_creation(args.host, args.port)
    test_api_client(args.host, args.port, args.token)
    test_sharing_client(args.host, args.port, args.token)

    print("\n=== Done ===")
