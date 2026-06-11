"""Framework-agnostic resource helpers relocated from the Django portal SDK.

No Django/DRF/Thrift dependencies — all backend access goes through the gRPC
facades on :class:`airavata_sdk.client.AiravataClient` (``client.research``,
``client.storage``, ``client.sharing``, ...).
"""
