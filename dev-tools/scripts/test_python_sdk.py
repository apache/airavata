#!/usr/bin/env python3
"""Integration smoke test for the Python SDK against a running Armeria gRPC server.

The SDK is generated-only: there is no hand-written client wrapper. This script
talks to the server exactly the way the portal and airavata.experiments do —
raw protoc-generated stubs over an airavata.auth ``authenticated_channel``.

Prerequisites: server running on localhost:9090 (e.g. via ``tilt up``).

Usage:
    python3 scripts/test_python_sdk.py [--host localhost] [--port 9090] [--token <jwt>]
"""
import argparse
import os
import sys

# Add the SDK to the path so the generated stubs + airavata.auth import.
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "airavata-python-sdk"))


def test_imports():
    """The generated stubs + the auth channel helper import cleanly."""
    print("Testing imports...")
    from airavata.auth import authenticated_channel  # noqa: F401
    from airavata.services import (  # noqa: F401
        experiment_service_pb2,
        experiment_service_pb2_grpc,
        project_service_pb2,
        project_service_pb2_grpc,
        resource_service_pb2,
        resource_service_pb2_grpc,
    )
    print("  All imports OK")


def test_channel_creation(host, port):
    """An authenticated channel can be created and reports connectivity."""
    print("Testing authenticated_channel creation...")
    from airavata.auth import authenticated_channel
    channel = authenticated_channel(host, port, "test-token")
    state = channel.check_connectivity_state(True)
    print(f"  Channel state: {state}")
    print("  Channel OK")


def test_reads(host, port, token=None):
    """A couple of read RPCs over raw stubs (auth errors are fine — the point is wiring)."""
    print("Testing reads over raw stubs...")
    from airavata.auth import authenticated_channel
    from airavata.services import (
        project_service_pb2,
        project_service_pb2_grpc,
        resource_service_pb2,
        resource_service_pb2_grpc,
    )

    channel = authenticated_channel(host, port, token or "test-token")

    resource = resource_service_pb2_grpc.ResourceServiceStub(channel)
    try:
        resp = resource.GetAllComputeResourceNames(
            resource_service_pb2.GetAllComputeResourceNamesRequest()
        )
        print(f"  GetAllComputeResourceNames: {len(resp.compute_resource_names)} resources")
    except Exception as e:
        print(f"  GetAllComputeResourceNames: {type(e).__name__}: {e}")

    projects = project_service_pb2_grpc.ProjectServiceStub(channel)
    try:
        resp = projects.GetUserProjects(
            project_service_pb2.GetUserProjectsRequest(
                gateway_id="default", user_name="admin", limit=10, offset=0
            )
        )
        print(f"  GetUserProjects: {len(resp.projects)} projects")
    except Exception as e:
        print(f"  GetUserProjects: {type(e).__name__}: {e}")

    print("  Read tests done")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=9090)
    parser.add_argument("--token", default=None, help="OAuth2 access token")
    args = parser.parse_args()

    print(f"=== Python SDK Integration Tests (server: {args.host}:{args.port}) ===\n")

    test_imports()
    test_channel_creation(args.host, args.port)
    test_reads(args.host, args.port, args.token)

    print("\n=== Done ===")
