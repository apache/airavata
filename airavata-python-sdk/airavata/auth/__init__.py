"""Python-centric auth for Airavata clients.

The hand-written gRPC wrapper (``airavata`` facade/helpers/clients) is being
retired in favour of the raw generated stubs; auth is one of the two pieces that
rightfully stays in Python. :func:`authenticated_channel` yields a channel that
Bearer-authenticates every call, so a client builds raw stubs from it directly.
"""

from airavata.auth.channel import BearerAuthInterceptor, authenticated_channel

__all__ = ["BearerAuthInterceptor", "authenticated_channel"]
