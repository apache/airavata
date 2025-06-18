import logging
import time

from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.credential_store_client import CredentialStoreClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator
from airavata_sdk.clients.sftp_file_handling_client import SFTPConnector
from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil
from airavata_sdk.clients.utils.data_model_creation_util import DataModelCreationUtil
from airavata_sdk.transport.settings import GatewaySettings

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

configFile: str = "/Users/isururanawaka/Documents/Airavata_Repository/airavata/airavata-api/airavata-client-sdks/airavata-python-sdk/airavata_sdk/transport/settings.ini"

authenticator = Authenticator(configFile)
username: str = "username"
password: str = "password"
gateway_id: str = "cyberwater"
token = authenticator.get_token_and_user_info_password_flow(username=username, password=password, gateway_id=gateway_id)

api_server_client = APIServerClient(configFile)

data_model_client = DataModelCreationUtil(configFile,
                                          gateway_id=gateway_id,
                                          username=username,
                                          password=password,
                                          access_token=token.accessToken)

credential_store_client = CredentialStoreClient(configFile)

airavata_util = APIServerClientUtil(
    configFile,
    gateway_id=gateway_id,
    username=username,
    password=password,
)

executionId = airavata_util.get_execution_id("Echo")
assert executionId is not None

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

sftp_connector = SFTPConnector(host="cyberwater.scigap.org", port=9000, username="isuru_janith",
                               password=token.accessToken)
path_suffix = sftp_connector.upload_files("/Users/isururanawaka/Documents/Cyberwater/poc2/resources/storage",
                                          "Default_Project",
                                          experiment.experimentName)

sftp_connector = SFTPConnector(host="cyberwater.scigap.org", port=9000, username="isuru_janith",
                               password=token.accessToken)
path_suffix = sftp_connector.upload_files("/Users/isururanawaka/Documents/Cyberwater/poc2/resources/storage",
                                          "Default_Project",
                                          experiment.experimentName)

gateway_settings = GatewaySettings(configFile)
path = gateway_settings.GATEWAY_DATA_STORE_DIR + path_suffix

# configure computational resources
experiment = data_model_client.configure_computation_resource_scheduling(experiment_model=experiment,
                                                                         computation_resource_name="karst.uits.iu.edu",
                                                                         group_resource_profile_name="Default Gateway Profile",
                                                                         storageId="pgadev.scigap.org",
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

sftp_connector.download_files(".", f"Default_Project/{experiment.experimentName}")
