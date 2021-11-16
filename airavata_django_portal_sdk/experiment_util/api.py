import logging

from airavata.model.application.io.ttypes import DataType
from airavata.model.group.ttypes import ResourcePermissionType
from django.conf import settings

from airavata_django_portal_sdk import remoteapi
from airavata_django_portal_sdk.user_storage import api as user_storage

logger = logging.getLogger(__name__)


def launch(request, experiment_id):
    if remoteapi.is_remote_api_configured():
        resp = remoteapi.call(request,
                              "/experiments/{experiment_id}/launch/",
                              path_params={"experiment_id": experiment_id},
                              base_url="/api",
                              method="post")
        data = resp.json()
        if not data["success"]:
            logger.error(f"Failed to launch experiment {experiment_id}: {data['errorMessage']})")
            raise Exception(data["errorMessage"])
        return
    else:
        experiment = request.airavata_client.getExperiment(
            request.authz_token, experiment_id)
        _set_storage_id_and_data_dir(request, experiment)
        _move_tmp_input_file_uploads_to_data_dir(request, experiment)
        request.airavata_client.updateExperiment(
            request.authz_token, experiment_id, experiment)
        request.airavata_client.launchExperiment(
            request.authz_token, experiment_id, settings.GATEWAY_ID)


def clone(request, experiment_id):
    """Clone experiment and return the experiment id of the clone."""
    if remoteapi.is_remote_api_configured():
        resp = remoteapi.call(request,
                              "/experiments/{experiment_id}/clone/",
                              path_params={"experiment_id": experiment_id},
                              base_url="/api",
                              method="post")
        data = resp.json()
        return data['experimentId']
    else:
        # figure what project to clone into
        experiment = request.airavata_client.getExperiment(
            request.authz_token, experiment_id)
        project_id = _get_writeable_project(request, experiment)

        # clone experiment
        cloned_experiment_id = request.airavata_client.cloneExperiment(
            request.authz_token, experiment_id,
            "Clone of {}".format(experiment.experimentName), project_id)
        cloned_experiment = request.airavata_client.getExperiment(
            request.authz_token, cloned_experiment_id)

        # Create a copy of the experiment input files
        _copy_cloned_experiment_input_uris(request, cloned_experiment)

        # Null out experimentDataDir so a new one will get created at launch
        # time
        cloned_experiment.userConfigurationData.experimentDataDir = None
        request.airavata_client.updateExperiment(
            request.authz_token, cloned_experiment.experimentId, cloned_experiment
        )
        return cloned_experiment_id


def _set_storage_id_and_data_dir(request, experiment):
    # Storage ID
    experiment.userConfigurationData.storageId = user_storage.get_default_storage_resource_id(request)
    # Create experiment dir and set it on model
    if not experiment.userConfigurationData.experimentDataDir:
        project = request.airavata_client.getProject(
            request.authz_token, experiment.projectId)
        _, exp_dir = user_storage.create_user_dir(
            request,
            dir_names=(project.name, experiment.experimentName),
            create_unique=True)
        experiment.userConfigurationData.experimentDataDir = exp_dir
    else:
        # create_user_dir will also validate that absolute paths are
        # inside the user's storage directory
        _, exp_dir = user_storage.create_user_dir(
            request,
            path=experiment.userConfigurationData.experimentDataDir)
        experiment.userConfigurationData.experimentDataDir = exp_dir


def _move_tmp_input_file_uploads_to_data_dir(request, experiment):
    exp_data_dir = experiment.userConfigurationData.experimentDataDir
    for experiment_input in experiment.experimentInputs:
        if experiment_input.type == DataType.URI:
            if experiment_input.value:
                experiment_input.value = \
                    _move_if_tmp_input_file_upload(
                        request, experiment_input.value, exp_data_dir)
        elif experiment_input.type == DataType.URI_COLLECTION:
            data_product_uris = experiment_input.value.split(
                ",") if experiment_input.value else []
            moved_data_product_uris = []
            for data_product_uri in data_product_uris:
                moved_data_product_uris.append(
                    _move_if_tmp_input_file_upload(request, data_product_uri,
                                                   exp_data_dir))
            experiment_input.value = ",".join(moved_data_product_uris)


def _move_if_tmp_input_file_upload(
        request, data_product_uri, experiment_data_dir):
    """
    Conditionally moves tmp input file to data dir and returns new dp URI.
    """
    data_product = request.airavata_client.getDataProduct(
        request.authz_token, data_product_uri)
    if user_storage.is_input_file(
            request, data_product):
        moved_data_product = \
            user_storage.move(request, data_product=data_product, path=experiment_data_dir)
        return moved_data_product.productUri
    else:
        return data_product_uri


def _get_writeable_project(request, experiment):
    # figure what project to clone into:
    # 1) project of this experiment if writeable
    # 2) else, first writeable project
    project_id = experiment.projectId
    if _can_write(request, project_id):
        return project_id
    user_projects = request.airavata_client.getUserProjects(
        request.authz_token, settings.GATEWAY_ID, request.user.username, -1, 0)
    for user_project in user_projects:
        if _can_write(request, user_project.projectID):
            return user_project.projectID
    raise Exception(
        "Could not find writeable project for user {} in "
        "gateway {}".format(request.user.username, settings.GATEWAY_ID))


def _can_write(request, entity_id):
    return request.airavata_client.userHasAccess(
        request.authz_token, entity_id, ResourcePermissionType.WRITE)


def _copy_cloned_experiment_input_uris(request, cloned_experiment):
    # update the experimentInputs of type URI, copying input files into the
    # tmp input files directory of the data store
    for experiment_input in cloned_experiment.experimentInputs:
        # skip inputs without values
        if not experiment_input.value:
            continue
        if experiment_input.type == DataType.URI:
            cloned_data_product = _copy_experiment_input_uri(request,
                                                             experiment_input.value)
            if cloned_data_product is None:
                logger.warning("Setting cloned input {} to null".format(
                    experiment_input.name))
                experiment_input.value = None
            else:
                experiment_input.value = cloned_data_product.productUri
        elif experiment_input.type == DataType.URI_COLLECTION:
            data_product_uris = experiment_input.value.split(
                ",") if experiment_input.value else []
            cloned_data_product_uris = []
            for data_product_uri in data_product_uris:
                cloned_data_product = _copy_experiment_input_uri(request,
                                                                 data_product_uri)
                if cloned_data_product is None:
                    logger.warning(
                        "Omitting a cloned input value for {}".format(
                            experiment_input.name))
                else:
                    cloned_data_product_uris.append(
                        cloned_data_product.productUri)
            experiment_input.value = ",".join(cloned_data_product_uris)


def _copy_experiment_input_uri(request, data_product_uri):
    if user_storage.exists(request, data_product_uri=data_product_uri):
        return user_storage._copy_input_file(
            request, data_product_uri=data_product_uri)
    else:
        logger.warning("Could not find file for source data "
                       "product {}".format(data_product_uri))
        return None
