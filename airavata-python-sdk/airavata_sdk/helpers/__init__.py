"""Framework-agnostic helpers relocated from the Django portal SDK.

These modules carry behavior that used to live in
``airavata_django_portal_sdk`` but contain no Django/DRF/Thrift dependencies.
All backend access is routed through the gRPC facades exposed by
:class:`airavata_sdk.client.AiravataClient` (``client.research``,
``client.storage``, ``client.sharing``).
"""
