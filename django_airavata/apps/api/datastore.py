import logging
import os
from urllib.parse import urlparse

from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist, SuspiciousFileOperation
from django.core.files.storage import FileSystemStorage

from airavata.model.data.replica.ttypes import (
    DataProductModel,
    DataProductType,
    DataReplicaLocationModel,
    ReplicaLocationCategory,
    ReplicaPersistentType
)

experiment_data_storage = FileSystemStorage(
    location=settings.GATEWAY_DATA_STORE_DIR)
logger = logging.getLogger(__name__)


def exists(data_product):
    """Check if replica for data product exists in this data store."""
    filepath = _get_replica_filepath(data_product)
    try:
        return experiment_data_storage.exists(filepath) if filepath else False
    except SuspiciousFileOperation as e:
        logger.warning("Unable to find file at {} for data product uri {}"
                       .format(filepath, data_product.productUri))
        return False


def open(data_product):
    """Open replica for data product if it exists in this data store."""
    if exists(data_product):
        filepath = _get_replica_filepath(data_product)
        return experiment_data_storage.open(filepath)
    else:
        raise ObjectDoesNotExist("Replica file does not exist")


def save(username, project_name, experiment_name, file):
    """Save file to username/project name/experiment_name in data store."""
    exp_dir = os.path.join(
        experiment_data_storage.get_valid_name(username),
        experiment_data_storage.get_valid_name(project_name),
        experiment_data_storage.get_valid_name(experiment_name))
    # file.name may be full path, so get just the name of the file
    file_name = os.path.basename(file.name)
    file_path = os.path.join(
        exp_dir,
        experiment_data_storage.get_valid_name(file_name))
    input_file_name = experiment_data_storage.save(file_path, file)
    input_file_fullpath = experiment_data_storage.path(input_file_name)
    # Create DataProductModel instance with DataReplicaLocationModel
    data_product = DataProductModel()
    data_product.gatewayId = settings.GATEWAY_ID
    data_product.ownerName = username
    data_product.productName = file_name
    data_product.dataProductType = DataProductType.FILE
    data_replica_location = DataReplicaLocationModel()
    data_replica_location.storageResourceId = \
        settings.GATEWAY_DATA_STORE_RESOURCE_ID
    data_replica_location.replicaName = \
        "{} gateway data store copy".format(file_name)
    data_replica_location.replicaLocationCategory = \
        ReplicaLocationCategory.GATEWAY_DATA_STORE
    data_replica_location.replicaPersistentType = \
        ReplicaPersistentType.TRANSIENT
    data_replica_location.filePath = \
        "file://{}:{}".format(settings.GATEWAY_DATA_STORE_HOSTNAME,
                              input_file_fullpath)
    data_product.replicaLocations = [data_replica_location]
    return data_product


def copy(username, project_name, experiment_name, data_product):
    """Copy a data product into username/project_name/experiment_name dir."""
    f = open(data_product)
    return save(username, project_name, experiment_name, f)


def delete(data_product):
    """Delete replica for data product in this data store."""
    if exists(data_product):
        filepath = _get_replica_filepath(data_product)
        try:
            experiment_data_storage.delete(filepath)
        except Exception as e:
            logger.error("Unable to delete file {} for data product uri {}"
                         .format(filepath, data_product.productUri))
            raise
    else:
        raise ObjectDoesNotExist("Replica file does not exist")


def get_experiment_dir(username, project_name, experiment_name):
    """Return an experiment directory (full path) for the given experiment."""
    experiment_dir_name = os.path.join(
        experiment_data_storage.get_valid_name(username),
        experiment_data_storage.get_valid_name(project_name),
        experiment_data_storage.get_valid_name(experiment_name))
    experiment_dir = experiment_data_storage.path(experiment_dir_name)
    if not experiment_data_storage.exists(experiment_dir):
        os.makedirs(experiment_dir,
                    mode=experiment_data_storage.directory_permissions_mode)
        # os.mkdir mode isn't always respected so need to chmod to be sure
        os.chmod(experiment_dir,
                 mode=experiment_data_storage.directory_permissions_mode)
    return experiment_dir


def _get_replica_filepath(data_product):
    replica_filepaths = [rep.filePath
                         for rep in data_product.replicaLocations
                         if rep.replicaLocationCategory ==
                         ReplicaLocationCategory.GATEWAY_DATA_STORE]
    replica_filepath = (replica_filepaths[0]
                        if len(replica_filepaths) > 0 else None)
    if replica_filepath:
        return urlparse(replica_filepath).path
    return None
