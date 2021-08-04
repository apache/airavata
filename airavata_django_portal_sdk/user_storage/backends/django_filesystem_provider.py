import logging
import os
import shutil

from django.core.exceptions import ObjectDoesNotExist, SuspiciousFileOperation
from django.core.files.storage import FileSystemStorage

from .base import UserStorageProvider

logger = logging.getLogger(__name__)


class DjangoFileSystemProvider(UserStorageProvider):
    def __init__(self, authz_token, resource_id, context=None, directory=None, **kwargs):
        super().__init__(authz_token, resource_id, context=context, **kwargs)
        self.directory = directory

    def save(self, path, file, name=None, content_type=None):
        full_path = self.datastore.save(path, file, name=name)
        return self.resource_id, full_path

    def open(self, resource_path):
        return self.datastore.open(resource_path)

    def exists(self, resource_path):
        return self.datastore.exists(resource_path)

    def is_file(self, resource_path):
        return self.datastore.file_exists(resource_path)

    def is_dir(self, resource_path):
        return self.datastore.dir_exists(resource_path)

    def get_metadata(self, resource_path):
        # TODO: also return an isDir boolean flag?
        datastore = self.datastore
        if datastore.dir_exists(resource_path):
            directories, files = datastore.list_user_dir(
                resource_path)
            directories_data = []
            for d in directories:
                dpath = os.path.join(resource_path, d)
                created_time = datastore.get_created_time(dpath)
                size = datastore.size(dpath)
                directories_data.append(
                    {
                        "name": d,
                        "path": datastore.rel_path(dpath),
                        "resource_path": datastore.path(dpath),
                        "created_time": created_time,
                        "size": size,
                    }
                )
            files_data = []
            for f in files:
                user_rel_path = os.path.join(resource_path, f)
                if not datastore.exists(user_rel_path):
                    logger.warning(f"listdir skipping {user_rel_path}, "
                                   "does not exist (broken symlink?)")
                    continue
                created_time = datastore.get_created_time(user_rel_path)
                size = datastore.size(user_rel_path)
                full_path = datastore.path(user_rel_path)
                files_data.append(
                    {
                        "name": f,
                        "path": datastore.rel_path(full_path),
                        "resource_path": full_path,
                        "created_time": created_time,
                        "size": size,
                    }
                )
            return directories_data, files_data
        elif datastore.exists(resource_path):

            created_time = datastore.get_created_time(resource_path)
            size = datastore.size(resource_path)
            full_path = datastore.path(resource_path)
            return [], [
                {
                    "name": os.path.basename(resource_path),
                    "path": datastore.rel_path(full_path),
                    "resource_path": full_path,
                    "created_time": created_time,
                    "size": size,
                }
            ]
        else:
            raise ObjectDoesNotExist(f"User storage path does not exist {resource_path}")

    def delete(self, resource_path):
        if self.datastore.file_exists(resource_path):
            self.datastore.delete(resource_path)
        elif self.datastore.dir_exists(resource_path):
            self.datastore.delete_dir(resource_path)
        else:
            raise ObjectDoesNotExist(f"User resource_path does not exist {resource_path}")

    def update(self, resource_path, file):
        full_path = self.datastore.path(resource_path)
        with open(full_path, 'w') as f:
            f.write(file.read())

    def create_dirs(self, resource_path, dir_names=[], create_unique=False):
        datastore = self.datastore
        # Special case: handle creating user's home directory
        if resource_path == '' and not datastore.exists(''):
            datastore.create_user_dir('')
            return self.resource_id, datastore.path(resource_path)
        if not datastore.exists(resource_path):
            raise ObjectDoesNotExist(f"User resource_path does not exist {resource_path}")
        valid_dir_names = []
        for dir_name in dir_names:
            valid_dir_names.append(datastore.get_valid_name(dir_name))
        final_path = os.path.join(resource_path, *valid_dir_names)
        if datastore.exists(final_path) and not create_unique:
            raise Exception(f"Directory {final_path} already exists")
        # Make sure path is unique if it already exists
        final_path = datastore.get_available_name(final_path)
        datastore.create_user_dir(final_path)
        return self.resource_id, datastore.path(final_path)

    def create_symlink(self, source_path, dest_path):
        datastore = self.datastore
        if (datastore.symlink_exists(dest_path) and
                os.path.realpath(source_path) == os.path.realpath(datastore.path(dest_path))):
            logger.debug(f"symlink at {dest_path} already points to {source_path}")
            return
        elif datastore.exists(dest_path):
            raise Exception(f"{dest_path} already exists, but isn't the expected symlink")
        else:
            datastore.create_symlink(source_path, dest_path)

    @property
    def datastore(self):
        directory = os.path.join(self.directory, self.username)
        owner_username = self.context.get('owner_username')
        # When the current user isn't the owner, set the directory based on the owner's username
        if owner_username:
            directory = os.path.join(self.directory, owner_username)
        return _Datastore(directory=directory)


