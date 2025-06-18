from .django_filesystem_provider import DjangoFileSystemProvider

try:
    # Try to import MFTUserStorageProvider, but dependencies may not be loaded
    # so the following might fail
    from .mft_provider import MFTUserStorageProvider
except Exception:
    pass

__all__ = ['DjangoFileSystemProvider', 'MFTUserStorageProvider']
