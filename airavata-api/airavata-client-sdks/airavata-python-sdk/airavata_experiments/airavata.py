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
from typing import Literal, NamedTuple
from .sftp import SFTPConnector
import warnings

import jwt
from airavata.model.security.ttypes import AuthzToken
from airavata.model.experiment.ttypes import ExperimentModel, ExperimentType, UserConfigurationDataModel
from airavata.model.scheduling.ttypes import ComputationalResourceSchedulingModel
from airavata.model.data.replica.ttypes import DataProductModel, DataProductType, DataReplicaLocationModel, ReplicaLocationCategory

from airavata_sdk.clients.api_server_client import APIServerClient

warnings.filterwarnings("ignore", category=DeprecationWarning)
logger = logging.getLogger("airavata_sdk.clients")
logger.setLevel(logging.INFO)

LaunchState = NamedTuple("LaunchState", [
  ("experiment_id", str),
  ("mount_point", Path),
  ("experiment_dir", str),
  ("sr_host", str),
])

class Settings:

  def __init__(self, config_path: str) -> None:
    
    import configparser
    config = configparser.ConfigParser()
    config.read(config_path)

    # api server client settings
    self.API_SERVER_HOST = config.get('APIServer', 'API_HOST')
    self.API_SERVER_PORT = config.getint('APIServer', 'API_PORT')
    self.API_SERVER_SECURE = config.getboolean('APIServer', 'API_SECURE')
    
    # gateway settings
    self.GATEWAY_ID = config.get('Gateway', 'GATEWAY_ID')
    self.GATEWAY_URL = config.get('Gateway', 'GATEWAY_URL')
    self.GATEWAY_DATA_STORE_DIR = config.get('Gateway', 'GATEWAY_DATA_STORE_DIR')
    self.STORAGE_RESOURCE_HOST = config.get('Gateway', 'STORAGE_RESOURCE_HOST')
    self.SFTP_PORT = config.get('Gateway', 'SFTP_PORT')
    
    # runtime-specific settings
    self.PROJECT_NAME = config.get('User', 'PROJECT_NAME')
    self.GROUP_RESOURCE_PROFILE_NAME = config.get('User', 'GROUP_RESOURCE_PROFILE_NAME')
    

