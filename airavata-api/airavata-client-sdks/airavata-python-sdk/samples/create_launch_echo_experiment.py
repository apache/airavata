import logging
import time
import json
import samples.file_utils as fb

from clients.keycloak_token_fetcher import Authenticator

from clients.api_server_client import APIServerClient

from clients.credential_store_client import CredentialStoreClient

from airavata.model.experiment.ttypes import ExperimentModel, ExperimentType, UserConfigurationDataModel
from airavata.model.scheduling.ttypes import ComputationalResourceSchedulingModel

from clients.utils.data_model_creation_util import DataModelCreationUtil

from clients.utils.api_server_client_util import APIServerClientUtil

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

configFile = "settings.ini"

authenticator = Authenticator(configFile)
username = "username"
password = "password"
gateway_id = "cyberwater"
token = authenticator.get_token_and_user_info_password_flow(username=username, password=password, gateway_id=gateway_id)

api_server_client = APIServerClient(configFile)

data_model_client = DataModelCreationUtil(configFile,
                                          username=username,
                                          password=password,
                                          gateway_id=gateway_id)

credential_store_client = CredentialStoreClient(configFile)

airavata_util = APIServerClientUtil(configFile,
                                    username=username,
                                    password=password,
                                    gateway_id=gateway_id)

executionId = airavata_util.get_execution_id("Echo")

projectId = airavata_util.get_project_id("Default Project")

resourceHostId = airavata_util.get_resource_host_id("karst.uits.iu.edu")

groupResourceProfileId = airavata_util.get_group_resource_profile_id("Default Gateway Profile")

storageId = airavata_util.get_storage_resource_id("pgadev.scigap.org")

# create experiment data model
experiment = data_model_client.get_experiment_data_model_for_single_application(
    project_name="Default Project",
    application_name="Echo",
    experiment_name="Testing_ECHO_SDK 2",
    description="Testing")

path = fb.upload_files(api_server_client, credential_store_client, token, "cyberwater",
                       storageId,
                       "pgadev.scigap.org", username, "Default_Project", experiment.experimentName,
                       "/Users/isururanawaka/Documents/Cyberwater/poc/resources/storage")

# configure computational resources
experiment = data_model_client.configure_computation_resource_scheduling(experiment_model=experiment,
                                                                         computation_resource_name="karst.uits.iu.edu",
                                                                         group_resource_profile_name="Default Gateway Profile",
                                                                         storage_name="pgadev.scigap.org",
                                                                         node_count=1,
                                                                         total_cpu_count=16,
                                                                         wall_time_limit=15,
                                                                         queue_name="batch",
                                                                         experiment_dir_path=path)

inputs = api_server_client.get_application_inputs(token, executionId)

experiment.experimentInputs = inputs

outputs = api_server_client.get_application_outputs(token, executionId)

experiment.experimentOutputs = outputs

# create experiment
ex_id = api_server_client.create_experiment(token, gateway_id, experiment)
print(ex_id)
# launch experiment
api_server_client.launch_experiment(token, ex_id,
                                    gateway_id)

status = api_server_client.get_experiment_status(token, ex_id);

if status is not None:
    print("Initial state " + str(status.state))
while status.state <= 6:
    status = api_server_client.get_experiment_status(token,
                                                     ex_id);
    time.sleep(30)
    print("State " + str(status.state))

print("Completed")

fb.download_files(api_server_client, credential_store_client, token, gateway_id,
                  storageId,
                  "pgadev.scigap.org", username, "Default_Project", experiment.experimentName, ".")
