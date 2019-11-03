import logging
import os
import shutil

from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist, SuspiciousFileOperation
from django.core.files.move import file_move_safe
from django.core.files.storage import FileSystemStorage

logger = logging.getLogger(__name__)


def exists(username, path):
    """Check if file path exists in this data store."""
    try:
        return (_user_data_storage(username).exists(path) and
                os.path.isfile(path_(username, path)))
    except SuspiciousFileOperation as e:
        logger.warning("Invalid path for user {}: {}".format(username, str(e)))
        return False


def dir_exists(username, path):
    """Check if directory path exists in this data store."""
    try:
        return (_user_data_storage(username).exists(path) and
                os.path.isdir(path_(username, path)))
    except SuspiciousFileOperation as e:
        logger.warning("Invalid path for user {}: {}".format(username, str(e)))
        return False


def open(username, path):
    """Open path for user if it exists in this data store."""
    if exists(username, path):
        return _user_data_storage(username).open(path)
    else:
        raise ObjectDoesNotExist("File path does not exist: {}".format(path))


def save(username, path, file, name=None):
    """Save file to username/path in data store."""
    # file.name may be full path, so get just the name of the file
    file_name = name if name is not None else os.path.basename(file.name)
    user_data_storage = _user_data_storage(username)
    file_path = os.path.join(
        path, user_data_storage.get_valid_name(file_name))
    input_file_name = user_data_storage.save(file_path, file)
    input_file_fullpath = user_data_storage.path(input_file_name)
    return input_file_fullpath


def move(source_username, source_path, target_username, target_dir, file_name):
    source_full_path = path_(source_username, source_path)
    user_data_storage = _user_data_storage(target_username)
    # Make file_name a valid filename
    target_path = os.path.join(target_dir,
                               user_data_storage.get_valid_name(file_name))
    # Get available file path: if there is an existing file at target_path
    # create a uniquely named path
    target_path = user_data_storage.get_available_name(target_path)
    target_full_path = path_(target_username, target_path)
    file_move_safe(source_full_path, target_full_path)
    return target_full_path


def move_external(external_path, target_username, target_dir, file_name):
    user_data_storage = _user_data_storage(target_username)
    # Make file_name a valid filename
    target_path = os.path.join(target_dir,
                               user_data_storage.get_valid_name(file_name))
    # Get available file path: if there is an existing file at target_path
    # create a uniquely named path
    target_path = user_data_storage.get_available_name(target_path)
    if not dir_exists(target_username, target_dir):
        create_user_dir(target_username, target_dir)
    target_full_path = path_(target_username, target_path)
    file_move_safe(external_path, target_full_path)
    return target_full_path


def create_user_dir(username, path):
    user_data_storage = _user_data_storage(username)
    if not user_data_storage.exists(path):
        _makedirs(username, path)
    else:
        raise Exception(
            "Directory {} already exists".format(path))


def copy(source_username,
         source_path,
         target_username,
         target_path,
         name=None):
    """Copy a user file into target_path dir."""
    f = open(source_username, source_path)
    return save(target_username, target_path, f, name=name)


def delete(username, path):
    """Delete file in this data store."""
    if exists(username, path):
        user_data_storage = _user_data_storage(username)
        user_data_storage.delete(path)
    else:
        raise ObjectDoesNotExist("File path does not exist: {}".format(path))


def delete_dir(username, path):
    """Delete entire directory in this data store."""
    if dir_exists(username, path):
        user_path = path_(username, path)
        shutil.rmtree(user_path)
    else:
        raise ObjectDoesNotExist("File path does not exist: {}".format(path))


def get_experiment_dir(
        username,
        project_name=None,
        experiment_name=None,
        path=None):
    """Return an experiment directory (full path) for the given experiment."""
    user_experiment_data_storage = _user_data_storage(username)
    if path is None:
        proj_dir_name = user_experiment_data_storage.get_valid_name(
            project_name)
        # AIRAVATA-3245 Make project directory with correct permissions
        if not user_experiment_data_storage.exists(proj_dir_name):
            _makedirs(username, proj_dir_name)
        experiment_dir_name = os.path.join(
            proj_dir_name,
            user_experiment_data_storage.get_valid_name(experiment_name))
        # Since there may already be another experiment with the same name in
        # this project, we need to check for available name
        experiment_dir_name = user_experiment_data_storage.get_available_name(
            experiment_dir_name)
        experiment_dir = user_experiment_data_storage.path(experiment_dir_name)
    else:
        # path can be relative to the user's storage space or absolute (as long
        # as it is still inside the user's storage space)
        # if path is passed in, assumption is that it has already been created
        user_experiment_data_storage = _user_data_storage(username)
        experiment_dir = user_experiment_data_storage.path(path)
    if not user_experiment_data_storage.exists(experiment_dir):
        _makedirs(username, experiment_dir)
    return experiment_dir


def _makedirs(username, dir_path):
    user_experiment_data_storage = _user_data_storage(username)
    full_path = user_experiment_data_storage.path(dir_path)
    os.makedirs(full_path,
                mode=user_experiment_data_storage.directory_permissions_mode)
    # os.makedirs mode isn't always respected so need to chmod to be sure
    os.chmod(full_path,
             mode=user_experiment_data_storage.directory_permissions_mode)


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


def rel_path(username, file_path):
    full_path = path_(username, file_path)
    return os.path.relpath(full_path, path_(username, ""))


def path_(username, file_path):
    user_data_storage = _user_data_storage(username)
    return user_data_storage.path(file_path)


def _user_data_storage(username):
    return FileSystemStorage(
        location=os.path.join(settings.GATEWAY_DATA_STORE_DIR, username))


# from https://stackoverflow.com/a/1392549
def _get_dir_size(start_path='.'):
    total_size = 0
    for dirpath, dirnames, filenames in os.walk(start_path):
        for f in filenames:
            fp = os.path.join(dirpath, f)
            # Check for broken symlinks (.exists return False for broken
            # symlinks)
            if os.path.exists(fp):
                total_size += os.path.getsize(fp)
    return total_size
