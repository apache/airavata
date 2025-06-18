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

import configparser
import logging
from typing import Optional

from airavata_sdk.transport import utils
from airavata_sdk.transport.settings import APIServerSettings

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class APIServerClient(object):

    def __init__(self, configuration_file_location: Optional[str] = None, api_server_settings: Optional[APIServerSettings] = None):
        if api_server_settings is not None:
            self.settings = api_server_settings
        elif configuration_file_location is not None:
            self.settings = APIServerSettings(configuration_file_location)
            self._load_settings(configuration_file_location)
        self.client = utils.initialize_api_client_pool(
            self.settings.API_SERVER_HOST,
            self.settings.API_SERVER_PORT,
            self.settings.API_SERVER_SECURE,
        )
        # expose the needed functions
        self.is_user_exists = self.client.isUserExists
        self.add_gateway = self.client.addGateway
        self.get_all_users_in_gateway = self.client.getAllUsersInGateway
        self.update_gateway = self.client.updateGateway
        self.get_gateway = self.client.getGateway
        self.delete_gateway = self.client.deleteGateway
        self.get_all_gateways = self.client.getAllGateways
        self.is_gateway_exist = self.client.isGatewayExist
        self.create_notification = self.client.createNotification
        self.update_notification = self.client.updateNotification
        self.delete_notification = self.client.deleteNotification
        self.get_notification = self.client.getNotification
        self.get_all_notifications = self.client.getAllNotifications
        self.generate_and_register_ssh_keys = self.client.generateAndRegisterSSHKeys
        self.register_pwd_credential = self.client.registerPwdCredential
        self.get_credential_summary = self.client.getCredentialSummary
        self.get_all_credential_summaries = self.client.getAllCredentialSummaries
        self.delete_ssh_pub_key = self.client.deleteSSHPubKey
        self.delete_pwd_credential = self.client.deletePWDCredential
        self.create_project = self.client.createProject
        self.update_project = self.client.updateProject
        self.get_project = self.client.getProject
        self.delete_project = self.client.deleteProject
        self.get_user_projects = self.client.getUserProjects
        self.search_projects = self.client.searchProjects
        self.search_experiments = self.client.searchExperiments
        self.get_experiment_statistics = self.client.getExperimentStatistics
        self.get_experiments_in_project = self.client.getExperimentsInProject
        self.get_user_experiments = self.client.getUserExperiments
        self.create_experiment = self.client.createExperiment
        self.delete_experiment = self.client.deleteExperiment
        self.get_experiment = self.client.getExperiment
        self.get_experiment_by_admin = self.client.getExperimentByAdmin
        self.get_detailed_experiment_tree = self.client.getDetailedExperimentTree
        self.update_experiment = self.client.updateExperiment
        self.update_experiment_configuration = self.client.updateExperimentConfiguration
        self.update_resource_scheduling = self.client.updateResourceScheduleing
        self.validate_experiment = self.client.validateExperiment
        self.launch_experiment = self.client.launchExperiment
        self.get_experiment_status = self.client.getExperimentStatus
        self.get_experiment_outputs = self.client.getExperimentOutputs
        self.get_intermediate_outputs = self.client.getIntermediateOutputs
        self.get_job_statuses = self.client.getJobStatuses
        self.get_job_details = self.client.getJobDetails
        self.clone_experiment = self.client.cloneExperiment
        self.clone_experiment_by_admin = self.client.cloneExperimentByAdmin
        self.terminate_experiment = self.client.terminateExperiment
        self.register_application_module = self.client.registerApplicationModule
        self.get_application_module = self.client.getApplicationModule
        self.update_application_module = self.client.updateApplicationModule
        self.get_all_app_modules = self.client.getAllAppModules
        self.get_accessible_app_modules = self.client.getAccessibleAppModules
        self.delete_application_module = self.client.deleteApplicationModule
        self.register_application_deployment = self.client.registerApplicationDeployment
        self.get_application_deployment = self.client.getApplicationDeployment
        self.update_application_deployment = self.client.updateApplicationDeployment
        self.delete_application_deployment = self.client.deleteApplicationDeployment
        self.get_all_application_deployments = self.client.getAllApplicationDeployments
        self.get_accessible_application_deployments = self.client.getAccessibleApplicationDeployments
        self.get_app_module_deployed_resources = self.client.getAppModuleDeployedResources
        self.get_application_deployments_for_app_module_and_group_resource_profile = self.client.getApplicationDeploymentsForAppModuleAndGroupResourceProfile
        self.register_application_interface = self.client.registerApplicationInterface
        self.clone_application_interface = self.client.cloneApplicationInterface
        self.get_application_interface = self.client.getApplicationInterface
        self.update_application_interface = self.client.updateApplicationInterface
        self.delete_application_interface = self.client.deleteApplicationInterface
        self.get_all_application_interface_names = self.client.getAllApplicationInterfaceNames
        self.get_all_application_interfaces = self.client.getAllApplicationInterfaces
        self.get_application_inputs = self.client.getApplicationInputs
        self.get_application_outputs = self.client.getApplicationOutputs
        self.get_available_app_interface_compute_resources = self.client.getAvailableAppInterfaceComputeResources
        self.register_compute_resource = self.client.registerComputeResource
        self.get_compute_resource = self.client.getComputeResource
        self.get_all_compute_resource_names = self.client.getAllComputeResourceNames
        self.update_compute_resource = self.client.updateComputeResource
        self.delete_compute_resource = self.client.deleteComputeResource
        self.register_storage_resource = self.client.registerStorageResource
        self.get_storage_resource = self.client.getStorageResource
        self.get_all_storage_resource_names = self.client.getAllStorageResourceNames
        self.update_storage_resource = self.client.updateStorageResource
        self.delete_storage_resource = self.client.deleteStorageResource
        self.add_local_submission_details = self.client.addLocalSubmissionDetails
        self.update_local_submission_details = self.client.updateLocalSubmissionDetails
        self.get_local_job_submission = self.client.getLocalJobSubmission
        self.add_ssh_job_submission_details = self.client.addSSHJobSubmissionDetails
        self.add_ssh_fork_job_submission_details = self.client.addSSHForkJobSubmissionDetails
        self.get_ssh_job_submission = self.client.getSSHJobSubmission
        self.add_unicore_job_submission_details = self.client.addUNICOREJobSubmissionDetails
        self.get_unicore_job_submission = self.client.getUnicoreJobSubmission
        self.add_cloud_job_submission_details = self.client.addCloudJobSubmissionDetails
        self.get_cloud_job_submission = self.client.getCloudJobSubmission
        self.update_ssh_job_submission_details = self.client.updateSSHJobSubmissionDetails
        self.update_cloud_job_submission_details = self.client.updateCloudJobSubmissionDetails
        self.update_unicore_job_submission_details = self.client.updateUnicoreJobSubmissionDetails
        self.add_local_data_movement_details = self.client.addLocalDataMovementDetails
        self.update_local_data_movement_details = self.client.updateLocalDataMovementDetails
        self.get_local_data_movement = self.client.getLocalDataMovement
        self.add_scp_data_movement_details = self.client.addSCPDataMovementDetails
        self.update_scp_data_movement_details = self.client.updateSCPDataMovementDetails
        self.get_scp_data_movement = self.client.getSCPDataMovement
        self.add_unicore_data_movement_details = self.client.addUnicoreDataMovementDetails
        self.update_unicore_data_movement_details = self.client.updateUnicoreDataMovementDetails
        self.get_unicore_data_movement = self.client.getUnicoreDataMovement
        self.add_grid_ftp_data_movement_details = self.client.addGridFTPDataMovementDetails
        self.update_grid_ftp_data_movement_details = self.client.updateGridFTPDataMovementDetails
        self.get_grid_ftp_data_movement = self.client.getGridFTPDataMovement
        self.change_job_submission_priority = self.client.changeJobSubmissionPriority
        self.change_data_movement_priority = self.client.changeDataMovementPriority
        self.change_job_submission_priorities = self.client.changeJobSubmissionPriorities
        self.change_data_movement_priorities = self.client.changeDataMovementPriorities
        self.delete_job_submission_interface = self.client.deleteJobSubmissionInterface
        self.delete_data_movement_interface = self.client.deleteDataMovementInterface
        self.register_resource_job_manager = self.client.registerResourceJobManager
        self.update_resource_job_manager = self.client.updateResourceJobManager
        self.get_resource_job_manager = self.client.getResourceJobManager
        self.delete_resource_job_manager = self.client.deleteResourceJobManager
        self.delete_batch_queue = self.client.deleteBatchQueue
        self.register_gateway_resource_profile = self.client.registerGatewayResourceProfile
        self.get_gateway_resource_profile = self.client.getGatewayResourceProfile
        self.update_gateway_resource_profile = self.client.updateGatewayResourceProfile
        self.delete_gateway_resource_profile = self.client.deleteGatewayResourceProfile
        self.add_gateway_compute_resource_preference = self.client.addGatewayComputeResourcePreference
        self.add_gateway_storage_preference = self.client.addGatewayStoragePreference
        self.get_gateway_compute_resource_preference = self.client.getGatewayComputeResourcePreference
        self.get_gateway_storage_preference = self.client.getGatewayStoragePreference
        self.get_all_gateway_compute_resource_preferences = self.client.getAllGatewayComputeResourcePreferences
        self.get_all_gateway_storage_preferences = self.client.getAllGatewayStoragePreferences
        self.get_all_gateway_resource_profiles = self.client.getAllGatewayResourceProfiles
        self.update_gateway_compute_resource_preference = self.client.updateGatewayComputeResourcePreference
        self.update_gateway_storage_preference = self.client.updateGatewayStoragePreference
        self.delete_gateway_compute_resource_preference = self.client.deleteGatewayComputeResourcePreference
        self.delete_gateway_storage_preference = self.client.deleteGatewayStoragePreference
        self.get_ssh_account_provisioners = self.client.getSSHAccountProvisioners
        self.does_user_have_ssh_account = self.client.doesUserHaveSSHAccount
        self.is_ssh_setup_complete_for_user_compute_resource_preference = self.client.isSSHSetupCompleteForUserComputeResourcePreference
        self.setup_user_compute_resource_preferences_for_ssh = self.client.setupUserComputeResourcePreferencesForSSH
        self.register_user_resource_profile = self.client.registerUserResourceProfile
        self.is_user_resource_profile_exists = self.client.isUserResourceProfileExists
        self.get_user_resource_profile = self.client.getUserResourceProfile
        self.update_user_resource_profile = self.client.updateUserResourceProfile
        self.delete_user_resource_profile = self.client.deleteUserResourceProfile
        self.add_user_compute_resource_preference = self.client.addUserComputeResourcePreference
        self.add_user_storage_preference = self.client.addUserStoragePreference
        self.get_user_compute_resource_preference = self.client.getUserComputeResourcePreference
        self.get_user_storage_preference = self.client.getUserStoragePreference
        self.get_all_user_compute_resource_preferences = self.client.getAllUserComputeResourcePreferences
        self.get_all_user_storage_preferences = self.client.getAllUserStoragePreferences
        self.get_all_user_resource_profiles = self.client.getAllUserResourceProfiles
        self.update_user_compute_resource_preference = self.client.updateUserComputeResourcePreference
        self.update_user_storage_preference = self.client.updateUserStoragePreference
        self.delete_user_compute_resource_preference = self.client.deleteUserComputeResourcePreference
        self.delete_user_storage_preference = self.client.deleteUserStoragePreference
        self.get_latest_queue_statuses = self.client.getLatestQueueStatuses
        self.register_data_product = self.client.registerDataProduct
        self.get_data_product = self.client.getDataProduct
        self.register_replica_location = self.client.registerReplicaLocation
        self.get_parent_data_product = self.client.getParentDataProduct
        self.get_child_data_products = self.client.getChildDataProducts
        self.share_resource_with_users = self.client.shareResourceWithUsers
        self.share_resource_with_groups = self.client.shareResourceWithGroups
        self.revoke_sharing_of_resource_from_users = self.client.revokeSharingOfResourceFromUsers
        self.revoke_sharing_of_resource_from_groups = self.client.revokeSharingOfResourceFromGroups
        self.get_all_accessible_users = self.client.getAllAccessibleUsers
        self.get_all_accessible_groups = self.client.getAllAccessibleGroups
        self.get_all_directly_accessible_users = self.client.getAllDirectlyAccessibleUsers
        self.get_all_directly_accessible_groups = self.client.getAllDirectlyAccessibleGroups
        self.user_has_access = self.client.userHasAccess
        self.create_group_resource_profile = self.client.createGroupResourceProfile
        self.update_group_resource_profile = self.client.updateGroupResourceProfile
        self.get_group_resource_profile = self.client.getGroupResourceProfile
        self.remove_group_resource_profile = self.client.removeGroupResourceProfile
        self.get_group_resource_list = self.client.getGroupResourceList
        self.remove_group_compute_prefs = self.client.removeGroupComputePrefs
        self.remove_group_compute_resource_policy = self.client.removeGroupComputeResourcePolicy
        self.remove_group_batch_queue_resource_policy = self.client.removeGroupBatchQueueResourcePolicy
        self.get_group_compute_resource_preference = self.client.getGroupComputeResourcePreference
        self.get_group_compute_resource_policy = self.client.getGroupComputeResourcePolicy
        self.get_batch_queue_resource_policy = self.client.getBatchQueueResourcePolicy
        self.get_group_compute_resource_pref_list = self.client.getGroupComputeResourcePrefList
        self.get_group_batch_queue_resource_policy_list = self.client.getGroupBatchQueueResourcePolicyList
        self.get_group_compute_resource_policy_list = self.client.getGroupComputeResourcePolicyList
        self.get_gateway_groups = self.client.getGatewayGroups
        self.get_parser = self.client.getParser
        self.save_parser = self.client.saveParser
        self.list_all_parsers = self.client.listAllParsers
        self.remove_parser = self.client.removeParser
        self.get_parsing_template = self.client.getParsingTemplate
        self.get_parsing_templates_for_experiment = self.client.getParsingTemplatesForExperiment
        self.save_parsing_template = self.client.saveParsingTemplate
        self.remove_parsing_template = self.client.removeParsingTemplate
        self.list_all_parsing_templates = self.client.listAllParsingTemplates

    def _load_settings(self, configuration_file_location: Optional[str]):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.settings.API_SERVER_HOST = config.get('APIServer', 'API_HOST')
            self.settings.API_SERVER_PORT = config.getint('APIServer', 'API_PORT')
            self.settings.API_SERVER_SECURE = config.getboolean('APIServer', 'API_SECURE')
