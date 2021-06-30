
class ProvidesDownloadUrl:
    """Mixin for UserStorageProvider that provides download url."""

    def get_download_url(self, resource_path):
        raise NotImplementedError()


class ProvidesUploadUrl:
    """Mixin for UserStorageProvider that provides upload url."""

    def get_upload_url(self, resource_path):
        raise NotImplementedError()


class UserStorageProvider:
    def __init__(self, authz_token, resource_id, context=None, **kwargs):
        self.authz_token = authz_token
        self.resource_id = resource_id
        # TODO probably don't need context for passing 'request'
        self.context = context

    # TODO remove content_type
    def save(self, resource_path, file, name=None, content_type=None):
        """
        Return a tuple of storage resource id and path, if any, to file.
        """
        raise NotImplementedError()

    def open(self, resource_path):
        raise NotImplementedError()

    def exists(self, resource_path):
        raise NotImplementedError()

    def is_file(self, resource_path):
        # TODO: is this needed if we have get_metadata?
        raise NotImplementedError()

    def is_dir(self, resource_path):
        # TODO: is this needed if we have get_metadata?
        raise NotImplementedError()

    def get_metadata(self, resource_path):
        """
        Return a tuple of two sequences: directories and files for the given
        resource_path. If the resource_path represents a file, then the
        directories sequence should be empty and the files sequence will only
        have the one file.
        """
        raise NotImplementedError()

    def delete(self, resource_path):
        raise NotImplementedError()

    def update(self, resource_path, file):
        raise NotImplementedError()

    def create_dirs(self, resource_path, dir_names=[], create_unique=False):
        """
        Create one or more named subdirectories inside the resource_path.
        resource_path must exist. dir_names will potentially be normalized as
        needed. The intermediate directories may already exist, but if the
        final directory already exists, this method will raise an Exception,
        unless create_unique is True in which the name will be modified until
        a unique directory name is found.
        """
        raise NotImplementedError()

    def create_symlink(self, source_path, dest_path):
        """
        Create a symlink at dest_path in user's storage that points to
        source_path, a filesystem path on the storage resource.
        """
        raise NotImplementedError()

    @property
    def username(self):
        return self.authz_token.claimsMap['userName']

    @property
    def access_token(self):
        return self.authz_token.accessToken
