#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import logging
from pathlib import Path
from .sftp import SFTPConnector

import jwt
from airavata.model.security.ttypes import AuthzToken
from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil
from airavata_sdk.clients.utils.data_model_creation_util import DataModelCreationUtil
from airavata_sdk.transport.settings import ExperimentSettings, GatewaySettings

logger = logging.getLogger("airavata_sdk.clients")
logger.setLevel(logging.INFO)


class AiravataOperator:

  def __init__(self, access_token: str, config_file: str = "settings.ini"):
    # store variables
    self.config_file = config_file
    self.access_token = access_token
    # load api server settings and create client
    self.api_server_client = APIServerClient(self.config_file)
    # load gateway settings
    self.gateway_conf = GatewaySettings(self.config_file)
    gateway_id = self.gateway_conf.GATEWAY_ID
    # load experiment settings
    self.experiment_conf = ExperimentSettings(self.config_file)
    self.airavata_token = self.__airavata_token__(self.access_token, gateway_id)
    self.api_util = APIServerClientUtil(self.config_file, username=self.user_id, password="", gateway_id=gateway_id, access_token=self.access_token)

  def __airavata_token__(self, access_token, gateway_id):
    """
    Decode access token (string) and create AuthzToken (object)

    """
    decode = jwt.decode(access_token, options={"verify_signature": False})
    self.user_id = str(decode["preferred_username"])
    claimsMap = {"userName": self.user_id, "gatewayID": gateway_id}
    return AuthzToken(accessToken=self.access_token, claimsMap=claimsMap)

  def get_experiment(self, experiment_id: str):
    """
    Get experiment by id

    """
    return self.api_server_client.get_experiment(self.airavata_token, experiment_id)

  def get_accessible_apps(self, gateway_id: str | None = None):
    """
    Get all applications available in the gateway

    """
    # use defaults for missing values
    gateway_id = gateway_id or self.gateway_conf.GATEWAY_ID
    # logic
    app_interfaces = self.api_server_client.get_all_application_interfaces(self.airavata_token, gateway_id)
    return app_interfaces

  def get_preferred_storage(self, gateway_id: str | None = None, storage_name: str | None = None):
    """
    Get preferred storage resource

    """
    # use defaults for missing values
    gateway_id = gateway_id or self.gateway_conf.GATEWAY_ID
    storage_name = storage_name or self.experiment_conf.STORAGE_RESOURCE_HOST
    # logic
    storage_id = self.api_util.get_storage_resource_id(storage_name)
    return self.api_server_client.get_gateway_storage_preference(self.airavata_token, gateway_id, storage_id)

  def get_storage(self, storage_name: str | None = None) -> any:  # type: ignore
    """
    Get storage resource by name

    """
    # use defaults for missing values
    storage_name = storage_name or self.experiment_conf.STORAGE_RESOURCE_HOST
    # logic
    storage_id = self.api_util.get_storage_resource_id(storage_name)
    storage = self.api_util.api_server_client.get_storage_resource(self.airavata_token, storage_id)
    return storage

  def get_group_resource_profile(self, grp_name: str | None = None):
    """
    Get group resource profile by name

    """
    # use defaults for missing values
    grp_name = grp_name or self.experiment_conf.GROUP_RESOURCE_PROFILE_NAME
    # logic
    grp_id = self.api_util.get_group_resource_profile_id(grp_name)
    grp = self.api_util.api_server_client.get_group_resource_profile(self.airavata_token, grp_id)
    return grp

  def get_compatible_deployments(self, app_interface_id: str, grp_name: str | None = None):
    """
    Get compatible deployments for an application interface and group resource profile

    """
    # use defaults for missing values
    grp_name = grp_name or self.experiment_conf.GROUP_RESOURCE_PROFILE_NAME
    # logic
    grp_id = self.api_util.get_group_resource_profile_id(grp_name)
    deployments = self.api_server_client.get_application_deployments_for_app_module_and_group_resource_profile(self.airavata_token, app_interface_id, grp_id)
    return deployments

  def get_app_interface_id(self, app_name: str, gateway_id: str | None = None):
    """
    Get application interface id by name

    """
    self.api_util.gateway_id = str(gateway_id or self.gateway_conf.GATEWAY_ID)
    return self.api_util.get_execution_id(app_name)

  def get_application_inputs(self, app_interface_id: str) -> list:
    """
    Get application inputs by id

    """
    return list(self.api_server_client.get_application_inputs(self.airavata_token, app_interface_id))  # type: ignore

  def get_compute_resources_by_ids(self, resource_ids: list[str]):
    """
    Get compute resources by ids

    """
    return [self.api_server_client.get_compute_resource(self.airavata_token, resource_id) for resource_id in resource_ids]

  def make_experiment_dir(self, storage_resource, project_name: str, experiment_name: str) -> str:
    """
    Make experiment directory on storage resource, and return the remote path

    Return Path: /{project_name}/{experiment_name}

    """
    host = storage_resource.hostName
    port = self.experiment_conf.SFTP_PORT
    sftp_connector = SFTPConnector(host=host, port=port, username=self.user_id, password=self.access_token)
    remote_path = sftp_connector.make_experiment_dir(project_name, experiment_name)
    logger.info("Experiment directory created at %s", remote_path)
    return remote_path

  def upload_files(self, storage_resource, files: list[Path], exp_dir: str) -> None:
    """
    Upload input files to storage resource, and return the remote path

    Return Path: /{project_name}/{experiment_name}

    """
    host = storage_resource.hostName
    port = self.experiment_conf.SFTP_PORT
    sftp_connector = SFTPConnector(host=host, port=port, username=self.user_id, password=self.access_token)
    sftp_connector.upload_files(files, exp_dir)
    logger.info("Input files uploaded to %s", exp_dir)

  def launch_experiment(
      self,
      experiment_name: str,
      app_name: str,
      computation_resource_name: str,
      inputs: dict[str, str | int | float | list[str]],
      *,
      gateway_id: str | None = None,
      queue_name: str | None = None,
      grp_name: str | None = None,
      sr_host: str | None = None,
      project_name: str | None = None,
      node_count: int | None = None,
      cpu_count: int | None = None,
      walltime: int | None = None,
      auto_schedule: bool = False,
  ) -> str:
    """
    Launch an experiment and return its id

    """
    # preprocess args (str)
    print("[AV] Preprocessing args...")
    gateway_id = str(gateway_id or self.gateway_conf.GATEWAY_ID)
    queue_name = str(queue_name or self.experiment_conf.QUEUE_NAME)
    grp_name = str(grp_name or self.experiment_conf.GROUP_RESOURCE_PROFILE_NAME)
    sr_host = str(sr_host or self.experiment_conf.STORAGE_RESOURCE_HOST)
    project_name = str(project_name or self.experiment_conf.PROJECT_NAME)
    mount_point = Path(self.gateway_conf.GATEWAY_DATA_STORE_DIR) / self.user_id

    # preprocess args (int)
    node_count = int(node_count or self.experiment_conf.NODE_COUNT or "1")
    cpu_count = int(cpu_count or self.experiment_conf.TOTAL_CPU_COUNT or "1")
    walltime = int(walltime or self.experiment_conf.WALL_TIME_LIMIT or "30")

    # validate args (str)
    print("[AV] Validating args...")
    assert len(experiment_name) > 0
    assert len(app_name) > 0
    assert len(computation_resource_name) > 0
    assert len(inputs) > 0
    assert len(gateway_id) > 0
    assert len(queue_name) > 0
    assert len(grp_name) > 0
    assert len(sr_host) > 0
    assert len(project_name) > 0
    assert len(mount_point.as_posix()) > 0

    # validate args (int)
    assert node_count > 0
    assert cpu_count > 0
    assert walltime > 0

    # setup runtime params
    print("[AV] Setting up runtime params...")
    storage = self.get_storage(sr_host)
    queue_name = queue_name or self.experiment_conf.QUEUE_NAME
    node_count = int(node_count or self.experiment_conf.NODE_COUNT or "1")
    cpu_count = int(cpu_count or self.experiment_conf.TOTAL_CPU_COUNT or "1")
    walltime = int(walltime or self.experiment_conf.WALL_TIME_LIMIT or "01:00:00")
    sr_id = storage.storageResourceId

    # setup application interface
    print("[AV] Setting up application interface...")
    app_interface_id = self.get_app_interface_id(app_name)
    assert app_interface_id is not None

    # setup experiment
    print("[AV] Setting up experiment...")
    data_model_util = DataModelCreationUtil(
        self.config_file,
        username=self.user_id,
        password=None,
        gateway_id=gateway_id,
        access_token=self.access_token,
    )
    experiment = data_model_util.get_experiment_data_model_for_single_application(
        experiment_name=experiment_name,
        application_name=app_name,
        project_name=project_name,
        description=experiment_name,
    )

    # setup experiment directory
    print("[AV] Setting up experiment directory...")
    exp_dir = self.make_experiment_dir(
        storage_resource=storage,
        project_name=project_name,
        experiment_name=experiment_name,
    )
    abs_path = (mount_point / exp_dir.lstrip("/")).as_posix().rstrip("/") + "/"
    print("[AV] exp_dir:", exp_dir)
    print("[AV] abs_path:", abs_path)

    experiment = data_model_util.configure_computation_resource_scheduling(
        experiment_model=experiment,
        computation_resource_name=computation_resource_name,
        group_resource_profile_name=grp_name,
        storageId=sr_id,
        node_count=node_count,
        total_cpu_count=cpu_count,
        wall_time_limit=walltime,
        queue_name=queue_name,
        experiment_dir_path=abs_path,
        auto_schedule=auto_schedule,
    )

    # set up file inputs
    print("[AV] Setting up file inputs...")

    def register_input_file(file: Path) -> str:
      return str(data_model_util.register_input_file(file.name, sr_host, sr_id, file.name, abs_path))

    # setup experiment inputs
    files_to_upload = list[Path]()
    file_inputs = dict[str, str | list[str]]()
    data_inputs = dict[str, str | list[str] | int | float]()
    for key, value in inputs.items():

      if isinstance(value, str) and Path(value).is_file():
        file = Path(value)
        files_to_upload.append(file)
        file_inputs[key] = register_input_file(file)

      elif isinstance(value, list) and all([isinstance(v, str) and Path(v).is_file() for v in value]):
        files = [*map(Path, value)]
        files_to_upload.extend(files)
        file_inputs[key] = [*map(register_input_file, files)]

      else:
        data_inputs[key] = value

    # configure file inputs for experiment
    print("[AV] Uploading file inputs for experiment...")
    self.upload_files(storage, files_to_upload, exp_dir)

    # configure experiment inputs
    experiment_inputs = []
    for exp_input in self.api_server_client.get_application_inputs(self.airavata_token, app_interface_id):  # type: ignore
      if exp_input.type < 3 and exp_input.name in data_inputs:
        value = data_inputs[exp_input.name]
        exp_input.value = repr(value)
      elif exp_input.type == 3 and exp_input.name in file_inputs:
        exp_input.value = file_inputs[exp_input.name]
      elif exp_input.type == 4 and exp_input.name in file_inputs:
        exp_input.value = ','.join(file_inputs[exp_input.name])
      experiment_inputs.append(exp_input)
    experiment.experimentInputs = experiment_inputs

    # configure experiment outputs
    outputs = self.api_server_client.get_application_outputs(self.airavata_token, app_interface_id)
    experiment.experimentOutputs = outputs

    # create experiment
    ex_id = self.api_server_client.create_experiment(self.airavata_token, gateway_id, experiment)

    # TODO agent_id generate and send as input parameter
    # connect to connection service after this point, and route all file-related requests through it
    # later build a ssh adapter for ls type tasks

    # launch experiment
    self.api_server_client.launch_experiment(self.airavata_token, ex_id, gateway_id)

    return str(ex_id)

  def get_experiment_status(self, experiment_id):
    status = self.api_server_client.get_experiment_status(
        self.airavata_token, experiment_id)
    return status
  
  def stop_experiment(self, experiment_id):
    status = self.api_server_client.terminate_experiment(
        self.airavata_token, experiment_id, self.gateway_conf.GATEWAY_ID)
    return status
