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

from airavata_sdk.transport.settings import APIServerSettings
from .sftp import SFTPConnector
import time
import warnings
import requests
from urllib.parse import urlparse
import uuid
import os
import base64

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
  ("agent_ref", str),
  ("process_id", str),
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
    self.CONNECTION_SVC_URL = config.get('APIServer', 'CONNECTION_SVC_URL')
    self.FILEMGR_SVC_URL = config.get('APIServer', 'FILEMGR_SVC_URL')
    
    # gateway settings
    self.GATEWAY_ID = config.get('Gateway', 'GATEWAY_ID')
    self.GATEWAY_URL = config.get('Gateway', 'GATEWAY_URL')
    self.GATEWAY_DATA_STORE_DIR = config.get('Gateway', 'GATEWAY_DATA_STORE_DIR')
    self.STORAGE_RESOURCE_HOST = config.get('Gateway', 'STORAGE_RESOURCE_HOST')
    self.SFTP_PORT = config.get('Gateway', 'SFTP_PORT')
    

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
      group: str,
      storageId: str,
      node_count: int,
      total_cpu_count: int,
      queue_name: str,
      wall_time_limit: int,
      experiment_dir_path: str,
      auto_schedule=False,
  ) -> ExperimentModel:
        resource_host_id = self.get_resource_host_id(computation_resource_name)
        groupResourceProfileId = self.get_group_resource_profile_id(group)
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
    api_server_settings = APIServerSettings(config_file)
    self.api_server_client = APIServerClient(api_server_settings=api_server_settings)
    # load gateway settings
    gateway_id = self.default_gateway_id()
    self.airavata_token = self.__airavata_token__(self.access_token, gateway_id)

  def default_gateway_id(self):
    return self.settings.GATEWAY_ID
  
  def default_gateway_data_store_dir(self):
    return self.settings.GATEWAY_DATA_STORE_DIR
  
  def default_sftp_port(self):
    return self.settings.SFTP_PORT
  
  def default_sr_hostname(self):
    return self.settings.STORAGE_RESOURCE_HOST
  
  def connection_svc_url(self):
    return self.settings.CONNECTION_SVC_URL
  
  def filemgr_svc_url(self):
    return self.settings.FILEMGR_SVC_URL

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
  
  def get_process_id(self, experiment_id: str) -> str:
    """
    Get process id by experiment id

    """
    tree = self.api_server_client.get_detailed_experiment_tree(self.airavata_token, experiment_id) # type: ignore
    processModels = tree.processes
    assert processModels is not None
    assert len(processModels) == 1, f"Expected 1 process model, got {len(processModels)}"
    return processModels[0].processId

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

  def get_group_resource_profile_id(self, group: str) -> str:
    """
    Get group resource profile id by name

    """
    # logic
    grps: list = self.api_server_client.get_group_resource_list(self.airavata_token, self.default_gateway_id()) # type: ignore
    grp_id = next((grp.groupResourceProfileId for grp in grps if grp.groupResourceProfileName == group))
    return str(grp_id)
  
  def get_group_resource_profile(self, group_id: str):
    grp = self.api_server_client.get_group_resource_profile(self.airavata_token, group_id) # type: ignore
    return grp

  def get_compatible_deployments(self, app_interface_id: str, group: str):
    """
    Get compatible deployments for an application interface and group resource profile

    """
    # logic
    grps: list = self.api_server_client.get_group_resource_list(self.airavata_token, self.default_gateway_id()) # type: ignore
    grp_id = next((grp.groupResourceProfileId for grp in grps if grp.groupResourceProfileName == group))
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

  def upload_files(self, process_id: str | None, agent_ref: str | None, sr_host: str, local_files: list[Path], remote_dir: str) -> list[str]:
    """
    Upload local files to a remote directory of a storage resource
    TODO add data_svc fallback

    Return Path: /{project_name}/{experiment_name}

    """

    # step = experiment staging
    if process_id is None and agent_ref is None:
      host = sr_host
      port = self.default_sftp_port()
      sftp_connector = SFTPConnector(host=host, port=int(port), username=self.user_id, password=self.access_token)
      paths = sftp_connector.put(local_files, remote_dir)
      logger.info(f"{len(paths)} Local files uploaded to remote dir: %s", remote_dir)
      return paths
    
    # step = post-staging file upload
    elif process_id is not None and agent_ref is not None:
      assert len(local_files) == 1, f"Expected 1 file, got {len(local_files)}"
      file = local_files[0]
      fp = os.path.join("/data", file.name)
      rawdata = file.read_bytes()
      b64data = base64.b64encode(rawdata).decode()
      res = requests.post(f"{self.connection_svc_url()}/agent/execute/shell", json={
          "agentId": agent_ref,
          "envName": "base",
          "workingDir": ".",
          "arguments": ["sh", "-c", f"echo {b64data} | base64 -d > {fp}"]
      })
      data = res.json()
      if data["error"] is not None:
        if str(data["error"]) == "Agent not found":
          port = self.default_sftp_port()
          sftp_connector = SFTPConnector(host=sr_host, port=int(port), username=self.user_id, password=self.access_token)
          paths = sftp_connector.put(local_files, remote_dir)
          return paths
        else:
          raise Exception(data["error"])
      else:
        exc_id = data["executionId"]
        while True:
          res = requests.get(f"{self.connection_svc_url()}/agent/execute/shell/{exc_id}")
          data = res.json()
          if data["executed"]:
            return [fp]
          time.sleep(1)

    # step = unknown
    else:
      raise ValueError("Invalid arguments for upload_files")
    
    # file manager service fallback
    assert process_id is not None, f"Expected process_id, got {process_id}"
    file = local_files[0]
    url_path = os.path.join(process_id, file.name)
    filemgr_svc_upload_url = f"{self.filemgr_svc_url()}/upload/live/{url_path}"

  def list_files(self, process_id: str, agent_ref: str, sr_host: str, remote_dir: str) -> list[str]:
    """
    List files in a remote directory of a storage resource
    TODO add data_svc fallback

    Return Path: /{project_name}/{experiment_name}

    """
    res = requests.post(f"{self.connection_svc_url()}/agent/execute/shell", json={
        "agentId": agent_ref,
        "envName": "base",
        "workingDir": ".",
        "arguments": ["sh", "-c", r"find /data -type d -name 'venv' -prune -o -type f -printf '%P\n' | sort"]
    })
    data = res.json()
    if data["error"] is not None:
      if str(data["error"]) == "Agent not found":
        port = self.default_sftp_port()
        sftp_connector = SFTPConnector(host=sr_host, port=int(port), username=self.user_id, password=self.access_token)
        return sftp_connector.ls(remote_dir)
      else:
        raise Exception(data["error"])
    else:
      exc_id = data["executionId"]
      while True:
        res = requests.get(f"{self.connection_svc_url()}/agent/execute/shell/{exc_id}")
        data = res.json()
        if data["executed"]:
          files = data["responseString"].split("\n")
          return files
        time.sleep(1)

    # file manager service fallback
    assert process_id is not None, f"Expected process_id, got {process_id}"
    filemgr_svc_ls_url = f"{self.filemgr_svc_url()}/list/live/{process_id}"

  def download_file(self, process_id: str, agent_ref: str, sr_host: str, remote_file: str, remote_dir: str, local_dir: str) -> str:
    """
    Download files from a remote directory of a storage resource to a local directory
    TODO add data_svc fallback

    Return Path: /{project_name}/{experiment_name}

    """
    import os
    fp = os.path.join("/data", remote_file)
    res = requests.post(f"{self.connection_svc_url()}/agent/execute/shell", json={
        "agentId": agent_ref,
        "envName": "base",
        "workingDir": ".",
        "arguments": ["sh", "-c", f"cat {fp} | base64 -w0"]
    })
    data = res.json()
    if data["error"] is not None:
      if str(data["error"]) == "Agent not found":
        port = self.default_sftp_port()
        fp = os.path.join(remote_dir, remote_file)
        sftp_connector = SFTPConnector(host=sr_host, port=int(port), username=self.user_id, password=self.access_token)
        path = sftp_connector.get(fp, local_dir)
        return path
      else:
        raise Exception(data["error"])
    else:
      exc_id = data["executionId"]
      while True:
        res = requests.get(f"{self.connection_svc_url()}/agent/execute/shell/{exc_id}")
        data = res.json()
        if data["executed"]:
          content = data["responseString"]
          import base64
          content = base64.b64decode(content)
          path =  Path(local_dir) / remote_file
          with open(path, "wb") as f:
            f.write(content)
          return path.as_posix()
        time.sleep(1)
    
    # file manager service fallback
    assert process_id is not None, f"Expected process_id, got {process_id}"
    url_path = os.path.join(process_id, remote_file)
    filemgr_svc_download_url = f"{self.filemgr_svc_url()}/download/live/{url_path}"
  
  def cat_file(self, process_id: str, agent_ref: str, sr_host: str, remote_file: str, remote_dir: str) -> bytes:
    """
    Download files from a remote directory of a storage resource to a local directory
    TODO add data_svc fallback

    Return Path: /{project_name}/{experiment_name}

    """
    import os
    fp = os.path.join("/data", remote_file)
    res = requests.post(f"{self.connection_svc_url()}/agent/execute/shell", json={
        "agentId": agent_ref,
        "envName": "base",
        "workingDir": ".",
        "arguments": ["sh", "-c", f"cat {fp} | base64 -w0"]
    })
    data = res.json()
    if data["error"] is not None:
      if str(data["error"]) == "Agent not found":
        port = self.default_sftp_port()
        fp = os.path.join(remote_dir, remote_file)
        sftp_connector = SFTPConnector(host=sr_host, port=int(port), username=self.user_id, password=self.access_token)
        data = sftp_connector.cat(fp)
        return data
      else:
        raise Exception(data["error"])
    else:
      exc_id = data["executionId"]
      while True:
        res = requests.get(f"{self.connection_svc_url()}/agent/execute/shell/{exc_id}")
        data = res.json()
        if data["executed"]:
          content = data["responseString"]
          import base64
          content = base64.b64decode(content)
          return content
        time.sleep(1)

    # file manager service fallback
    assert process_id is not None, f"Expected process_id, got {process_id}"
    url_path = os.path.join(process_id, remote_file)
    filemgr_svc_download_url = f"{self.filemgr_svc_url()}/download/live/{url_path}"

  def launch_experiment(
      self,
      experiment_name: str,
      project: str,
      app_name: str,
      inputs: dict[str, dict[str, str | int | float | list[str]]],
      computation_resource_name: str,
      queue_name: str,
      node_count: int,
      cpu_count: int,
      walltime: int,
      group: str = "Default",
      *,
      gateway_id: str | None = None,
      sr_host: str | None = None,
      auto_schedule: bool = False,
  ) -> LaunchState:
    """
    Launch an experiment and return its id

    """
    # preprocess args (str)
    print("[AV] Preprocessing args...")
    gateway_id = str(gateway_id or self.default_gateway_id())
    sr_host = str(sr_host or self.default_sr_hostname())
    mount_point = Path(self.default_gateway_data_store_dir()) / self.user_id
    server_url = urlparse(self.connection_svc_url()).netloc

    # validate args (str)
    print("[AV] Validating args...")
    assert len(experiment_name) > 0, f"Invalid experiment_name: {experiment_name}"
    assert len(app_name) > 0, f"Invalid app_name: {app_name}"
    assert len(computation_resource_name) > 0, f"Invalid computation_resource_name: {computation_resource_name}"
    assert len(inputs) > 0, f"Invalid inputs: {inputs}"
    assert len(gateway_id) > 0, f"Invalid gateway_id: {gateway_id}"
    assert len(queue_name) > 0, f"Invalid queue_name: {queue_name}"
    assert len(group) > 0, f"Invalid group name: {group}"
    assert len(sr_host) > 0, f"Invalid sr_host: {sr_host}"
    assert len(project) > 0, f"Invalid project_name: {project}"
    assert len(mount_point.as_posix()) > 0, f"Invalid mount_point: {mount_point}"

    # validate args (int)
    assert node_count > 0, f"Invalid node_count: {node_count}"
    assert cpu_count > 0, f"Invalid cpu_count: {cpu_count}"
    assert walltime > 0, f"Invalid walltime: {walltime}"

    # parse and validate inputs
    file_inputs = dict[str, Path | list[Path]]()
    data_inputs = dict[str, str | int | float]()
    for input_name, input_spec in inputs.items():
      input_type = input_spec["type"]
      input_value = input_spec["value"]
      if input_type == "uri":
        assert isinstance(input_value, str) and os.path.isfile(str(input_value)), f"Invalid {input_name}: {input_value}"
        file_inputs[input_name] = Path(input_value)
      elif input_type == "uri[]":
        assert isinstance(input_value, list) and all([os.path.isfile(str(v)) for v in input_value]), f"Invalid {input_name}: {input_value}"
        file_inputs[input_name] = [Path(v) for v in input_value]
      else:
        assert isinstance(input_value, (int, float, str)), f"Invalid {input_name}: {input_value}"
        data_inputs[input_name] = input_value
    data_inputs.update({"agent_id": data_inputs.get("agent_id", str(uuid.uuid4()))})
    data_inputs.update({"server_url": server_url})

    # setup runtime params
    print("[AV] Setting up runtime params...")
    storage = self.get_storage(sr_host)
    sr_id = storage.storageResourceId

    # setup application interface
    print("[AV] Setting up application interface...")
    app_interface_id = self.get_app_interface_id(app_name)
    assert app_interface_id is not None, f"Invalid app_interface_id: {app_interface_id}"

    # setup experiment
    print("[AV] Setting up experiment...")
    experiment = self.create_experiment_model(
        experiment_name=experiment_name,
        application_name=app_name,
        project_name=project,
        description=experiment_name,
        gateway_id=gateway_id,
    )
    # setup experiment directory
    print("[AV] Setting up experiment directory...")
    exp_dir = self.make_experiment_dir(
        sr_host=storage.hostName,
        project_name=project,
        experiment_name=experiment_name,
    )
    abs_path = (mount_point / exp_dir.lstrip("/")).as_posix().rstrip("/") + "/"
    print("[AV] exp_dir:", exp_dir)
    print("[AV] abs_path:", abs_path)

    experiment = self.configure_computation_resource_scheduling(
        experiment_model=experiment,
        computation_resource_name=computation_resource_name,
        group=group,
        storageId=sr_id,
        node_count=node_count,
        total_cpu_count=cpu_count,
        wall_time_limit=walltime,
        queue_name=queue_name,
        experiment_dir_path=abs_path,
        auto_schedule=auto_schedule,
    )

    def register_input_file(file: Path) -> str:
      return str(self.register_input_file(file.name, sr_host, sr_id, gateway_id, file.name, abs_path))
    
    # set up file inputs
    print("[AV] Setting up file inputs...")
    files_to_upload = list[Path]()
    file_refs = dict[str, str | list[str]]()
    for key, value in file_inputs.items():
      if isinstance(value, Path):
        files_to_upload.append(value)
        file_refs[key] = register_input_file(value)
      elif isinstance(value, list):
        assert all([isinstance(v, Path) for v in value]), f"Invalid file input value: {value}"
        files_to_upload.extend(value)
        file_refs[key] = [*map(register_input_file, value)]
      else:
        raise ValueError("Invalid file input type")

    # configure experiment inputs
    experiment_inputs = []
    for exp_input in self.api_server_client.get_application_inputs(self.airavata_token, app_interface_id):  # type: ignore
      assert exp_input.type is not None
      if exp_input.type < 3 and exp_input.name in data_inputs:
        value = data_inputs[exp_input.name]
        if exp_input.type == 0:
          exp_input.value = str(value)
        else:
          exp_input.value = repr(value)
      elif exp_input.type == 3 and exp_input.name in file_refs:
        ref = file_refs[exp_input.name]
        assert isinstance(ref, str)
        exp_input.value = ref
      elif exp_input.type == 4 and exp_input.name in file_refs:
        exp_input.value = ','.join(file_refs[exp_input.name])
      experiment_inputs.append(exp_input)
    experiment.experimentInputs = experiment_inputs

    # configure experiment outputs
    outputs = self.api_server_client.get_application_outputs(self.airavata_token, app_interface_id)
    experiment.experimentOutputs = outputs

    # upload file inputs for experiment
    print(f"[AV] Uploading {len(files_to_upload)} file inputs for experiment...")
    self.upload_files(None, None, storage.hostName, files_to_upload, exp_dir)

    # create experiment
    ex_id = self.api_server_client.create_experiment(self.airavata_token, gateway_id, experiment)
    ex_id = str(ex_id)
    print(f"[AV] Experiment {experiment_name} CREATED with id: {ex_id}")

    # launch experiment
    self.api_server_client.launch_experiment(self.airavata_token, ex_id, gateway_id)
    print(f"[AV] Experiment {experiment_name} STARTED with id: {ex_id}")

    # wait until experiment begins, then get process id
    print(f"[AV] Experiment {experiment_name} WAITING until experiment begins...")
    process_id = None
    while process_id is None:
      try:
        process_id = self.get_process_id(ex_id)
      except:
        time.sleep(2)
      else:
        time.sleep(2)
    print(f"[AV] Experiment {experiment_name} EXECUTING with pid: {process_id}")

    # wait until task begins, then get job id
    print(f"[AV] Experiment {experiment_name} WAITING until task begins...")
    job_id = job_state = None
    while job_state is None:
      try:
        job_id, job_state = self.get_task_status(ex_id)
      except:
        time.sleep(2)
      else:
        time.sleep(2)
    print(f"[AV] Experiment {experiment_name} - Task {job_state} with id: {job_id}")

    return LaunchState(
      experiment_id=ex_id,
      agent_ref=str(data_inputs["agent_id"]),
      process_id=process_id,
      mount_point=mount_point,
      experiment_dir=exp_dir,
      sr_host=storage.hostName,
    )

  def get_experiment_status(self, experiment_id: str) -> Literal['CREATED', 'VALIDATED', 'SCHEDULED', 'LAUNCHED', 'EXECUTING', 'CANCELING', 'CANCELED', 'COMPLETED', 'FAILED']:
    states = ["CREATED", "VALIDATED", "SCHEDULED", "LAUNCHED", "EXECUTING", "CANCELING", "CANCELED", "COMPLETED", "FAILED"]
    status = self.api_server_client.get_experiment_status(self.airavata_token, experiment_id)
    state = status.state.name
    if state in states:
      return state
    else:
      return "FAILED"

  def stop_experiment(self, experiment_id: str):
    status = self.api_server_client.terminate_experiment(
        self.airavata_token, experiment_id, self.default_gateway_id())
    return status
  
  def execute_py(self, project: str, libraries: list[str], code: str, agent_id: str, pid: str, runtime_args: dict, cold_start: bool = True) -> str | None:
    # lambda to send request
    try:
      if cold_start:
        print(f"[av] Looking for Agent {agent_id}...")
        res = requests.get(f"{self.connection_svc_url()}/{agent_id}")
        data = res.json()
        if data["agentUp"] == False:
          # waiting for agent to be available
          print(f"[av] Agent {agent_id} not found! Relaunching...")
          self.launch_experiment(
            experiment_name="Agent",
            app_name="AiravataAgent",
            project=project,
            inputs={
              "agent_id": {"type": "str", "value": agent_id},
              "server_url": {"type": "str", "value": urlparse(self.connection_svc_url()).netloc},
              "process_id": {"type": "str", "value": pid},
            },
            computation_resource_name=runtime_args["cluster"],
            queue_name=runtime_args["queue_name"],
            node_count=1,
            cpu_count=runtime_args["cpu_count"],
            walltime=runtime_args["walltime"],
            group=runtime_args["group"],
          )
        return self.execute_py(project, libraries, code, agent_id, pid, runtime_args, cold_start=False)

      print(f"[av] Waiting for Agent {agent_id}...")
      while True: # poll for response
        res = requests.get(f"{self.connection_svc_url()}/{agent_id}")
        data = res.json()
        if data["agentUp"]:
          break
        time.sleep(1)
      
      print(f"[av] Agent {agent_id} found! creating environment...")
      res = requests.post(f"{self.connection_svc_url()}/setup/env", json={
        "agentId": agent_id,
        "envName": "base",
        "libraries": ["python=3.10", "pip"],
        "pip": libraries,
      })
      data = res.json()
      if {exc_id := data["executionId"]} is None:
        raise Exception(data["error"])
      while True: # poll for response
        res = requests.get(f"{self.connection_svc_url()}/agent/setup/env/{exc_id}")
        data = res.json()
        if data["setup"]:
          response = str(data["responseString"])
          break
        time.sleep(1)

      print(f"[av] Agent {agent_id} env created! executing code...")
      res = requests.post(f"{self.connection_svc_url()}/agent/execute/python", json={
        "agentId": agent_id,
        "envName": "base",
        "workingDir": ".",
        "code": code,
      })
      data = res.json()
      if {exc_id := data["executionId"]} is None:
        raise Exception(data["error"])
      while True: # poll for response
        res = requests.get(f"{self.connection_svc_url()}/agent/execute/python/{exc_id}")
        data = res.json()
        if data["executed"]:
          response = str(data["responseString"])
          break
        time.sleep(1)
      
      print(f"[av] Agent {agent_id} code executed! response: {response}")
      return response
    
    except Exception as e:
      print(f"[av] Remote execution failed! {e}")
      return None
    
  def get_available_runtimes(self):
    from .runtime import Remote
    return [
      Remote(cluster="login.expanse.sdsc.edu", category="gpu", queue_name="gpu-shared", node_count=1, cpu_count=10, gpu_count=1, walltime=30, group="Default"),
      Remote(cluster="login.expanse.sdsc.edu", category="cpu", queue_name="shared", node_count=1, cpu_count=10, gpu_count=0, walltime=30, group="Default"),
      Remote(cluster="anvil.rcac.purdue.edu", category="cpu", queue_name="shared", node_count=1, cpu_count=24, gpu_count=0, walltime=30, group="Default"),
      Remote(cluster="login.expanse.sdsc.edu", category="gpu", queue_name="gpu-shared", node_count=1, cpu_count=10, gpu_count=1, walltime=30, group="GaussianGroup"),
      Remote(cluster="login.expanse.sdsc.edu", category="cpu", queue_name="shared", node_count=1, cpu_count=10, gpu_count=0, walltime=30, group="GaussianGroup"),
      Remote(cluster="anvil.rcac.purdue.edu", category="cpu", queue_name="shared", node_count=1, cpu_count=24, gpu_count=0, walltime=30, group="GaussianGroup"),
    ]
  
  def get_task_status(self, experiment_id: str) -> tuple[str, Literal["SUBMITTED", "UN_SUBMITTED", "SETUP", "QUEUED", "ACTIVE", "COMPLETE", "CANCELING", "CANCELED", "FAILED", "HELD", "SUSPENDED", "UNKNOWN"] | None]:
    states = ["SUBMITTED", "UN_SUBMITTED", "SETUP", "QUEUED", "ACTIVE", "COMPLETE", "CANCELING", "CANCELED", "FAILED", "HELD", "SUSPENDED", "UNKNOWN"]
    job_details: dict = self.api_server_client.get_job_statuses(self.airavata_token, experiment_id) # type: ignore
    job_id = job_state = None
    # get the most recent job id and state
    for job_id, v in job_details.items():
      if v.reason in states:
        job_state = v.reason
      else:
        job_state = states[int(v.jobState)]
    return job_id or "N/A", job_state # type: ignore