class AiravataOperator:

  def register_input_file(
      self,
      file_identifier: str,
      storage_name: str,
      storageId: str,
      gateway_id: str,
      input_file_name: str,
      uploaded_storage_path: str,
  ) -> str:
    
    dataProductModel = DataProductModel(
      gatewayId=gateway_id,
      ownerName=self.user_id,
      productName=file_identifier,
      dataProductType=DataProductType.FILE,
      replicaLocations=[
        DataReplicaLocationModel(
        replicaName="{} gateway data store copy".format(input_file_name),
        replicaLocationCategory=ReplicaLocationCategory.GATEWAY_DATA_STORE,
        storageResourceId=storageId,
        filePath="file://{}:{}".format(storage_name, uploaded_storage_path + input_file_name),
      )],
    )

    return self.api_server_client.register_data_product(self.airavata_token, dataProductModel) # type: ignore

  def create_experiment_model(
      self,
      project_name: str,
      application_name: str,
      experiment_name: str,
      description: str,
      gateway_id: str,
  ) -> ExperimentModel:
    
    execution_id = self.get_app_interface_id(application_name)
    project_id = self.get_project_id(project_name)
    return ExperimentModel(
      experimentName=experiment_name,
      gatewayId=gateway_id,
      userName=self.user_id,
      description=description,
      projectId=project_id,
      experimentType=ExperimentType.SINGLE_APPLICATION,
      executionId=execution_id
    )
  
  def get_resource_host_id(self, resource_name):
        resources: dict = self.api_server_client.get_all_compute_resource_names(self.airavata_token) # type: ignore
        return next((str(k) for k, v in resources.items() if v == resource_name))
  
  def configure_computation_resource_scheduling(
      self,
      experiment_model: ExperimentModel,
      computation_resource_name: str,
      group_resource_profile_name: str,
      storageId: str,
      node_count: int,
      total_cpu_count: int,
      queue_name: str,
      wall_time_limit: int,
      experiment_dir_path: str,
      auto_schedule=False,
  ) -> ExperimentModel:
        resource_host_id = self.get_resource_host_id(computation_resource_name)
        groupResourceProfileId = self.get_group_resource_profile_id(group_resource_profile_name)
        computRes = ComputationalResourceSchedulingModel()
        computRes.resourceHostId = resource_host_id
        computRes.nodeCount = node_count
        computRes.totalCPUCount = total_cpu_count
        computRes.queueName = queue_name
        computRes.wallTimeLimit = wall_time_limit

        userConfigData = UserConfigurationDataModel()
        userConfigData.computationalResourceScheduling = computRes

        userConfigData.groupResourceProfileId = groupResourceProfileId
        userConfigData.storageId = storageId

        userConfigData.experimentDataDir = experiment_dir_path
        userConfigData.airavataAutoSchedule = auto_schedule
        experiment_model.userConfigurationData = userConfigData

        return experiment_model

  def __init__(self, access_token: str, config_file: str = "settings.ini"):
    # store variables
    self.access_token = access_token
    self.settings = Settings(config_file)
    # load api server settings and create client
    self.api_server_client = APIServerClient(api_server_settings=self.settings)
    # load gateway settings
    gateway_id = self.default_gateway_id()
    self.airavata_token = self.__airavata_token__(self.access_token, gateway_id)

  def default_gateway_id(self):
    return self.settings.GATEWAY_ID
  
  def default_gateway_grp_name(self):
    return self.settings.GROUP_RESOURCE_PROFILE_NAME
  
  def default_gateway_data_store_dir(self):
    return self.settings.GATEWAY_DATA_STORE_DIR
  
  def default_sftp_port(self):
    return self.settings.SFTP_PORT
  
  def default_sr_hostname(self):
    return self.settings.STORAGE_RESOURCE_HOST
  
  def default_project_name(self):
    return self.settings.PROJECT_NAME

  def __airavata_token__(self, access_token: str, gateway_id: str):
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
    gateway_id = gateway_id or self.default_gateway_id()
    # logic
    app_interfaces = self.api_server_client.get_all_application_interfaces(self.airavata_token, gateway_id)
    return app_interfaces


  def get_preferred_storage(self, gateway_id: str | None = None, sr_hostname: str | None = None):
    """
    Get preferred storage resource

    """
    # use defaults for missing values
    gateway_id = gateway_id or self.default_gateway_id()
    sr_hostname = sr_hostname or self.default_sr_hostname()
    # logic
    sr_names: dict[str, str] = self.api_server_client.get_all_storage_resource_names(self.airavata_token) # type: ignore
    sr_id = next((str(k) for k, v in sr_names.items() if v == sr_hostname))
    return self.api_server_client.get_gateway_storage_preference(self.airavata_token, gateway_id, sr_id)


  def get_storage(self, storage_name: str | None = None) -> any:  # type: ignore
    """
    Get storage resource by name

    """
    # use defaults for missing values
    storage_name = storage_name or self.default_sr_hostname()
    # logic
    sr_names: dict[str, str] = self.api_server_client.get_all_storage_resource_names(self.airavata_token) # type: ignore
    sr_id = next((str(k) for k, v in sr_names.items() if v == storage_name))
    storage = self.api_server_client.get_storage_resource(self.airavata_token, sr_id)
    return storage
  



  def get_group_resource_profile_id(self, grp_name: str | None = None) -> str:
    """
    Get group resource profile id by name

    """
    # use defaults for missing values
    grp_name = grp_name or self.default_gateway_grp_name()
    # logic
    grps: list = self.api_server_client.get_group_resource_list(self.airavata_token, self.default_gateway_id()) # type: ignore
    grp_id = next((grp.groupResourceProfileId for grp in grps if grp.groupResourceProfileName == grp_name))
    return str(grp_id)
  
  def get_group_resource_profile(self, grp_id: str):
    grp: any = self.api_server_client.get_group_resource_profile(self.airavata_token, grp_id) # type: ignore
    return grp


  def get_compatible_deployments(self, app_interface_id: str, grp_name: str | None = None):
    """
    Get compatible deployments for an application interface and group resource profile

    """
    # use defaults for missing values
    grp_name = grp_name or self.default_gateway_grp_name()
    # logic
    grps: list = self.api_server_client.get_group_resource_list(self.airavata_token, self.default_gateway_id()) # type: ignore
    grp_id = next((grp.groupResourceProfileId for grp in grps if grp.groupResourceProfileName == grp_name))
    deployments = self.api_server_client.get_application_deployments_for_app_module_and_group_resource_profile(self.airavata_token, app_interface_id, grp_id)
    return deployments


  def get_app_interface_id(self, app_name: str, gateway_id: str | None = None):
    """
    Get application interface id by name

    """
    gateway_id = str(gateway_id or self.default_gateway_id())
    apps: list = self.api_server_client.get_all_application_interfaces(self.airavata_token, gateway_id) # type: ignore
    app_id = next((app.applicationInterfaceId for app in apps if app.applicationName == app_name))
    return str(app_id)
  

  def get_project_id(self, project_name: str, gateway_id: str | None = None):
    gateway_id = str(gateway_id or self.default_gateway_id())
    projects: list = self.api_server_client.get_user_projects(self.airavata_token, gateway_id, self.user_id, 10, 0) # type: ignore
    project_id = next((p.projectID for p in projects if p.name == project_name and p.owner == self.user_id))
    return str(project_id)


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


  def make_experiment_dir(self, sr_host: str, project_name: str, experiment_name: str) -> str:
    """
    Make experiment directory on storage resource, and return the remote path

    Return Path: /{project_name}/{experiment_name}

    """
    host = sr_host
    port = self.default_sftp_port()
    sftp_connector = SFTPConnector(host=host, port=int(port), username=self.user_id, password=self.access_token)
    remote_path = sftp_connector.mkdir(project_name, experiment_name)
    logger.info("Experiment directory created at %s", remote_path)
    return remote_path


  def upload_files(self, sr_host: str, local_files: list[Path], remote_dir: str) -> list[str]:
    """
    Upload local files to a remote directory of a storage resource

    Return Path: /{project_name}/{experiment_name}

    """
    host = sr_host
    port = self.default_sftp_port()
    sftp_connector = SFTPConnector(host=host, port=int(port), username=self.user_id, password=self.access_token)
    paths = sftp_connector.put(local_files, remote_dir)
    logger.info(f"{len(paths)} Local files uploaded to remote dir: %s", remote_dir)
    return paths


  def list_files(self, sr_host: str, remote_dir: str) -> list[str]:
    """
    List files in a remote directory of a storage resource

    Return Path: /{project_name}/{experiment_name}

    """
    host = sr_host
    port = self.default_sftp_port()
    sftp_connector = SFTPConnector(host=host, port=int(port), username=self.user_id, password=self.access_token)
    return sftp_connector.ls(remote_dir)


  def download_file(self, sr_host: str, remote_file: str, local_dir: str) -> str:
    """
    Download files from a remote directory of a storage resource to a local directory

    Return Path: /{project_name}/{experiment_name}

    """
    host = sr_host
    port = self.default_sftp_port()
    sftp_connector = SFTPConnector(host=host, port=int(port), username=self.user_id, password=self.access_token)
    path = sftp_connector.get(remote_file, local_dir)
    logger.info("Remote files downlaoded to local dir: %s", local_dir)
    return path
  
  def cat_file(self, sr_host: str, remote_file: str) -> bytes:
    """
    Download files from a remote directory of a storage resource to a local directory

    Return Path: /{project_name}/{experiment_name}

    """
    host = sr_host
    port = self.default_sftp_port()
    sftp_connector = SFTPConnector(host=host, port=int(port), username=self.user_id, password=self.access_token)
    data = sftp_connector.cat(remote_file)
    logger.info("Remote files downlaoded to local dir: %s bytes", len(data))
    return data

  def launch_experiment(
      self,
      experiment_name: str,
      app_name: str,
      inputs: dict[str, str | int | float | list[str]],
      computation_resource_name: str,
      queue_name: str,
      node_count: int,
      cpu_count: int,
      walltime: int,
      *,
      gateway_id: str | None = None,
      grp_name: str | None = None,
      sr_host: str | None = None,
      project_name: str | None = None,
      auto_schedule: bool = False,
  ) -> LaunchState:
    """
    Launch an experiment and return its id

    """
    # preprocess args (str)
    print("[AV] Preprocessing args...")
    gateway_id = str(gateway_id or self.default_gateway_id())
    grp_name = str(grp_name or self.default_gateway_grp_name())
    sr_host = str(sr_host or self.default_sr_hostname())
    mount_point = Path(self.default_gateway_data_store_dir()) / self.user_id
    project_name = str(project_name or self.default_project_name())

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
    sr_id = storage.storageResourceId

    # setup application interface
    print("[AV] Setting up application interface...")
    app_interface_id = self.get_app_interface_id(app_name)
    assert app_interface_id is not None

    # setup experiment
    print("[AV] Setting up experiment...")
    experiment = self.create_experiment_model(
        experiment_name=experiment_name,
        application_name=app_name,
        project_name=project_name,
        description=experiment_name,
        gateway_id=gateway_id,
    )
    # setup experiment directory
    print("[AV] Setting up experiment directory...")
    exp_dir = self.make_experiment_dir(
        sr_host=storage.hostName,
        project_name=project_name,
        experiment_name=experiment_name,
    )
    abs_path = (mount_point / exp_dir.lstrip("/")).as_posix().rstrip("/") + "/"
    print("[AV] exp_dir:", exp_dir)
    print("[AV] abs_path:", abs_path)

    experiment = self.configure_computation_resource_scheduling(
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
      return str(self.register_input_file(file.name, sr_host, sr_id, gateway_id, file.name, abs_path))

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
    print(f"[AV] Uploading {len(files_to_upload)} file inputs for experiment...")
    self.upload_files(storage.hostName, files_to_upload, exp_dir)

    # configure experiment inputs
    experiment_inputs = []
    for exp_input in self.api_server_client.get_application_inputs(self.airavata_token, app_interface_id):  # type: ignore
      if exp_input.type < 3 and exp_input.name in data_inputs:
        value = data_inputs[exp_input.name]
        if exp_input.type == 0:
          exp_input.value = str(value)
        else:
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

    return LaunchState(
      experiment_id=str(ex_id),
      mount_point=mount_point,
      experiment_dir=exp_dir,
      sr_host=str(storage.hostName),
    )


  def get_experiment_status(self, experiment_id) -> Literal["CREATED", "VALIDATED", "SCHEDULED", "LAUNCHED", "EXECUTING", "CANCELING", "CANCELED", "COMPLETED", "FAILED"]:
    states = ["CREATED", "VALIDATED", "SCHEDULED", "LAUNCHED", "EXECUTING", "CANCELING", "CANCELED", "COMPLETED", "FAILED"]
    status: any = self.api_server_client.get_experiment_status(self.airavata_token, experiment_id) # type: ignore
    return states[status.state]
  

  def stop_experiment(self, experiment_id):
    status = self.api_server_client.terminate_experiment(
        self.airavata_token, experiment_id, self.default_gateway_id())
    return status
