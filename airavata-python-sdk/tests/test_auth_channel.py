"""Contract for the ``airavata.auth`` authenticated gRPC channel.

The portal (and any client) wires to the *raw* generated stubs; auth is the one
Python-centric concern that stays. ``authenticated_channel`` returns a channel
that injects ``authorization: Bearer <token>`` on every outbound call via a
client interceptor, so call sites are pure ``stub.Method(request)`` with no
per-call metadata threading.
"""

from collections import namedtuple

from airavata.auth.channel import BearerAuthInterceptor

# Mirror grpc.ClientCallDetails' attribute surface for interceptor unit tests.
_CallDetails = namedtuple(
    "_CallDetails",
    ("method", "timeout", "metadata", "credentials", "wait_for_ready", "compression"),
)


def _details(metadata=None):
    return _CallDetails(
        method="/svc/Method",
        timeout=None,
        metadata=metadata,
        credentials=None,
        wait_for_ready=None,
        compression=None,
    )


class TestBearerAuthInterceptor:
    def test_injects_bearer_authorization_metadata(self):
        captured = {}

        def continuation(call_details, request):
            captured["metadata"] = list(call_details.metadata)
            return "response"

        result = BearerAuthInterceptor("tok123").intercept_unary_unary(
            continuation, _details(metadata=None), "req"
        )

        assert result == "response"
        assert ("authorization", "Bearer tok123") in captured["metadata"]

    def test_preserves_existing_metadata(self):
        captured = {}

        def continuation(call_details, request):
            captured["metadata"] = list(call_details.metadata)
            return "ok"

        BearerAuthInterceptor("t").intercept_unary_unary(
            continuation, _details(metadata=[("x-existing", "y")]), "req"
        )

        assert ("x-existing", "y") in captured["metadata"]
        assert ("authorization", "Bearer t") in captured["metadata"]

    def test_applies_to_all_four_call_types(self):
        seen = []

        def continuation(call_details, request):
            seen.append(list(call_details.metadata))
            return "r"

        interceptor = BearerAuthInterceptor("tok")
        interceptor.intercept_unary_unary(continuation, _details(), "req")
        interceptor.intercept_unary_stream(continuation, _details(), "req")
        interceptor.intercept_stream_unary(continuation, _details(), iter([]))
        interceptor.intercept_stream_stream(continuation, _details(), iter([]))

        assert len(seen) == 4
        assert all(("authorization", "Bearer tok") in md for md in seen)
