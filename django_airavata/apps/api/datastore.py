import logging
import os
import shutil

from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist, SuspiciousFileOperation
from django.core.files.storage import FileSystemStorage, Storage

experiment_data_storage = FileSystemStorage(
    location=settings.GATEWAY_DATA_STORE_DIR)
logger = logging.getLogger(__name__)


def exists(username, path):
    """Check if replica for data product exists in this data store."""
    try:
        return _user_data_storage(username).exists(path)
    except SuspiciousFileOperation as e:
        logger.warning("Invalid path for user {}: {}".format(username, str(e)))
        return False


def open(username, path):
    """Open path for user if it exists in this data store."""
    if exists(username, path):
        return _user_data_storage(username).open(path)
    else:
        raise ObjectDoesNotExist("File path does not exist: {}".format(path))


def save(username, path, file):
    """Save file to username/project name/experiment_name in data store."""
    # file.name may be full path, so get just the name of the file
    file_name = os.path.basename(file.name)
    user_data_storage = _user_data_storage(username)
    file_path = os.path.join(
        path, user_data_storage.get_valid_name(file_name))
    input_file_name = user_data_storage.save(file_path, file)
    input_file_fullpath = user_data_storage.path(input_file_name)
    return input_file_fullpath


def create_user_dir(username, path):
    user_data_storage = _user_data_storage(username)
    if not user_data_storage.exists(path):
        os.makedirs(user_data_storage.path(path))
    else:
        raise Exception(
            "Directory {} already exists".format(path))


def copy(username, source_path, target_path):
    """Copy a user file into target_path dir."""
    f = open(username, source_path)
    return save(username, target_path, f)


def delete(username, path):
    """Delete file in this data store."""
    if exists(username, path):
        user_data_storage = _user_data_storage(username)
        user_data_storage.delete(path)
    else:
        raise ObjectDoesNotExist("File path does not exist: {}".format(path))


def delete_dir(username, path):
    """Delete entire directory in this data store."""
    if exists(username, path):
        user_path = path_(username, path)
        shutil.rmtree(user_path)
    else:
        raise ObjectDoesNotExist("File path does not exist: {}".format(path))


# TODO: update this to just return an available experiment directory name
def get_experiment_dir(
        username,
        project_name=None,
        experiment_name=None,
        path=None):
    """Return an experiment directory (full path) for the given experiment."""
    user_experiment_data_storage = _user_data_storage(username)
    if path is None:
        experiment_dir_name = os.path.join(
            user_experiment_data_storage.get_valid_name(project_name),
            user_experiment_data_storage.get_valid_name(experiment_name))
        experiment_dir = user_experiment_data_storage.path(experiment_dir_name)
    else:
        # path can be relative to the user's storage space or absolute (as long
        # as it is still inside the user's storage space)
        user_experiment_data_storage = _user_data_storage(username)
        experiment_dir = user_experiment_data_storage.path(path)
    if not user_experiment_data_storage.exists(experiment_dir):
        os.makedirs(experiment_dir,
                    mode=user_experiment_data_storage.directory_permissions_mode)
        # os.mkdir mode isn't always respected so need to chmod to be sure
        os.chmod(experiment_dir,
                 mode=user_experiment_data_storage.directory_permissions_mode)
    return experiment_dir


def user_file_exists(username, file_path):
    """Check if file path exists in user's data storage space."""
    try:
        # file_path can be relative or absolute
        user_experiment_data_storage = _user_data_storage(username)
        return user_experiment_data_storage.exists(file_path)
    except SuspiciousFileOperation as e:
        logger.warning(
            "File does not exist for user {} at file path {}".format(
                username, file_path))
        return False


def list_user_dir(username, file_path):
    logger.debug("file_path={}".format(file_path))
    user_data_storage = _user_data_storage(username)
    return user_data_storage.listdir(file_path)


def get_created_time(username, file_path):
    user_data_storage = _user_data_storage(username)
    return user_data_storage.get_created_time(file_path)


def size(username, file_path):
    user_data_storage = _user_data_storage(username)
    full_path = path_(username, file_path)
    if os.path.isdir(full_path):
        return _get_dir_size(full_path)
    else:
        return user_data_storage.size(file_path)


def path(username, file_path):
    return path_(username, file_path)


def path_(username, file_path):
    user_data_storage = _user_data_storage(username)
    return user_data_storage.path(file_path)


def _user_dir_name(username):
    return Storage().get_valid_name(username)


def _user_data_storage(username):
    return FileSystemStorage(
        location=os.path.join(settings.GATEWAY_DATA_STORE_DIR,
                              _user_dir_name(username)))


# from https://stackoverflow.com/a/1392549
def _get_dir_size(start_path='.'):
    total_size = 0
    for dirpath, dirnames, filenames in os.walk(start_path):
        for f in filenames:
            fp = os.path.join(dirpath, f)
            total_size += os.path.getsize(fp)
    return total_size