class _Datastore:
    """Internal datastore abstraction."""

    def __init__(self, directory=None):
        self.directory = directory
        self.storage = self._user_data_storage(self.directory)

    def exists(self, path):
        """Check if path exists in this data store."""
        try:
            return self.storage.exists(path)
        except SuspiciousFileOperation as e:
            logger.warning(f"Invalid path: {e}")
            return False

    def file_exists(self, path):
        """Check if file path exists in this data store."""
        try:
            return self.storage.exists(path) and os.path.isfile(self.path(path))
        except SuspiciousFileOperation as e:
            logger.warning(f"Invalid path: {e}")
            return False

    def dir_exists(self, path):
        """Check if directory path exists in this data store."""
        logger.debug(f"dir_exists: {path}, {self.path(path)}")
        try:
            return self.storage.exists(path) and os.path.isdir(self.path(path))
        except SuspiciousFileOperation as e:
            logger.warning(f"Invalid path: {e}")
            return False

    def symlink_exists(self, path):
        """Check if symlink path exists in this data store."""
        logger.debug(f"symlink_exists: {path}, {self.path(path)}")
        try:
            return self.storage.exists(path) and os.path.islink(self.path(path))
        except SuspiciousFileOperation as e:
            logger.warning(f"Invalid path: {e}")
            return False

    def open(self, path):
        """Open path for user if it exists in this data store."""
        if self.exists(path):
            return self.storage.open(path)
        else:
            raise ObjectDoesNotExist(
                "File path does not exist: {}".format(path))

    def save(self, path, file, name=None):
        """Save file to username/path in data store."""
        # file.name may be full path, so get just the name of the file
        file_name = name if name is not None else os.path.basename(file.name)
        user_data_storage = self.storage
        file_path = os.path.join(
            path, user_data_storage.get_valid_name(file_name))
        input_file_name = user_data_storage.save(file_path, file)
        input_file_fullpath = user_data_storage.path(input_file_name)
        return input_file_fullpath

    def create_user_dir(self, path):
        user_data_storage = self.storage
        if not user_data_storage.exists(path):
            self._makedirs(path)
        else:
            raise Exception("Directory {} already exists".format(path))

    def create_symlink(self, source_path, dest_path):
        user_data_storage = self.storage
        full_path = user_data_storage.path(dest_path)
        os.symlink(source_path, full_path)
        return full_path

    def delete(self, path):
        """Delete file in this data store."""
        if self.file_exists(path):
            user_data_storage = self.storage
            user_data_storage.delete(path)
        else:
            raise ObjectDoesNotExist(
                "File path does not exist: {}".format(path))

    def delete_dir(self, path):
        """Delete entire directory in this data store."""
        if self.symlink_exists(path):
            user_path = self.path(path)
            os.unlink(user_path)
        elif self.dir_exists(path):
            user_path = self.path(path)
            shutil.rmtree(user_path)
        else:
            raise ObjectDoesNotExist(
                "File path does not exist: {}".format(path))

    def get_experiment_dir(
        self, project_name=None, experiment_name=None, path=None
    ):
        """Return an experiment directory (full path) for the given experiment."""
        user_experiment_data_storage = self.storage
        if path is None:
            proj_dir_name = user_experiment_data_storage.get_valid_name(
                project_name)
            # AIRAVATA-3245 Make project directory with correct permissions
            if not user_experiment_data_storage.exists(proj_dir_name):
                self._makedirs(proj_dir_name)
            experiment_dir_name = os.path.join(
                proj_dir_name,
                user_experiment_data_storage.get_valid_name(experiment_name),
            )
            # Since there may already be another experiment with the same name in
            # this project, we need to check for available name
            experiment_dir_name = user_experiment_data_storage.get_available_name(
                experiment_dir_name)
            experiment_dir = user_experiment_data_storage.path(
                experiment_dir_name)
        else:
            # path can be relative to the user's storage space or absolute (as long
            # as it is still inside the user's storage space)
            # if path is passed in, assumption is that it has already been
            # created
            user_experiment_data_storage = self.storage
            experiment_dir = user_experiment_data_storage.path(path)
        if not user_experiment_data_storage.exists(experiment_dir):
            self._makedirs(experiment_dir)
        return experiment_dir

    def get_valid_name(self, name):
        return self.storage.get_valid_name(name)

    def get_available_name(self, name):
        return self.storage.get_available_name(name)

    def _makedirs(self, dir_path):
        user_experiment_data_storage = self.storage
        full_path = user_experiment_data_storage.path(dir_path)
        os.makedirs(
            full_path,
            mode=user_experiment_data_storage.directory_permissions_mode)
        # os.makedirs mode isn't always respected so need to chmod to be sure
        os.chmod(
            full_path,
            mode=user_experiment_data_storage.directory_permissions_mode)

    def list_user_dir(self, file_path):
        logger.debug("file_path={}".format(file_path))
        user_data_storage = self.storage
        return user_data_storage.listdir(file_path)

    def get_created_time(self, file_path):
        user_data_storage = self.storage
        return user_data_storage.get_created_time(file_path)

    def size(self, file_path):
        user_data_storage = self.storage
        full_path = self.path(file_path)
        if os.path.isdir(full_path):
            return self._get_dir_size(full_path)
        else:
            return user_data_storage.size(file_path)

    def path(self, file_path):
        user_data_storage = self.storage
        return user_data_storage.path(file_path)

    def rel_path(self, file_path):
        full_path = self.path(file_path)
        return os.path.relpath(full_path, self.path(""))

    def _user_data_storage(self, directory):
        return FileSystemStorage(location=directory)

    # from https://stackoverflow.com/a/1392549
    def _get_dir_size(self, start_path="."):
        total_size = 0
        for dirpath, dirnames, filenames in os.walk(start_path):
            for f in filenames:
                fp = os.path.join(dirpath, f)
                # Check for broken symlinks (.exists return False for broken
                # symlinks)
                if os.path.exists(fp):
                    total_size += os.path.getsize(fp)
        return total_size
