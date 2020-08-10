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
import configparser

from airavata_sdk.transport.settings import APIServerClientSettings
from airavata_sdk.transport import utils

from airavata.api.error.ttypes import InvalidRequestException, AiravataClientException, AiravataSystemException, \
    AuthorizationException

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class APIServerClient(object):

    def __init__(self, configuration_file_location=None):
        self.api_server_settings = APIServerClientSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.api_server_client_pool = utils.initialize_api_client_pool(self.api_server_settings.API_SERVER_HOST,
                                                                       self.api_server_settings.API_SERVER_PORT,
                                                                       self.api_server_settings.API_SERVER_SECURE)

    def is_user_exists(self, authz_token, gateway_id, user_name):
        """
        :param authz_token:
        :param gateway_id:
        :param user_name:
        :return: true/false
        """
        try:
            return self.api_server_client_pool.isUserExists(authz_token, gateway_id, user_name)
        except InvalidRequestException:
            logger.exception("Error occurred in is_user_exists, probably due to invalid parameters ")
            raise
        except AiravataClientException:
            logger.exception("Error occurred in is_user_exists, probably due to  client misconfiguration ")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in is_user_exists, probably due to server side error ")
            raise
        except AuthorizationException:
            logger.exception("Error occurred in is_user_exists, probably due to invalid authz token ")
            raise

    def add_gateway(self, authz_token, gateway):
        """
        :param authz_token:
        :param gateway:
        :return: gatewayId
        """
        try:
            return self.api_server_client_pool.addGateway(authz_token, gateway)
        except InvalidRequestException:
            logger.exception("Error occurred in add_gateway, probably due to invalid parameters ")
            raise
        except AiravataClientException:
            logger.exception("Error occurred in add_gateway, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in add_gateway, probably due to server side error ",
                             )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in add_gateway, probably due to invalid authz token ",
                             )
            raise

    def get_all_users_in_gateway(self, authz_token, gateway_id):
        """
        :param authz_token:
        :param gateway_id:
        :return:
        """
        try:
            return self.api_server_client_pool.getAllUsersInGateway(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_users_in_gateway, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_all_users_in_gateway, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_users_in_gateway, probably due to server side error ",
                             )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_all_users_in_gateway, probably due to invalid authz token ",
                             )
            raise

    def update_gateway(self, authz_token, gateway_id, updated_gateway):
        """
        update  the gateway with gateway_id with provided information
        :param authz_token:
        :param gateway_id:
        :param updated_gateway:
        :return:
        """
        try:
            return self.api_server_client_pool.updateGateway(authz_token, gateway_id, updated_gateway)
        except InvalidRequestException:
            logger.exception("Error occurred in update_gateway, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in update_gateway, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_gateway, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_gateway, probably due to invalid authz token ",
                             )
            raise

    def get_gateway(self, authz_token, gateway_id):
        """
        return gateway provided by the gateway_id
        :param authz_token:
        :param gateway_id:
        :param updated_gateway:
        :return: gateway
        """
        try:
            return self.api_server_client_pool.getGateway(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_gateway, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_gateway, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_gateway, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_gateway, probably due to invalid authz token ",
                             )
            raise

    def delete_gateway(self, authz_token, gateway_id):
        """
        delete the given gateway
        :param authz_token:
        :param gateway_id:
        :return: true/false
        """
        try:
            return self.api_server_client_pool.deleteGateway(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_gateway, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in delete_gateway, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_gateway, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_gateway, probably due to invalid authz token ",
                             )
            raise

    def get_all_gateways(self, authz_token):
        """
        get all gateways
        :param authz_token:
        :return: gateways
        """
        try:
            return self.api_server_client_pool.getAllGateways(authz_token)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_gateways, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_all_gateways, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_gateways, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_all_gateways, probably due to invalid authz token ",
                             )
            raise

    def is_gateway_exist(self, authz_token, gateway_id):
        """
        return gateway exists
        :param authz_token:
        :param gateway_id:
        :return: true/false
        """
        try:
            return self.api_server_client_pool.isGatewayExist(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in is_gateway_exist, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in is_gateway_exist, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in is_gateway_exist, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in is_gateway_exist, probably due to invalid authz token ")
            raise

    def create_notification(self, authz_token, notification):
        """
        create notification
        :param authz_token:
        :param notification:
        :return: notification id
        """
        try:
            return self.api_server_client_pool.createNotification(authz_token, notification)
        except InvalidRequestException:
            logger.exception("Error occurred in create_notification, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in create_notification, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in create_notification, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in create_notification, probably due to invalid authz token ",
                             )
            raise

    def update_notification(self, authz_token, notification):
        """
        update notification
        :param authz_token:
        :param notification:
        :return: true /false
        """
        try:
            return self.api_server_client_pool.updateNotification(authz_token, notification)
        except InvalidRequestException:
            logger.exception("Error occurred in update_notification, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in update_notification, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_notification, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_notification, probably due to invalid authz token ",
                             )
            raise

    def delete_notification(self, authz_token, gateway_id, notification_id):
        """
        delete notification
        :param authz_token:
        :param gateway_id:
        :param notification_id:
        :return: true/false
        """
        try:
            return self.api_server_client_pool.deleteNotification(authz_token, gateway_id, notification_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_notification, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in delete_notification, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_notification, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_notification, probably due to invalid authz token ",
                             )
            raise

    def get_notification(self, authz_token, gateway_id, notification_id):
        """
        get notification
        :param authz_token:
        :param gateway_id:
        :param notification_id:
        :return: notification
        """
        try:
            return self.api_server_client_pool.getNotification(authz_token, gateway_id, notification_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_notification, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_notification, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_notification, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_notification, probably due to invalid authz token ",
                             )
            raise

    def get_all_notifications(self, authz_token, gateway_id):
        """
        get all notifications
        :param authz_token:
        :param gateway_id:
        :param notification_id:
        :return: notifications
        """
        try:
            return self.api_server_client_pool.getAllNotifications(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_notifications, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_all_notifications, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_notifications, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_all_notifications, probably due to invalid authz token ",
                             )
            raise

    def generate_and_register_ssh_keys(self, authz_token, description):
        """
        Generate and Register SSH Key Pair with Airavata Credential Store.

        @param description
           The description field for a credential type, all type of credential can have a description.

        @return airavataCredStoreToken
          An SSH Key pair is generated and stored in the credential store and associated with users or community account
          belonging to a Gateway.



        Parameters:
         - authz_token
         - description
        """
        try:
            return self.api_server_client_pool.generateAndRegisterSSHKeys(authz_token, description)
        except InvalidRequestException:
            logger.exception("Error occurred in generate_and_register_ssh_keys, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in generate_and_register_ssh_keys, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in generate_and_register_ssh_keys, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in generate_and_register_ssh_keys, probably due to invalid authz token ",
                             )
            raise

    def register_pwd_credential(self, authz_token, login_user_name, password, description):
        """
        Generate and Register Username PWD Pair with Airavata Credential Store.

        @param loginUserName

        @param password

        @return airavataCredStoreToken
          An SSH Key pair is generated and stored in the credential store and associated with users or community account
          belonging to a Gateway.
        Parameters:
         :param description:
         :param authz_token:
         :param login_user_name:
         :param authz_token:
        """
        try:
            return self.api_server_client_pool.registerPwdCredential(authz_token, login_user_name, password,
                                                                     description)
        except InvalidRequestException:
            logger.exception("Error occurred in register_pwd_credential, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in register_pwd_credential, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in register_pwd_credential, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in register_pwd_credential, probably due to invalid authz token ",
                             )
            raise

    def get_credential_summary(self, authz_token, token_id):
        """
        get credential summary
        Parameters:
         - authz_token
         - tokenId
        """
        try:
            return self.api_server_client_pool.getCredentialSummary(authz_token, token_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_credential_summary, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_credential_summary, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_credential_summary, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_credential_summary, probably due to invalid authz token ",
                             )
            raise

    def get_all_credential_summaries(self, authz_token, type):
        """
        Parameters:
         - authz_token
         - type
        """
        try:
            return self.api_server_client_pool.getAllCredentialSummaries(authz_token, type)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_credential_summaries, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_credential_summaries, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_credential_summaries, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_all_credential_summaries, probably due to invalid authz token ",
                             )
            raise

    def delete_ssh_pub_key(self, authz_token, airavata_cred_store_token):
        """
        Parameters:
         - authz_token
         - airavataCredStoreToken
        """
        try:
            return self.api_server_client_pool.delete_ssh_pub_key(authz_token, airavata_cred_store_token)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_ssh_pub_key, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in delete_ssh_pub_key, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_ssh_pub_key, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_ssh_pub_key, probably due to invalid authz token ",
                             )
            raise

    def delete_pwd_credential(self, authz_token, airavata_cred_store_token):
        """
        Parameters:
         - authz_token
         - airavataCredStoreToken
        """
        try:
            return self.api_server_client_pool.deletePWDCredential(authz_token, airavata_cred_store_token)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_pwd_credential, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in delete_pwd_credential, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_pwd_credential, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_pwd_credential, probably due to invalid authz token ",
                             )
            raise

    def create_project(self, authz_token, gateway_id, project):
        """

        Creates a Project with basic metadata.
           A Project is a container of experiments.

        @param gatewayId
           The identifier for the requested gateway.

        @param Project
           The Project Object described in the workspace_model.



        Parameters:
         - authz_token
         - gatewayId
         - project
        """
        try:
            return self.api_server_client_pool.createProject(authz_token, gateway_id, project)
        except InvalidRequestException:
            logger.exception("Error occurred in create_project, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in create_project, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in create_project, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in create_project, probably due to invalid authz token ",
                             )
            raise

    def update_project(self, authz_token, project_id, updated_project):
        """

        Update an Existing Project

        @param projectId
           The projectId of the project needed an update.

        @return void
           Currently this does not return any value.



        Parameters:
         - authz_token
         - projectId
         - updatedProject
        """
        try:
            return self.api_server_client_pool.updateProject(authz_token, project_id, updated_project)
        except InvalidRequestException:
            logger.exception("Error occurred in update_project, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in update_project, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_project, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_project, probably due to invalid authz token ",
                             )
            raise

    def get_project(self, authz_token, project_id):
        """

        Get a Project by ID
           This method is to obtain a project by providing a projectId.

        @param projectId
           projectId of the project you require.

        @return project
           project data model will be returned.



        Parameters:
         - authz_token
         - projectId
        """
        try:
            return self.api_server_client_pool.getProject(authz_token, project_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_project, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_project, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_project, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_project, probably due to invalid authz token ",
                             )
            raise

    def delete_project(self, authz_token, project_id):
        """

        Delete a Project
           This method is used to delete an existing Project.

        @param projectId
           projectId of the project you want to delete.

        @return boolean
           Boolean identifier for the success or failure of the deletion operation.

           NOTE: This method is not used within gateways connected with Airavata.



        Parameters:
         - authz_token
         - projectId
        """
        try:
            return self.api_server_client_pool.deleteProject(authz_token, project_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_project, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_project, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_project, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_project, probably due to invalid authz token ",
                             )
            raise

    def get_user_projects(self, authz_token, gateway_id, user_name, limit, offset):
        """

        Get All User Projects
        Get all Project for the user with pagination. Results will be ordered based on creation time DESC.

        @param gateway_id
           The identifier for the requested gateway.

        @param user_name
           The identifier of the user.

        @param limit
           The amount results to be fetched.

        @param offset
           The starting point of the results to be fetched.

        Parameters:
         - authz_token
         - gateway_id
         - userName
         - limit
         - offset
        """
        try:
            return self.api_server_client_pool.getUserProjects(authz_token, gateway_id, user_name, limit, offset)
        except InvalidRequestException:
            logger.exception("Error occurred in get_project, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_project, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_project, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_project, probably due to invalid authz token ",
                             )
            raise

    def search_projects(self, authz_token, gateway_id, user_name, filters, limit, offset):
        """

        Search User Projects
        Search and get all Projects for user by project description or/and project name  with pagination.
        Results will be ordered based on creation time DESC.

        @param gatewayId
           The unique identifier of the gateway making the request.

        @param userName
           The identifier of the user.

        @param filters
           Map of multiple filter criteria. Currenlt search filters includes Project Name and Project Description

        @param limit
           The amount results to be fetched.

        @param offset
           The starting point of the results to be fetched.



        Parameters:
         - authz_token
         - gateway_id
         - user_name
         - filters
         - limit
         - offset
        """
        try:
            return self.api_server_client_pool.searchProjects(authz_token, gateway_id, user_name, filters, limit,
                                                              offset)
        except InvalidRequestException:
            logger.exception("Error occurred in search_projects, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in search_projects, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in search_projects, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in search_projects, probably due to invalid authz token ",
                             )
            raise

    def search_experiments(self, authz_token, gateway_id, user_name, filters, limit, offset):
        """
        Search Experiments.
        Search Experiments by using multiple filter criteria with pagination. Results will be sorted based on creation time DESC.

        @param gatewayId
              Identifier of the requested gateway.

        @param userName
              Username of the user requesting the search function.

        @param filters
              Map of multiple filter criteria. Currenlt search filters includes Experiment Name, Description, Application, etc....

        @param limit
              Amount of results to be fetched.

        @param offset
              The starting point of the results to be fetched.

        @return ExperimentSummaryModel
           List of experiments for the given search filter. Here only the Experiment summary will be returned.



        Parameters:
         - authz_token
         - gatewayId
         - userName
         - filters
         - limit
         - offset
        """
        try:
            return self.api_server_client_pool.searchExperiments(authz_token, gateway_id, user_name, filters, limit,
                                                                 offset)
        except InvalidRequestException:
            logger.exception("Error occurred in search_experiments, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in search_experiments, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in search_experiments, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in search_experiments, probably due to invalid authz token ",
                             )
            raise

    def get_experiment_statistics(self, authz_token, gateway_id, from_time, to_time, user_name, application_name,
                                  resource_host_name):
        """

        Get Experiment Statistics
        Get Experiment Statisitics for a given gateway for a specific time period. This feature is available only for admins of a particular gateway. Gateway admin access is managed by the user roles.

        @param gateway_id
              Unique identifier of the gateway making the request to fetch statistics.

        @param from_time
              Starting date time.

        @param to_time
              Ending data time.

        @param user_name
              Gateway username substring with which to further filter statistics.

        @param application_name
              Application id substring with which to further filter statistics.

        @param resource_host_name
              Hostname id substring with which to further filter statistics.



        Parameters:
         - authz_token
         - gateway_id
         - from_time
         - to_time
         - user_name
         - application_name
         - resource_host_name
        """
        try:
            return self.api_server_client_pool.getExperimentStatistics(authz_token, gateway_id, from_time, to_time,
                                                                       user_name, application_name, resource_host_name)
        except InvalidRequestException:
            logger.exception("Error occurred in get_experiment_statistics, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_experiment_statistics, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_experiment_statistics, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_experiment_statistics, probably due to invalid authz token ",
                             )
            raise

    def get_experiments_in_project(self, authz_token, project_id, limit, offset):
        """

        Get All Experiments of the Project
        Get Experiments within project with pagination. Results will be sorted based on creation time DESC.

        @param project_id
              Uniqie identifier of the project.

        @param limit
              Amount of results to be fetched.

        @param offset
              The starting point of the results to be fetched.



        Parameters:
         - authz_token
         - project_id
         - limit
         - offset
        """
        try:
            return self.api_server_client_pool.getExperimentsInProject(authz_token, project_id, limit, offset)
        except InvalidRequestException:
            logger.exception("Error occurred in get_experiments_in_project, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_experiments_in_project, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_experiments_in_project, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_experiments_in_project, probably due to invalid authz token ",
                             )
            raise

    def get_user_experiments(self, authz_token, gateway_id, user_name, limit, offset):
        """

        Get All Experiments of the User
        Get experiments by user with pagination. Results will be sorted based on creation time DESC.

        @param gatewayId
              Identifier of the requesting gateway.

        @param userName
              Username of the requested end user.

        @param limit
              Amount of results to be fetched.

        @param offset
              The starting point of the results to be fetched.



        Parameters:
         - authz_token
         - gatewayId
         - userName
         - limit
         - offset
        """
        try:
            return self.api_server_client_pool.getUserExperiments(authz_token, gateway_id, user_name, limit, offset)
        except InvalidRequestException:
            logger.exception("Error occurred in get_user_experiments, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_user_experiments, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_user_experiments, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_user_experiments, probably due to invalid authz token ",
                             )
            raise

    def create_experiment(self, authz_token, gateway_id, experiment):
        """
          *
          * Create New Experiment
          * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
          *   but inferred from the sshKeyAuthentication header. This experiment is just a persistent place holder. The client
          *   has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
          *   registering the experiment in a persistent store.
          *
          * @param gatewayId
          *    The unique ID of the gateway where the experiment is been created.
          *
          * @param ExperimentModel
          *    The create experiment will require the basic experiment metadata like the name and description, intended user,
          *      the gateway identifer and if the experiment should be shared public by defualt. During the creation of an experiment
          *      the ExperimentMetadata is a required field.
          *
          * @return
          *   The server-side generated.airavata.registry.core.experiment.globally unique identifier.
          *
          * @throws org.apache.airavata.model.error.InvalidRequestException
          *    For any incorrect forming of the request itself.
          *
          * @throws org.apache.airavata.model.error.AiravataClientException
          *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
          *
          *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
          *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
          *         gateway registration steps and retry this request.
          *
          *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
          *         For now this is a place holder.
          *
          *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
          *         is implemented, the authorization will be more substantial.
          *
          * @throws org.apache.airavata.model.error.AiravataSystemException
          *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
          *       rather an Airavata Administrator will be notified to take corrective action.
          *
        *

        Parameters:
         - authz_token
         - gatewayId
         - experiment
        """
        try:
            return self.api_server_client_pool.createExperiment(authz_token, gateway_id, experiment)
        except InvalidRequestException:
            logger.exception("Error occurred in create_experiment, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in create_experiment, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in create_experiment, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in create_experiment, probably due to invalid authz token ",
                             )
            raise

    def delete_experiment(self, authz_token, experiment_id):
        """

        Delete an Experiment
        If the experiment is not already launched experiment can be deleted.

        @param authz_token

        @param experiment_id
            Experiment ID of the experimnet you want to delete.

        @return boolean
            Identifier for the success or failure of the deletion operation.



        Parameters:
         - authz_token
         - experimentId
        """
        try:
            return self.api_server_client_pool.deleteExperiment(authz_token, experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in create_experiment, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in create_experiment, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in create_experiment, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in create_experiment, probably due to invalid authz token ",
                             )
            raise

    def get_experiment(self, authz_token, airavata_experiment_id):
        """
          *
          * Get Experiment
          * Fetch previously created experiment metadata.
          *
          * @param airavataExperimentId
          *    The unique identifier of the requested experiment. This ID is returned during the create experiment step.
          *
          * @return ExperimentModel
          *   This method will return the previously stored experiment metadata.
          *
          * @throws org.apache.airavata.model.error.InvalidRequestException
          *    For any incorrect forming of the request itself.
          *
          * @throws org.apache.airavata.model.error.ExperimentNotFoundException
          *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
          *
          * @throws org.apache.airavata.model.error.AiravataClientException
          *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
          *
          *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
          *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
          *         gateway registration steps and retry this request.
          *
          *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
          *         For now this is a place holder.
          *
          *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
          *         is implemented, the authorization will be more substantial.
          *
          * @throws org.apache.airavata.model.error.AiravataSystemException
          *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
          *       rather an Airavata Administrator will be notified to take corrective action.
          *
        *

        Parameters:
         - authz_token
         - airavataExperimentId
        """
        try:
            return self.api_server_client_pool.getExperiment(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_experiment, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_experiment, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_experiment, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_experiment, probably due to invalid authz token ",
                             )
            raise

    def get_experiment_by_admin(self, authz_token, airavata_experiment_id):
        """
          *
          * Get Experiment by an admin user
          *
          * Used by an admin user to fetch previously created experiment metadata.
          *
          * @param airavataExperimentId
          *    The unique identifier of the requested experiment. This ID is returned during the create experiment step.
          *
          * @return ExperimentModel
          *   This method will return the previously stored experiment metadata.
          *
          * @throws org.apache.airavata.model.error.InvalidRequestException
          *    For any incorrect forming of the request itself.
          *
          * @throws org.apache.airavata.model.error.ExperimentNotFoundException
          *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
          *
          * @throws org.apache.airavata.model.error.AiravataClientException
          *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
          *
          *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
          *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
          *         gateway registration steps and retry this request.
          *
          *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
          *         For now this is a place holder.
          *
          *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
          *         is implemented, the authorization will be more substantial.
          *
          * @throws org.apache.airavata.model.error.AiravataSystemException
          *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
          *       rather an Airavata Administrator will be notified to take corrective action.
          *
        *

        Parameters:
         - authz_token
         - airavataExperimentId
        """
        try:
            return self.api_server_client_pool.get_experiment_by_admin(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_experiment_by_admin, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_experiment_by_admin, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_experiment_by_admin, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_experiment_by_admin, probably due to invalid authz token ",
                             )
            raise

    def get_detailed_experiment_tree(self, authz_token, airavata_experiment_id):
        """

        Get Complete Experiment Details
        Fetch the completed nested tree structue of previously created experiment metadata which includes processes ->
        tasks -> jobs information.

        @param airavataExperimentId
           The identifier for the requested experiment. This is returned during the create experiment step.

        @return ExperimentModel
          This method will return the previously stored experiment metadata including application input parameters, computational resource scheduling
          information, special input output handling and additional quality of service parameters.

        @throws org.apache.airavata.model.error.InvalidRequestException
           For any incorrect forming of the request itself.

        @throws org.apache.airavata.model.error.ExperimentNotFoundException
           If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.

        @throws org.apache.airavata.model.error.AiravataClientException
           The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:

             UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
                step, then Airavata Registry will not have a provenance area setup. The client has to follow
                gateway registration steps and retry this request.

             AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
                For now this is a place holder.

             INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
                is implemented, the authorization will be more substantial.

        @throws org.apache.airavata.model.error.AiravataSystemException
           This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
              rather an Airavata Administrator will be notified to take corrective action.


        Parameters:
         - authz_token
         - airavataExperimentId
        """
        try:
            return self.api_server_client_pool.getDetailedExperimentTree(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_detailed_experiment_tree, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to invalid authz token ",
                             )
            raise

    def update_experiment(self, authz_token, airavata_experiment_id, experiment):
        """

        Update a Previously Created Experiment
        Configure the CREATED experiment with required inputs, scheduling and other quality of service parameters. This method only updates the experiment object within the registry.
        The experiment has to be launched to make it actionable by the server.

        @param airavataExperimentId
           The identifier for the requested experiment. This is returned during the create experiment step.

        @param ExperimentModel
           The configuration information of the experiment with application input parameters, computational resource scheduling
             information, special input output handling and additional quality of service parameters.

        @return
          This method call does not have a return value.

        @throws org.apache.airavata.model.error.InvalidRequestException
           For any incorrect forming of the request itself.

        @throws org.apache.airavata.model.error.ExperimentNotFoundException
           If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.

        @throws org.apache.airavata.model.error.AiravataClientException
           The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:

             UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
                step, then Airavata Registry will not have a provenance area setup. The client has to follow
                gateway registration steps and retry this request.

             AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
                For now this is a place holder.

             INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
                is implemented, the authorization will be more substantial.

        @throws org.apache.airavata.model.error.AiravataSystemException
           This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
              rather an Airavata Administrator will be notified to take corrective action.


        Parameters:
         - authz_token
         - airavataExperimentId
         - experiment
        """
        try:
            return self.api_server_client_pool.updateExperiment(authz_token, airavata_experiment_id, experiment)
        except InvalidRequestException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_detailed_experiment_tree, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to invalid authz token ",
                             )
            raise

    def update_experiment_configuration(self, authz_token, airavata_experiment_id, user_configuration):
        """
        Parameters:
         - authz_token
         - airavata_experiment_id
         - user_configuration
        """
        try:
            return self.api_server_client_pool.updateExperimentConfiguration(authz_token, airavata_experiment_id,
                                                                             user_configuration)
        except InvalidRequestException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_detailed_experiment_tree, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_detailed_experiment_tree, probably due to invalid authz token ",
                             )
            raise

    def update_resource_scheduling(self, authz_token, airavata_experiment_id, resource_scheduling):
        """
        Parameters:
         - authz_token
         - airavata_experiment_id
         - resource_scheduling
        """
        try:
            return self.api_server_client_pool.updateResourceScheduleing(authz_token, airavata_experiment_id,
                                                                         resource_scheduling)
        except InvalidRequestException:
            logger.exception("Error occurred in update_resource_scheduling, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in update_resource_scheduling, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_resource_scheduling, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_resource_scheduling, probably due to invalid authz token ",
                             )
            raise

    def validate_experiment(self, authz_token, airavata_experiment_id):
        """
         *
         * Validate experiment configuration.
         * A true in general indicates, the experiment is ready to be launched.
         *
         * @param airavataExperimentId
         *    Unique identifier of the experiment (Experimnent ID) of the experiment which need to be validated.
         *
         * @return boolean
         *      Identifier for the success or failure of the validation operation.
         *
        *

        Parameters:
         - authz_token
         - airavata_experiment_id
        """
        try:
            return self.api_server_client_pool.validateExperiment(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in update_resource_scheduling, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in update_resource_scheduling, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_resource_scheduling, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_resource_scheduling, probably due to invalid authz token ",
                             )
            raise

    def launch_experiment(self, authz_token, airavata_experiment_id, gateway_id):
        """

        Launch a Previously Created & Configured Experiment.
        Airavata Server will then start processing the request and appropriate notifications and intermediate and output data will be subsequently available for this experiment.

        @gateway_id
           ID of the gateway which will launch the experiment.

        @param airavata_experiment_id
           The identifier for the requested experiment. This is returned during the create experiment step.

        @return
          This method call does not have a return value.

        @throws org.apache.airavata.model.error.InvalidRequestException
           For any incorrect forming of the request itself.

        @throws org.apache.airavata.model.error.ExperimentNotFoundException
           If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.

        @throws org.apache.airavata.model.error.AiravataClientException
           The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:

             UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
                step, then Airavata Registry will not have a provenance area setup. The client has to follow
                gateway registration steps and retry this request.

             AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
                For now this is a place holder.

             INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
                is implemented, the authorization will be more substantial.

        @throws org.apache.airavata.model.error.AiravataSystemException
           This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
              rather an Airavata Administrator will be notified to take corrective action.


        Parameters:
         - authz_token
         - airavata_experiment_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.launchExperiment(authz_token, airavata_experiment_id, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in launch_experiment, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in launch_experiment, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in launch_experiment, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in launch_experiment, probably due to invalid authz token ",
                             )
            raise

    def get_experiment_status(self, authz_token, airavata_experiment_id):
        """

        Get Experiment Status

        Obtain the status of an experiment by providing the Experiment Id

        @param authz_token

        @param airavata_experiment_id
            Experiment ID of the experimnet you require the status.

        @return ExperimentStatus
            ExperimentStatus model with the current status will be returned.



        Parameters:
         - authz_token
         - airavataExperimentId
        """
        try:
            return self.api_server_client_pool.getExperimentStatus(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_experiment_status, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_experiment_status, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_experiment_status, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_experiment_status, probably due to invalid authz token ",
                             )
            raise

    def get_experiment_outputs(self, authz_token, airavata_experiment_id):
        """

        Get Experiment Outputs
        This method to be used when need to obtain final outputs of a certain Experiment

        @param authz_token

        @param airavata_experiment_id
            Experiment ID of the experimnet you need the outputs.

        @return list
            List of experiment outputs will be returned. They will be returned as a list of OutputDataObjectType for the experiment.



        Parameters:
         - authz_token
         - airavata_experiment_id
        """
        try:
            return self.api_server_client_pool.getExperimentOutputs(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_experiment_outputs, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_experiment_outputs, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_experiment_outputs, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_experiment_outputs, probably due to invalid authz token ",
                             )
            raise

    def get_intermediate_outputs(self, authz_token, airavata_experiment_id):
        """

        Get Intermediate Experiment Outputs
        This method to be used when need to obtain intermediate outputs of a certain Experiment

        @param authz_token

        @param airavataExperimentId
            Experiment ID of the experimnet you need intermediate outputs.

        @return list
            List of intermediate experiment outputs will be returned. They will be returned as a list of OutputDataObjectType for the experiment.



        Parameters:
         - authz_token
         - airavataExperimentId
        """
        try:
            return self.api_server_client_pool.getIntermediateOutputs(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_intermediate_outputs, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_intermediate_outputs, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_intermediate_outputs, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_intermediate_outputs, probably due to invalid authz token ",
                             )
            raise

    def get_job_statuses(self, authz_token, airavata_experiment_id):
        """

        Get Job Statuses for an Experiment
        This method to be used when need to get the job status of an Experiment. An experiment may have one or many jobs; there for one or many job statuses may turnup

        @param authz_token

        @param airavata_experiment_id
            Experiment ID of the experimnet you need the job statuses.

        @return JobStatus
            Job status (string) for all all the existing jobs for the experiment will be returned in the form of a map



        Parameters:
         - authz_token
         - airavata_experiment_id
        """
        try:
            return self.api_server_client_pool.getJobStatuses(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_intermediate_outputs, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_intermediate_outputs, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_intermediate_outputs, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_intermediate_outputs, probably due to invalid authz token ",
                             )
            raise

    def get_job_details(self, authz_token, airavata_experiment_id):
        """

        Get Job Details for all the jobs within an Experiment.
        This method to be used when need to get the job details for one or many jobs of an Experiment.

        @param authz_token

        @param airavata_experiment_id
            Experiment ID of the experimnet you need job details.

        @return list of JobDetails
            Job details.



        Parameters:
         - authz_token
         - airavata_experiment_id
        """
        try:
            return self.api_server_client_pool.getJobDetails(authz_token, airavata_experiment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_job_details, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_job_details, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_job_details, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_job_details, probably due to invalid authz token ",
                             )
            raise

    def clone_experiment(self, authz_token, existing_experiment_id, new_experiment_name, new_experiment_projectId):
        """

        Clone an Existing Experiment
        Existing specified experiment is cloned and a new name is provided. A copy of the experiment configuration is made and is persisted with new metadata.
          The client has to subsequently update this configuration if needed and launch the cloned experiment.

        @param existing_experiment_id
           experiment name that should be used in the cloned experiment

        @param new_experiment_name
           Once an experiment is cloned, to disambiguate, the users are suggested to provide new metadata. This will again require
             the basic experiment metadata like the name and description, intended user, the gateway identifier and if the experiment
             should be shared public by default.
        @param new_experiment_projectId
           The project in which to create the cloned experiment. This is optional and if null the experiment will be created
             in the same project as the existing experiment.

        @return
          The server-side generated.airavata.registry.core.experiment.globally unique identifier (Experiment ID) for the newly cloned experiment.

        @throws org.apache.airavata.model.error.InvalidRequestException
           For any incorrect forming of the request itself.

        @throws org.apache.airavata.model.error.ExperimentNotFoundException
           If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.

        @throws org.apache.airavata.model.error.AiravataClientException
           The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:

             UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
                step, then Airavata Registry will not have a provenance area setup. The client has to follow
                gateway registration steps and retry this request.

             AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
                For now this is a place holder.

             INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
                is implemented, the authorization will be more substantial.

        @throws org.apache.airavata.model.error.AiravataSystemException
           This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
              rather an Airavata Administrator will be notified to take corrective action.


        Parameters:
         - authz_token
         - existing_experiment_id
         - new_experiment_name
         - new_experiment_projectId
        """
        try:
            return self.api_server_client_pool.cloneExperiment(authz_token, existing_experiment_id, new_experiment_name,
                                                               new_experiment_projectId)
        except InvalidRequestException:
            logger.exception("Error occurred in get_job_details, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_job_details, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_job_details, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_job_details, probably due to invalid authz token ",
                             )
            raise

    def clone_experiment_by_admin(self, authz_token, existing_experiment_id, new_experiment_name,
                                  new_experiment_projectId):
        """

        Clone an Existing Experiment by an admin user
        Existing specified experiment is cloned and a new name is provided. A copy of the experiment configuration is made and is persisted with new metadata.
          The client has to subsequently update this configuration if needed and launch the cloned experiment.

        @param new_experiment_name
           experiment name that should be used in the cloned experiment

        @param existing_experiment_id
           Once an experiment is cloned, to disambiguate, the users are suggested to provide new metadata. This will again require
             the basic experiment metadata like the name and description, intended user, the gateway identifier and if the experiment
             should be shared public by default.
        @param new_experiment_projectId
           The project in which to create the cloned experiment. This is optional and if null the experiment will be created
             in the same project as the existing experiment.

        @return
          The server-side generated.airavata.registry.core.experiment.globally unique identifier (Experiment ID) for the newly cloned experiment.

        @throws org.apache.airavata.model.error.InvalidRequestException
           For any incorrect forming of the request itself.

        @throws org.apache.airavata.model.error.ExperimentNotFoundException
           If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.

        @throws org.apache.airavata.model.error.AiravataClientException
           The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:

             UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
                step, then Airavata Registry will not have a provenance area setup. The client has to follow
                gateway registration steps and retry this request.

             AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
                For now this is a place holder.

             INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
                is implemented, the authorization will be more substantial.

        @throws org.apache.airavata.model.error.AiravataSystemException
           This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
              rather an Airavata Administrator will be notified to take corrective action.


        Parameters:
         - authz_token
         - existing_experiment_id
         - new_experiment_name
         - new_experiment_projectId
        """
        try:
            return self.api_server_client_pool.cloneExperimentByAdmin(authz_token, existing_experiment_id,
                                                                      new_experiment_name,
                                                                      new_experiment_projectId)
        except InvalidRequestException:
            logger.exception("Error occurred in get_job_details, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_job_details, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_job_details, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_job_details, probably due to invalid authz token ",
                             )
            raise

    def terminate_experiment(self, authz_token, airavata_experiment_id, gateway_id):
        """

        Terminate a running Experiment.

        @gateway_id
           ID of the gateway which will terminate the running Experiment.

        @param airavata_experiment_id
           The identifier of the experiment required termination. This ID is returned during the create experiment step.

        @return status
          This method call does not have a return value.

        @throws org.apache.airavata.model.error.InvalidRequestException
           For any incorrect forming of the request itself.

        @throws org.apache.airavata.model.error.ExperimentNotFoundException
           If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.

        @throws org.apache.airavata.model.error.AiravataClientException
           The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:

             UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
                step, then Airavata Registry will not have a provenance area setup. The client has to follow
                gateway registration steps and retry this request.

             AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
                For now this is a place holder.

             INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
                is implemented, the authorization will be more substantial.

        @throws org.apache.airavata.model.error.AiravataSystemException
           This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
              rather an Airavata Administrator will be notified to take corrective action.


        Parameters:
         - authz_token
         - airavata_experiment_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.terminateExperiment(authz_token, airavata_experiment_id,
                                                                   gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_job_details, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_job_details, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_job_details, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_job_details, probably due to invalid authz token ",
                             )
            raise

    def register_application_module(self, authz_token, gateway_id, application_module):
        """

        Register a Application Module.

        @gatewayId
           ID of the gateway which is registering the new Application Module.

        @param applicationModule
           Application Module Object created from the datamodel.

        @return appModuleId
          Returns the server-side generated airavata appModule globally unique identifier.


        Parameters:
         - authz_token
         - gateway_id
         - application_module
        """
        try:
            return self.api_server_client_pool.registerApplicationModule(authz_token, gateway_id,
                                                                         application_module)
        except InvalidRequestException:
            logger.exception("Error occurred in get_job_details, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_job_details, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_job_details, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_job_details, probably due to invalid authz token ",
                             )
            raise

    def get_application_module(self, authz_token, app_module_id):
        """

        Fetch a Application Module.

        @param app_module_id
          The unique identifier of the application module required

        @return applicationModule
          Returns an Application Module Object.


        Parameters:
         - authz_token
         - app_module_id
        """
        try:
            return self.api_server_client_pool.getApplicationModule(authz_token, app_module_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_module, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_application_module, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_module, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_module, probably due to invalid authz token ",
                             )
            raise

    def update_application_module(self, authz_token, app_module_id, application_module):
        """

        Update a Application Module.

        @param app_module_id
          The identifier for the requested application module to be updated.

        @param application_module
           Application Module Object created from the datamodel.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - appModuleId
         - applicationModule
        """
        try:
            return self.api_server_client_pool.updateApplicationModule(authz_token, app_module_id, application_module)
        except InvalidRequestException:
            logger.exception("Error occurred in update_application_module, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in update_application_module, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_application_module, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_application_module, probably due to invalid authz token ",
                             )
            raise

    def get_all_app_modules(self, authz_token, gateway_id):
        """

        Fetch all Application Module Descriptions.

        @param gateway_id
           ID of the gateway which need to list all available application deployment documentation.

        @return list
           Returns the list of all Application Module Objects.


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getAllAppModules(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_app_modules, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_all_app_modules, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_app_modules, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_all_app_modules, probably due to invalid authz token ",
                             )
            raise

    def get_accessible_app_modules(self, authz_token, gateway_id):
        """

        Fetch all accessible Application Module Descriptions.

        @param gateway_id
           ID of the gateway which need to list all accessible application deployment documentation.

        @return list
           Returns the list of all Application Module Objects that are accessible to the user.


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getAccessibleAppModules(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_accessible_app_modules, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_accessible_app_modules, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_accessible_app_modules, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_accessible_app_modules, probably due to invalid authz token ",
                             )
            raise

    def delete_application_module(self, authz_token, app_module_id):
        """

        Delete an Application Module.

        @param appModuleId
          The identifier of the Application Module to be deleted.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - appModuleId
        """
        try:
            return self.api_server_client_pool.deleteApplicationModule(authz_token, app_module_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_application_module, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in delete_application_module, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_application_module, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_application_module, probably due to invalid authz token ",
                             )
            raise

    def register_application_deployment(self, authz_token, gateway_id, application_deployment):
        """

        Register an Application Deployment.

        @param gateway_id
           ID of the gateway which is registering the new Application Deployment.

        @param application_deployment
           Application Module Object created from the datamodel.

        @return appDeploymentId
          Returns a server-side generated airavata appDeployment globally unique identifier.


        Parameters:
         - authz_token
         - gateway_id
         - application_deployment
        """
        try:
            return self.api_server_client_pool.registerApplicationDeployment(authz_token, gateway_id,
                                                                             application_deployment)
        except InvalidRequestException:
            logger.exception("Error occurred in register_application_deployment, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in register_application_deployment, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in register_application_deployment, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in register_application_deployment, probably due to invalid authz token ",
                             )
            raise

    def get_application_deployment(self, authz_token, app_deployment_id):
        """

        Fetch a Application Deployment.

        @param app_deployment_id
          The identifier for the requested application module

        @return applicationDeployment
          Returns a application Deployment Object.


        Parameters:
         - authz_token
         - app_deployment_id
        """
        try:
            return self.api_server_client_pool.getApplicationDeployment(authz_token, app_deployment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_deployment, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_application_deployment, probably due to  client misconfiguration ",
                             )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_deployment, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_deployment, probably due to invalid authz token ",
                             )
            raise

    def update_application_deployment(self, authz_token, app_deployment_id, application_deployment):
        """

        Update an Application Deployment.

        @param app_deployment_id
          The identifier of the requested application deployment to be updated.

        @param application_deployment
           Application Deployment Object created from the datamodel.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - app_deployment_id
         - application_deployment
        """
        try:
            return self.api_server_client_pool.updateApplicationDeployment(authz_token, app_deployment_id,
                                                                           application_deployment)
        except InvalidRequestException:
            logger.exception("Error occurred in update_application_deployment, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_application_deployment, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_application_deployment, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_application_deployment, probably due to invalid authz token ",
                             )
            raise

    def delete_application_deployment(self, authz_token, app_deployment_id):
        """

        Delete an Application Deployment.

        @param app_deployment_id
          The unique identifier of application deployment to be deleted.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - app_deployment_id
        """
        try:
            return self.api_server_client_pool.deleteApplicationDeployment(authz_token, app_deployment_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_application_deployment, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_application_deployment, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_application_deployment, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_application_deployment, probably due to invalid authz token ",
                             )
            raise

    def get_all_application_deployments(self, authz_token, gateway_id):
        """

        Fetch all Application Deployment Descriptions.

        @param gatewayId
           ID of the gateway which need to list all available application deployment documentation.

        @return list<applicationDeployment.
           Returns the list of all application Deployment Objects.


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getAllApplicationDeployments(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_application_deployments, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_application_deployments, probably due to  client misconfiguration ",
                )
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_application_deployments, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_all_application_deployments, probably due to invalid authz token ",
                             )
            raise

    def get_accessible_application_deployments(self, authz_token, gateway_id, permission_type):
        """

        Fetch all accessible Application Deployment Descriptions.

        @param gateway_id
           ID of the gateway which need to list all accessible application deployment documentation.
        @param permission_type
           ResourcePermissionType to check for this user

        @return list<applicationDeployment.
           Returns the list of all application Deployment Objects that are accessible to the user.


        Parameters:
         - authz_token
         - gateway_id
         - permission_type
        """
        try:
            return self.api_server_client_pool.get_accessible_application_deployments(authz_token, gateway_id,
                                                                                      permission_type)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_accessible_application_deployments, probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_accessible_application_deployments,"
                             "                                                  probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_accessible_application_deployments, probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_accessible_application_deployments, probably due to invalid authz token ",
                )
            raise

    def get_app_module_deployed_resources(self, authz_token, app_module_id):
        """
        Fetch a list of Deployed Compute Hosts.

        @param app_module_id
          The identifier for the requested application module

        @return list<string>
          Returns a list of Deployed Resources.


        Parameters:
         - authz_token
         - app_module_id
        """
        try:
            return self.api_server_client_pool.getAppModuleDeployedResources(authz_token, app_module_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_app_module_deployed_resources, probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_app_module_deployed_resources,"
                             "                                                  probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_app_module_deployed_resources, probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_app_module_deployed_resources, probably due to invalid authz token ",
                )
            raise

    def get_application_deployments_for_app_module_and_group_resource_profile(self, authz_token, app_module_id,
                                                                              group_resource_profileId):
        """
        Fetch a list of Application Deployments that this user can use for executing the given Application Module using the given Group Resource Profile.
        The user must have at least READ access to the Group Resource Profile.

        @param app_module_id
           The identifier for the Application Module

        @param group_resource_profileId
           The identifier for the Group Resource Profile

        @return list<ApplicationDeploymentDescription>
           Returns a list of Application Deployments

        Parameters:
         - authz_token
         - app_module_id
         - group_resource_profileId
        """
        try:
            return self.api_server_client_pool.getApplicationDeploymentsForAppModuleAndGroupResourceProfile(authz_token,
                                                                                                            app_module_id,
                                                                                                            group_resource_profileId)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile,"
                             " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile,"
                             "                                                  probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile, "
                             "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile, "
                             "probably due to invalid authz token ",
                             )
            raise

    def register_application_interface(self, authz_token, gateway_id, application_interface):
        """

        Register a Application Interface.

        @param applicationInterface
           Application Module Object created from the datamodel.

        @return appInterfaceId
          Returns a server-side generated airavata application interface globally unique identifier.


        Parameters:
         - authz_token
         - gateway_id
         - application_interface
        """
        try:
            return self.api_server_client_pool.registerApplicationInterface(authz_token, gateway_id,
                                                                            application_interface)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile,"
                             " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile,"
                             "                                                  probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile, "
                             "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile, "
                             "probably due to invalid authz token ",
                             )
            raise

    def clone_application_interface(self, authz_token, existing_app_interface_id, new_application_name, gateway_id):
        """

        Clone an Application Interface.

        @gateway_id
           The identifier for the gateway profile to be requested

        @param existing_app_interface_id
           Identifier of the existing Application interface you wich to clone.

        @param new_application_name
           Name for the new application interface.

        @return appInterfaceId
           Returns a server-side generated globally unique identifier for the newly cloned application interface.


        Parameters:
         - authz_token
         - existing_app_interface_id
         - new_application_name
         - gateway_id
        """
        try:
            return self.api_server_client_pool.cloneApplicationInterface(authz_token, existing_app_interface_id,
                                                                         new_application_name, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile,"
                             " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile,"
                             "                                                  probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile, "
                             "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_deployments_for_app_module_and_group_resource_profile, "
                             "probably due to invalid authz token ",
                             )
            raise

    def get_application_interface(self, authz_token, app_interface_id):
        """

        Fetch an Application Interface.

        @param app_interface_id
          The identifier for the requested application interface.

        @return applicationInterface
          Returns an application Interface Object.


        Parameters:
         - authz_token
         - app_interface_id
        """
        try:
            return self.api_server_client_pool.getApplicationInterface(authz_token, app_interface_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_interface," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception("Error occurred in get_application_interface," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_interface, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_interface, " "probably due to invalid authz token ",
                             )
            raise

    def update_application_interface(self, authz_token, app_interface_id, application_interface):
        """

        Update a Application Interface.

        @param app_interface_id
          The identifier of the requested application deployment to be updated.

        @param application_interface
           Application Interface Object created from the datamodel.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - appInterfaceId
         - applicationInterface
        """
        try:
            return self.api_server_client_pool.updateApplicationInterface(authz_token, app_interface_id,
                                                                          application_interface)
        except InvalidRequestException:
            logger.exception("Error occurred in updateApplicationInterface," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in updateApplicationInterface," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in updateApplicationInterface, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in updateApplicationInterface, " "probably due to invalid authz token ",
                             )
            raise

    def delete_application_interface(self, authz_token, app_interface_id):
        """

        Delete an Application Interface.

        @param app_interface_id
          The identifier for the requested application interface to be deleted.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - app_interface_id
        """
        try:
            return self.api_server_client_pool.deleteApplicationInterface(authz_token, app_interface_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_application_interface," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_application_interface," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_application_interface, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_application_interface, " "probably due to invalid authz token ",
                             )
            raise

    def get_all_application_interface_names(self, authz_token, gateway_id):
        """

        Fetch name and ID of  Application Interface documents.


        @return map<applicationId, applicationInterfaceNames>
          Returns a list of application interfaces with corresponsing ID's


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getAllApplicationInterfaceNames(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_application_interface_names," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_application_interface_names," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_application_interface_names, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_application_interface_names, " "probably due to invalid authz token ",
                )
            raise

    def get_all_application_interfaces(self, authz_token, gateway_id):
        """

        Fetch all Application Interface documents.


        @return map<applicationId, applicationInterfaceNames>
          Returns a list of application interfaces documents (Application Interface ID, name, description, Inputs and Outputs objects).


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getAllApplicationInterfaces(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_application_interface_names," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_application_interface_names," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_application_interface_names, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_application_interface_names, " "probably due to invalid authz token ",
                )
            raise

    def get_application_inputs(self, authz_token, app_interface_id):
        """

        Fetch the list of Application Inputs.

        @param app_interface_id
          The identifier of the application interface which need inputs to be fetched.

        @return list<application_interface_model.InputDataObjectType>
          Returns a list of application inputs.


        Parameters:
         - authz_token
         - app_interface_id
        """
        try:
            return self.api_server_client_pool.getApplicationInputs(authz_token, app_interface_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_inputs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_application_inputs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to invalid authz token ",
                             )
            raise

    def get_application_outputs(self, authz_token, app_interface_id):
        """

        Fetch list of Application Outputs.

        @param appInterfaceId
          The identifier of the application interface which need outputs to be fetched.

        @return list<application_interface_model.OutputDataObjectType>
          Returns a list of application outputs.


        Parameters:
         - authz_token
         - app_interface_id
        """
        try:
            return self.api_server_client_pool.getApplicationOutputs(authz_token, app_interface_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_outputs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_application_outputs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_outputs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_outputs, " "probably due to invalid authz token ",
                             )
            raise

    def get_available_app_interface_compute_resources(self, authz_token, app_interface_id):
        """

        Fetch a list of all deployed Compute Hosts for a given application interfaces.

        @param app_interface_id
          The identifier for the requested application interface.

        @return map<computeResourceId, computeResourceName>
          A map of registered compute resource id's and their corresponding hostnames.
          Deployments of each modules listed within the interfaces will be listed.


        Parameters:
         - authz_token
         - app_interface_id
        """
        try:
            return self.api_server_client_pool.getAvailableAppInterfaceComputeResources(authz_token, app_interface_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_inputs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_application_inputs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to invalid authz token ",
                             )
            raise

    def register_compute_resource(self, authz_token, compute_resource_description):
        """
        Register a Compute Resource.

        @param compute_resource_description
           Compute Resource Object created from the datamodel.

        @return computeResourceId
          Returns a server-side generated airavata compute resource globally unique identifier.


        Parameters:
         - authz_token
         - compute_resource_description
        """
        try:
            return self.api_server_client_pool.registerComputeResource(authz_token, compute_resource_description)
        except InvalidRequestException:
            logger.exception("Error occurred in register_compute_resource," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in register_compute_resource," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in register_compute_resource, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in register_compute_resource, " "probably due to invalid authz token ",
                             )
            raise

    def get_compute_resource(self, authz_token, compute_resource_id):
        """
        Fetch the given Compute Resource.

        @param compute_resource_id
          The identifier for the requested compute resource

        @return computeResourceDescription
           Compute Resource Object created from the datamodel..


        Parameters:
         - authz_token
         - compute_resource_id
        """
        try:
            return self.api_server_client_pool.getComputeResource(authz_token, compute_resource_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_compute_resource," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_compute_resource," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_compute_resource, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_compute_resource, " "probably due to invalid authz token ",
                             )
            raise

    def get_all_compute_resource_names(self, authz_token):
        """

        Fetch all registered Compute Resources.

        @return A map of registered compute resource id's and thier corresponding hostnames.
           Compute Resource Object created from the datamodel..


        Parameters:
         - authz_token
        """
        try:
            return self.api_server_client_pool.getAllComputeResourceNames(authz_token)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_compute_resource_names," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_compute_resource_names," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_compute_resource_names, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_compute_resource_names, " "probably due to invalid authz token ",
                )
            raise

    def update_compute_resource(self, authz_token, compute_resource_id, compute_resource_description):
        """
        Update a Compute Resource.

        @param compute_resource_id
          The identifier for the requested compute resource to be updated.

        @param compute_resource_description
           Compute Resource Object created from the datamodel.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - compute_resource_id
         - compute_resource_description
        """
        try:
            return self.api_server_client_pool.updateComputeResource(authz_token, compute_resource_id,
                                                                     compute_resource_description)
        except InvalidRequestException:
            logger.exception("Error occurred in update_compute_resource," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_compute_resource," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_compute_resource, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_compute_resource, " "probably due to invalid authz token ",
                             )
            raise

    def delete_compute_resource(self, authz_token, compute_resource_id):
        """
        Delete a Compute Resource.

        @param compute_resource_id
          The identifier for the requested compute resource to be deleted.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - compute_resource_id
        """
        try:
            return self.api_server_client_pool.deleteComputeResource(authz_token, compute_resource_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_compute_resource," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_compute_resource," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_compute_resource, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_compute_resource, " "probably due to invalid authz token ",
                             )
            raise

    def register_storage_resource(self, authz_token, storage_resource_description):
        """
        Register a Storage Resource.

        @param storage_resource_description
           Storge Resource Object created from the datamodel.

        @return storageResourceId
          Returns a server-side generated airavata storage resource globally unique identifier.


        Parameters:
         - authz_token
         - storage_resource_description
        """
        try:
            return self.api_server_client_pool.registerStorageResource(authz_token, storage_resource_description)
        except InvalidRequestException:
            logger.exception("Error occurred in register_storage_resource," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in register_storage_resource," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in register_storage_resource, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in register_storage_resource, " "probably due to invalid authz token ",
                             )
            raise

    def get_storage_resource(self, authz_token, storage_resource_id):
        """
        Fetch the given Storage Resource.

        @param storageResourceId
          The identifier for the requested storage resource

        @return storageResourceDescription
           Storage Resource Object created from the datamodel..


        Parameters:
         - authz_token
         - storage_resource_id
        """
        try:
            return self.api_server_client_pool.getStorageResource(authz_token, storage_resource_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_storage_resource," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_storage_resource," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_storage_resource, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_storage_resource, " "probably due to invalid authz token ",
                             )
            raise

    def get_all_storage_resource_names(self, authz_token):
        """
        Fetch all registered Storage Resources.

        @return A map of registered compute resource id's and thier corresponding hostnames.
           Compute Resource Object created from the datamodel..


        Parameters:
         - authz_token
        """
        try:
            return self.api_server_client_pool.getAllStorageResourceNames(authz_token)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_storage_resource_names," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_storage_resource_names," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_storage_resource_names, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_storage_resource_names, " "probably due to invalid authz token ",
                )
            raise

    def update_storage_resource(self, authz_token, storage_resource_id, storage_resource_description):
        """
        Update a Storage Resource.

        @param storage_resource_id
          The identifier for the requested compute resource to be updated.

        @param storage_resource_description
           Storage Resource Object created from the datamodel.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - storage_resource_id
         - storage_resource_description
        """
        try:
            return self.api_server_client_pool.updateStorageResource(authz_token, storage_resource_id,
                                                                     storage_resource_description)
            logger.exception("Error occurred in update_storage_resource," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_storage_resource," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_storage_resource, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_storage_resource, " "probably due to invalid authz token ",
                             )
            raise

    def delete_storage_resource(self, authz_token, storage_resource_id):
        """
        Delete a Storage Resource.

        @param storage_resource_id
          The identifier of the requested compute resource to be deleted.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - storageResourceId
        """
        try:
            return self.api_server_client_pool.getApplicationInputs(authz_token, storage_resource_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_storage_resource," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_storage_resource," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_storage_resource, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_storage_resource, " "probably due to invalid authz token ",
                             )
            raise

    def add_local_submission_details(self, authz_token, compute_resource_id, priority_order, local_submission):
        """
        Add a Local Job Submission details to a compute resource
         App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.

        @param compute_resource_id
          The identifier of the compute resource to which JobSubmission protocol to be added

        @param priority_order
          Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.

        @param local_submission
          The LOCALSubmission object to be added to the resource.

        @return status
          Returns the unique job submission id.


        Parameters:
         - authz_token
         - compute_resource_id
         - priority_order
         - local_submission
        """
        try:
            return self.api_server_client_pool.addLocalSubmissionDetails(authz_token, compute_resource_id,
                                                                         priority_order, local_submission)
        except InvalidRequestException:
            logger.exception("Error occurred in add_local_submission_details," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_local_submission_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in add_local_submission_details, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in add_local_submission_details, " "probably due to invalid authz token ",
                             )
            raise

    def update_local_submission_details(self, authz_token, job_submission_interface_id, local_submission):
        """
        Update the given Local Job Submission details

        @param job_submission_interface_id
          The identifier of the JobSubmission Interface to be updated.

        @param local_submission
          The LOCALSubmission object to be updated.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - job_submission_interface_id
         - local_submission
        """
        try:
            return self.api_server_client_pool.updateLocalSubmissionDetails(authz_token, authz_token,
                                                                            job_submission_interface_id,
                                                                            local_submission)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_local_submission_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_local_submission_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_local_submission_details, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_local_submission_details, " "probably due to invalid authz token ",
                )
            raise

    def get_local_job_submission(self, authz_token, job_submission_id):
        """
        This method returns localJobSubmission object
        @param job_submission_id
          The identifier of the JobSubmission Interface to be retrieved.
         @return LOCALSubmission instance


        Parameters:
         - authz_token
         - job_submission_id
        """
        try:
            return self.api_server_client_pool.getLocalJobSubmission(authz_token, job_submission_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_local_job_submission," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_local_job_submission," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_local_job_submission, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_local_job_submission, " "probably due to invalid authz token ",
                             )
            raise

    def add_ssh_job_submission_details(self, authz_token, compute_resource_id, priority_order, ssh_job_submission):
        """
        Add a SSH Job Submission details to a compute resource
         App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.

        @param compute_resource_id
          The identifier of the compute resource to which JobSubmission protocol to be added

        @param priority_order
          Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.

        @param ssh_job_submission
          The SSHJobSubmission object to be added to the resource.

        @return status
          Returns the unique job submission id.


        Parameters:
         - authz_token
         - compute_resource_id
         - priority_order
         - ssh_job_submission
        """
        try:
            return self.api_server_client_pool.addSSHJobSubmissionDetails(authz_token, compute_resource_id,
                                                                          priority_order, ssh_job_submission)
        except InvalidRequestException:
            logger.exception("Error occurred in add_ssh_job_submission_details," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_ssh_job_submission_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in add_ssh_job_submission_details, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_ssh_job_submission_details, " "probably due to invalid authz token ",
                )
            raise

    def add_ssh_fork_job_submission_details(self, authz_token, compute_resource_id, priority_order, ssh_job_submission):
        """
        Add a SSH_FORK Job Submission details to a compute resource
         App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.

        @param compute_resource_id
          The identifier of the compute resource to which JobSubmission protocol to be added

        @param priority_order
          Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.

        @param ssh_job_submission
          The SSHJobSubmission object to be added to the resource.

        @return status
          Returns the unique job submission id.


        Parameters:
         - authz_token
         - compute_resource_id
         - priority_order
         - ssh_job_submission
        """
        try:
            return self.api_server_client_pool.addSSHForkJobSubmissionDetails(authz_token, compute_resource_id,
                                                                              priority_order, ssh_job_submission)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in add_ssh_fork_job_submission_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_ssh_fork_job_submission_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in add_ssh_fork_job_submission_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_ssh_fork_job_submission_details, " "probably due to invalid authz token ",
                )
            raise

    def get_ssh_job_submission(self, authz_token, job_submission_id):
        """
        This method returns SSHJobSubmission object
        @param job_submission_id
          The identifier of the JobSubmission Interface to be retrieved.
         @return SSHJobSubmission instance


        Parameters:
         - authz_token
         - job_submission_id
        """
        try:
            return self.api_server_client_pool.getSSHJobSubmission(authz_token, job_submission_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_ssh_job_submission," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_ssh_job_submission," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_ssh_job_submission, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_ssh_job_submission, " "probably due to invalid authz token ",
                             )
            raise

    def add_unicore_job_submission_details(self, authz_token, compute_resource_id, priority_order,
                                           unicore_job_submission):
        """

        Add a UNICORE Job Submission details to a compute resource
         App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.

        @param compute_resource_id
          The identifier of the compute resource to which JobSubmission protocol to be added

        @param priority_order
          Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.

        @param unicore_job_submission
          The UnicoreJobSubmission object to be added to the resource.

        @return status
         Returns the unique job submission id.


        Parameters:
         - authz_token
         - compute_resource_id
         - priority_order
         - unicore_job_submission
        """
        try:
            return self.api_server_client_pool.addUNICOREJobSubmissionDetails(authz_token, compute_resource_id,
                                                                              priority_order, unicore_job_submission)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in add_unicore_job_submission_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_unicore_job_submission_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in add_unicore_job_submission_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_unicore_job_submission_details, " "probably due to invalid authz token ",
                )
            raise

    def get_unicore_job_submission(self, authz_token, job_submission_id):
        """
          *
          * This method returns UnicoreJobSubmission object
          *
          * @param job_submission_id
          *   The identifier of the JobSubmission Interface to be retrieved.
          *  @return UnicoreJobSubmission instance
          *
        *

        Parameters:
         - authz_token
         - job_submission_id
        """
        try:
            return self.api_server_client_pool.getUnicoreJobSubmission(authz_token, job_submission_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_unicore_job_submission," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_unicore_job_submission," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_unicore_job_submission, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_unicore_job_submission, " "probably due to invalid authz token ",
                             )
            raise

    def add_cloud_job_submission_details(self, authz_token, compute_resource_id, priority_order, cloud_submission):
        """
           *
           * Add a Cloud Job Submission details to a compute resource
           *  App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
           *
           * @param compute_resource_id
           *   The identifier of the compute resource to which JobSubmission protocol to be added
           *
           * @param priority_order
           *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
           *
           * @param cloud_submission
           *   The SSHJobSubmission object to be added to the resource.
           *
           * @return status
           *   Returns the unique job submission id.
           *
        *

        Parameters:
         - authz_token
         - compute_resource_id
         - priority_order
         - cloud_submission
        """
        try:
            return self.api_server_client_pool.addCloudJobSubmissionDetails(authz_token, compute_resource_id,
                                                                            priority_order, cloud_submission)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_inputs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_application_inputs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to invalid authz token ",
                             )
            raise

    def get_cloud_job_submission(self, authz_token, job_submission_id):
        """
           *
           * This method returns cloudJobSubmission object
           * @param jobSubmissionInterfaceI
               *   The identifier of the JobSubmission Interface to be retrieved.
           *  @return CloudJobSubmission instance
        *

        Parameters:
         - authz_token
         - job_submission_id
        """
        try:
            return self.api_server_client_pool.getCloudJobSubmission(authz_token, job_submission_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_cloud_job_submission," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_cloud_job_submission," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_cloud_job_submission, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_cloud_job_submission, " "probably due to invalid authz token ",
                             )
            raise

    def update_ssh_job_submission_details(self, authz_token, job_submission_interface_id, ssh_job_submission):
        """

        Update the given SSH Job Submission details

        @param job_submission_interface_id
          The identifier of the JobSubmission Interface to be updated.

        @param ssh_job_submission
          The SSHJobSubmission object to be updated.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - job_submission_interface_id
         - ssh_job_submission
        """
        try:
            return self.api_server_client_pool.updateSSHJobSubmissionDetails(authz_token, job_submission_interface_id,
                                                                             ssh_job_submission)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_ssh_job_submission_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_ssh_job_submission_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_ssh_job_submission_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_ssh_job_submission_details, " "probably due to invalid authz token ",
                )
            raise

    def update_cloud_job_submission_details(self, authz_token, job_submission_interface_id, ssh_job_submission):
        """

        Update the cloud Job Submission details

        @param job_submission_interface_id
          The identifier of the JobSubmission Interface to be updated.

        @param ssh_job_submission
          The CloudJobSubmission object to be updated.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - job_submission_interface_id
         - ssh_job_submission
        """
        try:
            return self.api_server_client_pool.updateCloudJobSubmissionDetails(authz_token, job_submission_interface_id,
                                                                               ssh_job_submission)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_cloud_job_submission_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_cloud_job_submission_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_cloud_job_submission_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_cloud_job_submission_details, " "probably due to invalid authz token ",
                )
            raise

    def update_unicore_job_submission_details(self, authz_token, job_submission_interface_id, unicore_job_submission):
        """

        Update the UNIOCRE Job Submission details

        @param job_submission_interface_id
          The identifier of the JobSubmission Interface to be updated.

        @param unicore_job_submission
          The UnicoreJobSubmission object to be updated.

        @return status
          Returns a success/failure of the update.



        Parameters:
         - authz_token
         - job_submission_interface_id
         - unicore_job_submission
        """
        try:
            return self.api_server_client_pool.updateUnicoreJobSubmissionDetails(authz_token,
                                                                                 job_submission_interface_id,
                                                                                 unicore_job_submission)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_unicore_job_submission_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_unicore_job_submission_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_unicore_job_submission_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_unicore_job_submission_details, " "probably due to invalid authz token ",
                )
            raise

    def add_local_data_movement_details(self, authz_token, product_uri, data_move_type, priority_order,
                                        local_data_movement):
        """

        Add a Local data movement details to a compute resource
         App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.

        @param product_uri
          The identifier of the compute resource to which JobSubmission protocol to be added

        @param data_move_type
          DMType object to be added to the resource.

        @param priority_order
          Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.

        @param local_data_movement
          The LOCALDataMovement object to be added to the resource.

        @return status
          Returns the unique job submission id.



        Parameters:
         - authz_token
         - product_uri
         - data_move_type
         - priority_order
         - local_data_movement
        """
        try:
            return self.api_server_client_pool.addLocalDataMovementDetails(authz_token, product_uri, data_move_type,
                                                                           priority_order, local_data_movement)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in add_local_data_movement_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_local_data_movement_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in add_local_data_movement_details, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_local_data_movement_details, " "probably due to invalid authz token ",
                )
            raise

    def update_local_data_movement_details(self, authz_token, data_movementInterface_id, local_data_movement):
        """

        Update the given Local data movement details

        @param data_movementInterface_id
          The identifier of the data movement Interface to be updated.

        @param local_data_movement
          The LOCALDataMovement object to be updated.

        @return status
          Returns a success/failure of the update.



        Parameters:
         - authz_token
         - data_movementInterface_id
         - local_data_movement
        """
        try:
            return self.api_server_client_pool.updateLocalDataMovementDetails(authz_token, data_movementInterface_id,
                                                                              local_data_movement)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_local_data_movement_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_local_data_movement_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_local_data_movement_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_local_data_movement_details, " "probably due to invalid authz token ",
                )
            raise

    def get_local_data_movement(self, authz_token, data_movement_id):
        """

        This method returns local datamovement object.

        @param data_movement_id
          The identifier of the datamovement Interface to be retrieved.

         @return LOCALDataMovement instance



        Parameters:
         - authz_token
         - data_movement_id
        """
        try:
            return self.api_server_client_pool.getLocalDataMovement(authz_token, data_movement_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_local_data_movement," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_local_data_movement," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_local_data_movement, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_local_data_movement, " "probably due to invalid authz token ",
                             )
            raise

    def add_scp_data_movement_details(self, authz_token, product_uri, data_move_type, priority_order,
                                      scp_data_movement):
        """

        Add a SCP data movement details to a compute resource
         App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.

        @param productUri
          The identifier of the compute resource to which JobSubmission protocol to be added

        @param priorityOrder
          Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.

        @param scpDataMovement
          The SCPDataMovement object to be added to the resource.

        @return status
          Returns the unique job submission id.


        Parameters:
         - authz_token
         - product_uri
         - data_move_type
         - priority_order
         - scp_data_movement
        """
        try:
            return self.api_server_client_pool.addSCPDataMovementDetails(authz_token, product_uri, data_move_type,
                                                                         priority_order, scp_data_movement)
        except InvalidRequestException:
            logger.exception("Error occurred in add_scp_data_movement_details," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_scp_data_movement_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in add_scp_data_movement_details, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in add_scp_data_movement_details, " "probably due to invalid authz token ",
                             )
            raise

    def update_scp_data_movement_details(self, authz_token, data_movement_interface_id, scp_data_movement):
        """

        Update the given scp data movement details
         App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.

        @param data_movement_interface_id
          The identifier of the data movement Interface to be updated.

        @param scp_data_movement
          The SCPDataMovement object to be updated.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - data_movement_interface_id
         - scp_data_movement
        """
        try:
            return self.api_server_client_pool.updateSCPDataMovementDetails(authz_token, data_movement_interface_id,
                                                                            scp_data_movement)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_scp_data_movement_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_scp_data_movement_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_scp_data_movement_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_scp_data_movement_details, " "probably due to invalid authz token ",
                )
            raise

    def get_scp_data_movement(self, authz_token, data_movement_id):
        """
        This method returns SCP datamovement object

        @param dataMovementId
          The identifier of the datamovement Interface to be retrieved.

        @return SCPDataMovement instance



        Parameters:
         - authz_token
         - data_movement_id
        """
        try:
            return self.api_server_client_pool.getSCPDataMovement(authz_token, data_movement_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_scp_data_movement," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_scp_data_movement," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_scp_data_movement, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_scp_data_movement, " "probably due to invalid authz token ",
                             )
            raise

    def add_unicore_data_movement_details(self, authz_token, product_uri, data_move_type, priority_order,
                                          unicore_data_movement):
        """

        Add a UNICORE data movement details to a compute resource
         App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.

        @param product_uri
          The identifier of the compute resource to which data movement protocol to be added

        @param priority_order
          Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.

        @param unicore_data_movement
          The UnicoreDataMovement object to be added to the resource.

        @return status
          Returns the unique data movement id.


        Parameters:
         - authz_token
         - product_uri
         - data_move_type
         - priority_order
         - unicore_data_movement
        """
        try:
            return self.api_server_client_pool.addUnicoreDataMovementDetails(authz_token, product_uri, data_move_type,
                                                                             priority_order, unicore_data_movement)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in add_unicore_data_movement_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_unicore_data_movement_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in add_unicore_data_movement_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_unicore_data_movement_details, " "probably due to invalid authz token ",
                )
            raise

    def update_unicore_data_movement_details(self, authz_token, data_movement_interface_id, unicore_data_movement):
        """

        Update a selected UNICORE data movement details
         App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.

        @param data_movement_interface_id
          The identifier of the data movement Interface to be updated.

        @param unicore_data_movement
          The UnicoreDataMovement object to be updated.

        @return status
          Returns a success/failure of the update.



        Parameters:
         - authz_token
         - data_movement_interface_id
         - unicore_data_movement
        """
        try:
            return self.api_server_client_pool.updateUnicoreDataMovementDetails(authz_token, data_movement_interface_id,
                                                                                unicore_data_movement)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_inputs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_application_inputs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to invalid authz token ",
                             )
            raise

    def get_unicore_data_movement(self, authz_token, data_movement_id):
        """

        This method returns UNICORE datamovement object

        @param data_movement_id
          The identifier of the datamovement Interface to be retrieved.

        @return UnicoreDataMovement instance



        Parameters:
         - authz_token
         - data_movement_id
        """
        try:
            return self.api_server_client_pool.getUnicoreDataMovement(authz_token, data_movement_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_unicore_data_movement," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_unicore_data_movement," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_unicore_data_movement, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_unicore_data_movement, " "probably due to invalid authz token ",
                             )
            raise

    def add_grid_ftp_data_movement_details(self, authz_token, product_uri, data_move_type, priority_order,
                                           grid_ftp_data_movement):
        """

        Add a GridFTP data movement details to a compute resource
         App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.

        @param product_uri
          The identifier of the compute resource to which dataMovement protocol to be added

        @param data_move_type
           The DMType object to be added to the resource.

        @param priority_order
          Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.

        @param grid_ftp_data_movement
          The GridFTPDataMovement object to be added to the resource.

        @return status
          Returns the unique data movement id.



        Parameters:
         - authz_token
         - product_uri
         - data_move_type
         - priority_order
         - grid_ftp_data_movement
        """
        try:
            return self.api_server_client_pool.addGridFTPDataMovementDetails(authz_token, product_uri, data_move_type,
                                                                             priority_order, grid_ftp_data_movement)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in add_grid_ftp_data_movement_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_grid_ftp_data_movement_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in add_grid_ftp_data_movement_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_grid_ftp_data_movement_details, " "probably due to invalid authz token ",
                )
            raise

    def update_grid_ftp_data_movement_details(self, authz_token, data_movement_interface_id, grid_ftp_data_movement):
        """
        Update the given GridFTP data movement details to a compute resource
         App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.

        @param data_movement_interface_id
          The identifier of the data movement Interface to be updated.

        @param grid_ftp_data_movement
          The GridFTPDataMovement object to be updated.

        @return boolean
          Returns a success/failure of the update.



        Parameters:
         - authz_token
         - data_movement_interface_id
         - grid_ftp_data_movement
        """
        try:
            return self.api_server_client_pool.updateGridFTPDataMovementDetails(authz_token, data_movement_interface_id,
                                                                                grid_ftp_data_movement)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_grid_ftp_data_movement_details," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_grid_ftp_data_movement_details," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_grid_ftp_data_movement_details, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_grid_ftp_data_movement_details, " "probably due to invalid authz token ",
                )
            raise

    def get_grid_ftp_data_movement(self, authz_token, data_movement_id):
        """
        This method returns GridFTP datamovement object

        @param data_movement_id
          The identifier of the datamovement Interface to be retrieved.

         @return GridFTPDataMovement instance



        Parameters:
         - authz_token
         - data_movement_id
        """
        try:
            return self.api_server_client_pool.getGridFTPDataMovement(authz_token, data_movement_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_grid_ftp_data_movement," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_grid_ftp_data_movement," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_grid_ftp_data_movement, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_grid_ftp_data_movement, " "probably due to invalid authz token ",
                             )
            raise

    def change_job_submission_priority(self, authz_token, job_submission_interface_id, new_priority_order):
        """
        Change the priority of a given job submisison interface

        @param job_submission_interface_id
          The identifier of the JobSubmission Interface to be changed

        @param new_priority_order
          The new priority of the job manager interface.

        @return status
          Returns a success/failure of the change.



        Parameters:
         - authz_token
         - job_submission_interface_id
         - new_priority_order
        """
        try:
            return self.api_server_client_pool.changeJobSubmissionPriority(authz_token, job_submission_interface_id,
                                                                           new_priority_order)
        except InvalidRequestException:
            logger.exception("Error occurred in change_job_submission_priority," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in change_job_submission_priority," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in change_job_submission_priority, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in change_job_submission_priority, " "probably due to invalid authz token ",
                )
            raise

    def change_data_movement_priority(self, authz_token, data_movement_interface_id, new_priority_order):
        """
        Change the priority of a given data movement interface

        @param data_movement_interface_id
          The identifier of the DataMovement Interface to be changed

        @param new_priority_order
          The new priority of the data movement interface.

        @return status
          Returns a success/failure of the change.



        Parameters:
         - authz_token
         - data_movement_interface_id
         - new_priority_order
        """
        try:
            return self.api_server_client_pool.changeDataMovementPriority(authz_token, data_movement_interface_id,
                                                                          new_priority_order)
        except InvalidRequestException:
            logger.exception("Error occurred in change_data_movement_priority," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in change_data_movement_priority," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in change_data_movement_priority, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in change_data_movement_priority, " "probably due to invalid authz token ",
                             )
            raise

    def change_job_submission_priorities(self, authz_token, job_submission_priority_map):
        """
        Change the priorities of a given set of job submission interfaces

        @param job_submission_priority_map
          A Map of identifiers of the JobSubmission Interfaces and thier associated priorities to be set.

        @return status
          Returns a success/failure of the changes.


        Parameters:
         - authz_token
         - job_submission_priority_map
        """
        try:
            return self.api_server_client_pool.changeJobSubmissionPriorities(authz_token, job_submission_priority_map)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in change_job_submission_priorities," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in change_job_submission_priorities," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in change_job_submission_priorities, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in change_job_submission_priorities, " "probably due to invalid authz token ",
                )
            raise

    def change_data_movement_priorities(self, authz_token, data_movement_priority_map):
        """
        Change the priorities of a given set of data movement interfaces

        @param data_movement_priority_map
          A Map of identifiers of the DataMovement Interfaces and thier associated priorities to be set.

        @return status
          Returns a success/failure of the changes.



        Parameters:
         - authz_token
         - data_movement_priority_map
        """
        try:
            return self.api_server_client_pool.changeDataMovementPriorities(authz_token, data_movement_priority_map)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in change_data_movement_priorities," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in change_data_movement_priorities," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in change_data_movement_priorities, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in change_data_movement_priorities, " "probably due to invalid authz token ",
                )
            raise

    def delete_job_submission_interface(self, authz_token, compute_resource_id, job_submission_interface_id):
        """
        Delete a given job submisison interface

        @param job_submission_interface_id
          The identifier of the JobSubmission Interface to be changed

        @return status
          Returns a success/failure of the deletion.



        Parameters:
         - authz_token
         - compute_resource_id
         - jobSubmissionInterfaceId
        """
        try:
            return self.api_server_client_pool.deleteJobSubmissionInterface(authz_token, compute_resource_id,
                                                                            job_submission_interface_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in delete_job_submission_interface," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_job_submission_interface," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_job_submission_interface, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in delete_job_submission_interface, " "probably due to invalid authz token ",
                )
            raise

    def delete_data_movement_interface(self, authz_token, product_uri, data_movement_interface_id, data_move_type):
        """
        Delete a given data movement interface

        @param data_movement_interface_id
          The identifier of the DataMovement Interface to be changed

        @return status
          Returns a success/failure of the deletion.



        Parameters:
         - authz_token
         - product_uri
         - data_movement_interface_id
         - data_move_type
        """
        try:
            return self.api_server_client_pool.deleteDataMovementInterface(authz_token, product_uri,
                                                                           data_movement_interface_id, data_move_type)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_data_movement_interface," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_data_movement_interface," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_data_movement_interface, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in delete_data_movement_interface, " "probably due to invalid authz token ",
                )
            raise

    def register_resource_job_manager(self, authz_token, resource_job_manager):
        """
        Parameters:
         - authz_token
         - resource_job_manager
        """
        try:
            return self.api_server_client_pool.registerResourceJobManager(authz_token, resource_job_manager)
        except InvalidRequestException:
            logger.exception("Error occurred in register_resource_job_manager," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in register_resource_job_manager," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in register_resource_job_manager, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in register_resource_job_manager, " "probably due to invalid authz token ",
                             )
            raise

    def update_resource_job_manager(self, authz_token, resource_job_manager_id, updated_resource_job_manager):
        """
        Parameters:
         - authz_token
         - resourceJobManagerId
         - updatedResourceJobManager
        """
        try:
            return self.api_server_client_pool.updateResourceJobManager(authz_token,
                                                                        resource_job_manager_id,
                                                                        updated_resource_job_manager)
        except InvalidRequestException:
            logger.exception("Error occurred in update_resource_job_manager," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_resource_job_manager," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_resource_job_manager, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_resource_job_manager, " "probably due to invalid authz token ",
                             )
            raise

    def get_resource_job_manager(self, authz_token, resource_job_manager_id):
        """
        Parameters:
         - authz_token
         - resourceJobManagerId
        """
        try:
            return self.api_server_client_pool.getResourceJobManager(authz_token, resource_job_manager_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_resource_job_manager," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_resource_job_manager," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_resource_job_manager, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_resource_job_manager, " "probably due to invalid authz token ",
                             )
            raise

    def delete_resource_job_manager(self, authz_token, resource_job_manager_id):
        """
        Parameters:
         - authz_token
         - resource_job_manager_id
        """
        try:
            return self.api_server_client_pool.deleteResourceJobManager(authz_token, resource_job_manager_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_resource_job_manager," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_resource_job_manager," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_resource_job_manager, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_resource_job_manager, " "probably due to invalid authz token ",
                             )
            raise

    def delete_batch_queue(self, authz_token, compute_resource_id, queue_name):
        """
        Delete a Compute Resource Queue

        @param compute_resource_id
          The identifier of the compute resource which has the queue to be deleted

        @param queue_name
          Name of the queue need to be deleted. Name is the uniqueue identifier for the queue within a compute resource

        @return status
          Returns a success/failure of the deletion.



        Parameters:
         - authz_token
         - compute_resource_id
         - queue_name
        """
        try:
            return self.api_server_client_pool.deleteBatchQueue(authz_token, compute_resource_id, queue_name)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_batch_queue," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_batch_queue," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_batch_queue, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_batch_queue, " "probably due to invalid authz token ",
                             )
            raise

    def register_gateway_resource_profile(self, authz_token, gateway_resource_profile):
        """
        Register a Gateway Resource Profile.

        @param gateway_resource_profile
           Gateway Resource Profile Object.
           The GatewayID should be obtained from Airavata gateway registration and passed to register a corresponding
             resource profile.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - gateway_resource_profile
        """
        try:
            return self.api_server_client_pool.registerGatewayResourceProfile(authz_token, gateway_resource_profile)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in register_gateway_resource_profile," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in register_gateway_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in register_gateway_resource_profile, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in register_gateway_resource_profile, " "probably due to invalid authz token ",
                )
            raise

    def get_gateway_resource_profile(self, authz_token, gateway_id):
        """
        Fetch the given Gateway Resource Profile.

        @param gateway_id
          The identifier for the requested gateway resource.

        @return gatewayResourceProfile
           Gateway Resource Profile Object.


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getGatewayResourceProfile(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_gateway_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_gateway_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_gateway_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_gateway_resource_profile, " "probably due to invalid authz token ",
                             )
            raise

    def update_gateway_resource_profile(self, authz_token, gateway_id, gateway_resource_profile):
        """
        Update a Gateway Resource Profile.

        @param gateway_id
          The identifier for the requested gateway resource to be updated.

        @param gateway_resource_profile
           Gateway Resource Profile Object.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - gateway_id
         - gateway_resource_profile
        """
        try:
            return self.api_server_client_pool.updateGatewayResourceProfile(authz_token, gateway_id,
                                                                            gateway_resource_profile)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_gateway_resource_profile," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_gateway_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_gateway_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_gateway_resource_profile, " "probably due to invalid authz token ",
                )
            raise

    def delete_gateway_resource_profile(self, authz_token, gateway_id):
        """
        Delete the given Gateway Resource Profile.

        @param gateway_id
          The identifier for the requested gateway resource to be deleted.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.deleteGatewayResourceProfile(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in delete_gateway_resource_profile," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_gateway_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_gateway_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in delete_gateway_resource_profile, " "probably due to invalid authz token ",
                )
            raise

    def add_gateway_compute_resource_preference(self, authz_token, gateway_id, compute_resource_id,
                                                compute_resource_preferance):
        """
        Add a Compute Resource Preference to a registered gateway profile.

        @param gateway_id
          The identifier for the gateway profile to be added.

        @param compute_resource_id
          Preferences related to a particular compute resource

        @param compute_resource_preferance
          The ComputeResourcePreference object to be added to the resource profile.

        @return status
          Returns a success/failure of the addition. If a profile already exists, this operation will fail.
           Instead an update should be used.


        Parameters:
         - authz_token
         - gateway_id
         - compute_resource_id
         - compute_resource_preferance
        """
        try:
            return self.api_server_client_pool.addGatewayComputeResourcePreference(authz_token, gateway_id,
                                                                                   compute_resource_id,
                                                                                   compute_resource_preferance)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in add_gateway_compute_resource_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_gateway_compute_resource_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in add_gateway_compute_resource_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_gateway_compute_resource_preference, " "probably due to invalid authz token ",
                )
            raise

    def add_gateway_storage_preference(self, authz_token, gateway_id, storage_resource_id, storage_preference):
        """
        Add a Storage Resource Preference to a registered gateway profile.

        @param gateway_id
          The identifier of the gateway profile to be added.

        @param storage_resource_id
          Preferences related to a particular compute resource

        @param storage_preference
          The ComputeResourcePreference object to be added to the resource profile.

        @return status
          Returns a success/failure of the addition. If a profile already exists, this operation will fail.
           Instead an update should be used.


        Parameters:
         - authz_token
         - gatewayID
         - storageResourceId
         - storagePreference
        """
        try:
            return self.api_server_client_pool.addGatewayStoragePreference(authz_token, authz_token, gateway_id,
                                                                           storage_resource_id, storage_preference)
        except InvalidRequestException:
            logger.exception("Error occurred in add_gateway_storage_preference," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_gateway_storage_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in add_gateway_storage_preference, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_gateway_storage_preference, " "probably due to invalid authz token ",
                )
            raise

    def get_gateway_compute_resource_preference(self, authz_token, gateway_id, compute_resource_id):
        """

        Fetch a Compute Resource Preference of a registered gateway profile.

        @param gateway_id
          The identifier for the gateway profile to be requested

        @param compute_resource_id
          Preferences related to a particular compute resource

        @return computeResourcePreference
          Returns the ComputeResourcePreference object.


        Parameters:
         - authz_token
         - gateway_id
         - compute_resource_id
        """
        try:
            return self.api_server_client_pool.getGatewayComputeResourcePreference(authz_token, gateway_id,
                                                                                   compute_resource_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_inputs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_application_inputs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to invalid authz token ",
                             )
            raise

    def get_gateway_storage_preference(self, authz_token, gateway_id, storage_resourceId):
        """

        Fetch a Storage Resource Preference of a registered gateway profile.

        @param gatewayID
          The identifier of the gateway profile to request to fetch the particular storage resource preference.

        @param storageResourceId
          Identifier of the Stprage Preference required to be fetched.

        @return StoragePreference
          Returns the StoragePreference object.


        Parameters:
         - authz_token
         - gateway_id
         - storage_resourceId
        """
        try:
            return self.api_server_client_pool.getGatewayStoragePreference(authz_token, gateway_id, storage_resourceId)
        except InvalidRequestException:
            logger.exception("Error occurred in get_gateway_storage_preference," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_gateway_storage_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_gateway_storage_preference, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_gateway_storage_preference, " "probably due to invalid authz token ",
                )
            raise

    def get_all_gateway_compute_resource_preferences(self, authz_token, gateway_id):
        """

        Fetch all Compute Resource Preferences of a registered gateway profile.

        @param gateway_id
          The identifier for the gateway profile to be requested

        @return computeResourcePreference
          Returns the ComputeResourcePreference object.


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getAllGatewayComputeResourcePreferences(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_gateway_compute_resource_preferences," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_gateway_compute_resource_preferences," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_gateway_compute_resource_preferences, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_gateway_compute_resource_preferences, " "probably due to invalid authz token ",
                )
            raise

    def get_all_gateway_storage_preferences(self, authz_token, gateway_id):
        """
        Fetch all Storage Resource Preferences of a registered gateway profile.

        @param gateway_id
          The identifier for the gateway profile to be requested

        @return StoragePreference
          Returns the StoragePreference object.


        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getAllGatewayStoragePreferences(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_gateway_storage_preferences," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_gateway_storage_preferences," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_gateway_storage_preferences, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_gateway_storage_preferences, " "probably due to invalid authz token ",
                )
            raise

    def get_all_gateway_resource_profiles(self, authz_token):
        """

        Fetch all Gateway Profiles registered

        @return GatewayResourceProfile
          Returns all the GatewayResourcePrifle list object.



        Parameters:
         - authz_token
        """
        try:
            return self.api_server_client_pool.getAllGatewayResourceProfiles(authz_token)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_gateway_resource_profiles," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_gateway_resource_profiles," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_gateway_resource_profiles, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_gateway_resource_profiles, " "probably due to invalid authz token ",
                )
            raise

    def update_gateway_compute_resource_preference(self, authz_token, gateway_id, compute_resource_id,
                                                   compute_resource_preference):
        """
        Update a Compute Resource Preference to a registered gateway profile.

        @param gateway_id
          The identifier for the gateway profile to be updated.

        @param compute_resource_id
          Preferences related to a particular compute resource

        @param compute_resource_preference
          The ComputeResourcePreference object to be updated to the resource profile.

        @return status
          Returns a success/failure of the updation.


        Parameters:
         - authz_token
         - gateway_id
         - compute_resource_id
         - compute_resource_preference
        """
        try:
            return self.api_server_client_pool.getApplicationInputs(authz_token, gateway_id, compute_resource_id,
                                                                    compute_resource_preference)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_gateway_compute_resource_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_gateway_compute_resource_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_gateway_compute_resource_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_gateway_compute_resource_preference, " "probably due to invalid authz token ",
                )
            raise

    def update_gateway_storage_preference(self, authz_token, gateway_id, storage_id, storage_preference):
        """
        Update a Storage Resource Preference of a registered gateway profile.

        @param gateway_id
          The identifier of the gateway profile to be updated.

        @param storage_id
          The Storage resource identifier of the one that you want to update

        @param storage_preference
          The storagePreference object to be updated to the resource profile.

        @return status
          Returns a success/failure of the updation.


        Parameters:
         - authz_token
         - gateway_id
         - storage_id
         - storage_preference
        """
        try:
            return self.api_server_client_pool.updateGatewayStoragePreference(authz_token, gateway_id, storage_id,
                                                                              storage_preference)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_gateway_storage_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_gateway_storage_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_gateway_storage_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_gateway_storage_preference, " "probably due to invalid authz token ",
                )
            raise

    def delete_gateway_compute_resource_preference(self, authz_token, gateway_id, compute_resource_id):
        """
        Delete the Compute Resource Preference of a registered gateway profile.

        @param gateway_id
          The identifier for the gateway profile to be deleted.

        @param compute_resource_id
          Preferences related to a particular compute resource

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - gateway_id
         - compute_resource_id
        """
        try:
            return self.api_server_client_pool.deleteGatewayComputeResourcePreference(authz_token, gateway_id,
                                                                                      compute_resource_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in delete_gateway_compute_resource_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_gateway_compute_resource_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in delete_gateway_compute_resource_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in delete_gateway_compute_resource_preference, " "probably due to invalid authz token ",
                )
            raise

    def delete_gateway_storage_preference(self, authz_token, gateway_id, storage_id):
        """
        Delete the Storage Resource Preference of a registered gateway profile.

        @param gateway_id
          The identifier of the gateway profile to be deleted.

        @param storage_id
          ID of the storage preference you want to delete.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - gateway_id
         - storage_id
        """
        try:
            return self.api_server_client_pool.deleteGatewayStoragePreference(authz_token, gateway_id, storage_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in delete_gateway_storage_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_gateway_storage_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in delete_gateway_storage_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in delete_gateway_storage_preference, " "probably due to invalid authz token ",
                )
            raise

    def get_ssh_account_provisioners(self, authz_token):
        """
        Parameters:
         - authz_token
        """
        try:
            return self.api_server_client_pool.getSSHAccountProvisioners(authz_token)
        except InvalidRequestException:
            logger.exception("Error occurred in get_ssh_account_provisioners," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_ssh_account_provisioners," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_ssh_account_provisioners, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_ssh_account_provisioners, " "probably due to invalid authz token ",
                             )
            raise

    def does_user_have_ssh_account(self, authz_token, compute_resource_id, user_id):
        """
        Check if user has an SSH account on the given compute resource. This
        method will only work if the compute resource has an SSHAccountProvisioner configured for it.

        Parameters:
         - authz_token
         - compute_resource_id
         - user_id
        """
        try:
            return self.api_server_client_pool.doesUserHaveSSHAccount(authz_token, compute_resource_id, user_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_inputs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_application_inputs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to invalid authz token ",
                             )
            raise

    def is_ssh_setup_complete_for_user_compute_resource_preference(self, authz_token, compute_resource_id,
                                                                   airavata_cred_store_token):
        """
        Check if SSH account setup is complete for this user on the given compute resource.

        Parameters:
         - authz_token
         - compute_resource_id
         - airavata_cred_store_token
        """
        try:
            return self.api_server_client_pool.isSSHSetupCompleteForUserComputeResourcePreference(authz_token,
                                                                                                  compute_resource_id,
                                                                                                  airavata_cred_store_token)
        except InvalidRequestException:
            logger.exception("Error occurred in get_application_inputs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_application_inputs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_application_inputs, " "probably due to invalid authz token ",
                             )
            raise

    def setup_user_compute_resource_preferences_for_ssh(self, authz_token, compute_resource_id, user_id,
                                                        airavata_cred_store_token):
        """
        Setup and return a UserComputeResourcePreference object for this user to SSH into the given compute resource with
        the given SSH credential. This method will only work if the compute resource has an SSHAccountProvisioner
        configured for it. The returned UserComputeResourcePreference object is not saved; it is up to the client to
        call addUserComputeResourcePreference to persist it.

        Parameters:
         - authz_token
         - compute_resource_id
         - user_id
         - airavata_cred_store_token
        """
        try:
            return self.api_server_client_pool.setupUserComputeResourcePreferencesForSSH(authz_token,
                                                                                         compute_resource_id, user_id,
                                                                                         airavata_cred_store_token)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in setup_user_compute_resource_preferences_for_ssh," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in setup_user_compute_resource_preferences_for_ssh," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in setup_user_compute_resource_preferences_for_ssh, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in setup_user_compute_resource_preferences_for_ssh, " "probably due to invalid authz token ",
                )
            raise

    def register_user_resource_profile(self, authz_token, user_resource_profile):
        """
        Register User Resource Profile.

        @param user_resource_profile
           User Resource Profile Object.
           The userId should be obtained from Airavata user profile data model and passed to register a corresponding
             resource profile.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - user_resource_profile
        """
        try:
            return self.api_server_client_pool.registerUserResourceProfile(authz_token, user_resource_profile)
        except InvalidRequestException:
            logger.exception("Error occurred in register_user_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in register_user_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in register_user_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in register_user_resource_profile, " "probably due to invalid authz token ",
                )
            raise

    def is_user_resource_profile_exists(self, authz_token, user_id, gateway_id):
        """
        Check if User Resource Profile exists.

        @param user_id
          The identifier for the requested user resource profile.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @return bool


        Parameters:
         - authz_token
         - user_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.isUserResourceProfileExists(authz_token, user_id, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in is_user_resource_profile_exists," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in is_user_resource_profile_exists," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in is_user_resource_profile_exists, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in is_user_resource_profile_exists, " "probably due to invalid authz token ",
                )
            raise

    def get_user_resource_profile(self, authz_token, user_id, gateway_id):
        """
        Fetch the given User Resource Profile.

        @param userId
          The identifier for the requested user resource profile.

        @param gatewayID
          The identifier to link a gateway for the requested user resource profile.

        @return UserResourceProfile
           User Resource Profile Object.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getUserResourceProfile(authz_token, user_id, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_user_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_user_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_user_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_user_resource_profile, " "probably due to invalid authz token ",
                             )
            raise

    def update_user_resource_profile(self, authz_token, user_id, gateway_id, user_resource_profile):
        """
        Update a User Resource Profile.

        @param user_id
          The identifier for the requested user resource to be updated.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_resource_profile
           User Resource Profile Object.

        @return status
          Returns a success/failure of the update.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
         - user_resource_profile
        """
        try:
            return self.api_server_client_pool.updateUserResourceProfile(authz_token, user_id, gateway_id,
                                                                         user_resource_profile)
        except InvalidRequestException:
            logger.exception("Error occurred in update_user_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_user_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_user_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_user_resource_profile, " "probably due to invalid authz token ",
                             )
            raise

    def delete_user_resource_profile(self, authz_token, user_id, gateway_id):
        """
        Delete the given User Resource Profile.

        @param user_id
          The identifier for the requested user resource to be deleted.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.deleteUserResourceProfile(authz_token, user_id, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_user_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_user_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_user_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in delete_user_resource_profile, " "probably due to invalid authz token ",
                             )
            raise

    def add_user_compute_resource_preference(self, authz_token, user_id, gateway_id, user_compute_resource_id,
                                             user_compute_resource_preference):
        """
        Add a Compute Resource Preference to a registered User profile.

        @param user_id
          The identifier for the User resource profile to be added.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_compute_resource_id
          Preferences related to a particular compute resource

        @param user_compute_resource_preference
          The ComputeResourcePreference object to be added to the resource profile.

        @return status
          Returns a success/failure of the addition. If a profile already exists, this operation will fail.
           Instead an update should be used.


        Parameters:
         - authz_token
         - userId
         - gatewayID
         - userComputeResourceId
         - userComputeResourcePreference
        """
        try:
            return self.api_server_client_pool.addUserComputeResourcePreference(authz_token, user_id, gateway_id,
                                                                                user_compute_resource_id,
                                                                                user_compute_resource_preference)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in add_user_compute_resource_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_user_compute_resource_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in add_user_compute_resource_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in add_user_compute_resource_preference, " "probably due to invalid authz token ",
                )
            raise

    def add_user_storage_preference(self, authz_token, user_id, gateway_id, user_storage_resource_id,
                                    user_storage_preference):
        """
        Add a Storage Resource Preference to a registered user resource profile.

        @param user_id
          The identifier of the user resource profile to be added.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_storage_resource_id
          Preferences related to a particular compute resource

        @param user_storage_preference
          The ComputeResourcePreference object to be added to the resource profile.

        @return status
          Returns a success/failure of the addition. If a profile already exists, this operation will fail.
           Instead an update should be used.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
         - user_storage_resource_id
         - user_storage_preference
        """
        try:
            return self.api_server_client_pool.addUserStoragePreference(authz_token, user_id, gateway_id,
                                                                        user_storage_resource_id,
                                                                        user_storage_preference)
        except InvalidRequestException:
            logger.exception("Error occurred in add_user_storage_preference," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in add_user_storage_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in add_user_storage_preference, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in add_user_storage_preference, " "probably due to invalid authz token ",
                             )
            raise

    def get_user_compute_resource_preference(self, authz_token, user_id, gateway_id, user_compute_resource_id):
        """

        Fetch a Compute Resource Preference of a registered user resource profile.

        @param user_id
          The identifier for the user profile to be requested

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_compute_resource_id
          Preferences related to a particular compute resource

        @return computeResourcePreference
          Returns the ComputeResourcePreference object.


        Parameters:
         - authz_token
         - userId
         - gatewayID
         - userComputeResourceId
        """
        try:
            return self.api_server_client_pool.getUserComputeResourcePreference(authz_token, user_id, gateway_id,
                                                                                user_compute_resource_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_user_compute_resource_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_user_compute_resource_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_user_compute_resource_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_user_compute_resource_preference, " "probably due to invalid authz token ",
                )
            raise

    def get_user_storage_preference(self, authz_token, user_id, gateway_id, user_storage_resource_id):
        """

        Fetch a Storage Resource Preference of a registered user resource profile.

        @param user_id
          The identifier of the user resource profile to request to fetch the particular storage resource preference.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_storage_resource_id
          Identifier of the Stprage Preference required to be fetched.

        @return UserStoragePreference
          Returns the StoragePreference object.


        Parameters:
         - authz_token
         - userId
         - gatewayID
         - userStorageResourceId
        """
        try:
            return self.api_server_client_pool.getUserStoragePreference(authz_token, user_id, gateway_id,
                                                                        user_storage_resource_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_user_storage_preference," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_user_storage_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_user_storage_preference, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_user_storage_preference, " "probably due to invalid authz token ",
                             )
            raise

    def get_all_user_compute_resource_preferences(self, authz_token, user_id, gateway_id):
        """

        Fetch all Compute Resource Preferences of a registered gateway profile.

        @param user_id
          The identifier of the user resource profile to request to fetch the particular storage resource preference.

        @param gateway_id
          The identifier for the gateway profile to be requested

        @return computeResourcePreference
          Returns the ComputeResourcePreference object.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getAllUserComputeResourcePreferences(authz_token, user_id, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_user_compute_resource_preferences," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_user_compute_resource_preferences," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_user_compute_resource_preferences, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_user_compute_resource_preferences, " "probably due to invalid authz token ",
                )
            raise

    def get_all_user_storage_preferences(self, authz_token, user_id, gateway_id):
        """
        Fetch all User Storage Resource Preferences of a registered user profile.

        @param user_id
          The identifier of the user resource profile to request to fetch the particular storage resource preference.

        @param gateway_id
          The identifier for the gateway profile to be requested

        @return StoragePreference
          Returns the StoragePreference object.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getApplicationInputs(authz_token, user_id, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_user_storage_preferences," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_user_storage_preferences," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_user_storage_preferences, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_user_storage_preferences, " "probably due to invalid authz token ",
                )
            raise

    def get_all_user_resource_profiles(self, authz_token):
        """

        Fetch all user resources Profiles registered

        @return UserResourceProfile
          Returns all the UserResourcePrifle list object.



        Parameters:
         - authz_token
        """
        try:
            return self.api_server_client_pool.getAllUserResourceProfiles(authz_token)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_user_resource_profiles," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_user_resource_profiles," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_user_resource_profiles, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_user_resource_profiles, " "probably due to invalid authz token ",
                )
            raise

    def update_user_compute_resource_preference(self, authz_token, user_id, gateway_id, user_compute_resourceId,
                                                user_compute_resource_preference):
        """
        Update a Compute Resource Preference to a registered user resource profile.

        @param user_id
          The identifier for the user profile to be updated.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_compute_resourceId
          Preferences related to a particular compute resource

        @param user_compute_resource_preference
          The ComputeResourcePreference object to be updated to the resource profile.

        @return status
          Returns a success/failure of the updation.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
         - user_compute_resourceId
         - user_compute_resource_preference
        """
        try:
            return self.api_server_client_pool.updateUserComputeResourcePreference(authz_token, user_id, gateway_id,
                                                                                   user_compute_resourceId,
                                                                                   user_compute_resource_preference)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in update_user_compute_resource_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_user_compute_resource_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in update_user_compute_resource_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_user_compute_resource_preference, " "probably due to invalid authz token ",
                )
            raise

    def update_user_storage_preference(self, authz_token, user_id, gateway_id, user_storage_id,
                                       user_storage_preference):
        """
        Update a Storage Resource Preference of a registered user resource profile.

        @param user_id
          The identifier of the user resource profile to be updated.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_storage_id
          The Storage resource identifier of the one that you want to update

        @param user_storage_preference
          The storagePreference object to be updated to the resource profile.

        @return status
          Returns a success/failure of the updation.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
         - user_storage_id
         - user_storage_preference
        """
        try:
            return self.api_server_client_pool.updateUerStoragePreference(authz_token, user_id, gateway_id,
                                                                          user_storage_id, user_storage_preference)
        except InvalidRequestException:
            logger.exception("Error occurred in update_user_storage_preference," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_user_storage_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_user_storage_preference, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in update_user_storage_preference, " "probably due to invalid authz token ",
                )
            raise

    def delete_user_compute_resource_preference(self, authz_token, user_id, gateway_id, user_compute_resource_id):
        """
        Delete the Compute Resource Preference of a registered user resource profile.

        @param user_id
          The identifier for the user resource profile to be deleted.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_compute_resource_id
          Preferences related to a particular compute resource

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
         - user_compute_resource_id
        """
        try:
            return self.api_server_client_pool.deleteUserComputeResourcePreference(authz_token, user_id, gateway_id,
                                                                                   user_compute_resource_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in delete_user_compute_resource_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_user_compute_resource_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in delete_user_compute_resource_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in delete_user_compute_resource_preference, " "probably due to invalid authz token ",
                )
            raise

    def delete_user_storage_preference(self, authz_token, user_id, gateway_id, user_storage_id):
        """
        Delete the Storage Resource Preference of a registered user resource profile.

        @param user_id
          The identifier of the user profile to be deleted.

        @param gateway_id
          The identifier to link a gateway for the requested user resource profile.

        @param user_storage_id
          ID of the storage preference you want to delete.

        @return status
          Returns a success/failure of the deletion.


        Parameters:
         - authz_token
         - user_id
         - gateway_id
         - user_storage_id
        """
        try:
            return self.api_server_client_pool.deleteUserStoragePreference(authz_token, user_id, gateway_id,
                                                                           user_storage_id)
        except InvalidRequestException:
            logger.exception("Error occurred in delete_user_storage_preference," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in delete_user_storage_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in delete_user_storage_preference, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in delete_user_storage_preference, " "probably due to invalid authz token ",
                )
            raise

    def get_latest_queue_statuses(self, authz_token):
        """
        Parameters:
         - authz_token
        """
        try:
            return self.api_server_client_pool.getLatestQueueStatuses(authz_token)
        except InvalidRequestException:
            logger.exception("Error occurred in get_latest_queue_statuses," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_latest_queue_statuses," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_latest_queue_statuses, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_latest_queue_statuses, " "probably due to invalid authz token ",
                             )
            raise

    def register_data_product(self, authz_token, data_product_model):
        """
        API Methods related to replica catalog


        Parameters:
         - authz_token
         - data_product_model
        """
        try:
            return self.api_server_client_pool.registerDataProduct(authz_token, data_product_model)
        except InvalidRequestException:
            logger.exception("Error occurred in register_data_product," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in register_data_product," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in register_data_product, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in register_data_product, " "probably due to invalid authz token ",
                             )
            raise

    def get_data_product(self, authz_token, data_product_uri):
        """
        Parameters:
         - authz_token
         - data_product_uri
        """
        try:
            return self.api_server_client_pool.getDataProduct(authz_token, data_product_uri)
        except InvalidRequestException:
            logger.exception("Error occurred in get_data_product," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_data_product," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_data_product, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_data_product, " "probably due to invalid authz token ",
                             )
            raise

    def register_replica_location(self, authz_token, replica_location_model):
        """
        Parameters:
         - authz_token
         - replica_location_model
        """
        try:
            return self.api_server_client_pool.registerReplicaLocation(authz_token, replica_location_model)
        except InvalidRequestException:
            logger.exception("Error occurred in register_replica_location," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in register_replica_location," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in register_replica_location, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in register_replica_location, " "probably due to invalid authz token ",
                             )
            raise

    def get_parent_data_product(self, authz_token, product_uri):
        """
        Parameters:
         - authz_token
         - product_uri
        """
        try:
            return self.api_server_client_pool.getParentDataProduct(authz_token, product_uri)
        except InvalidRequestException:
            logger.exception("Error occurred in get_parent_data_product," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_parent_data_product," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_parent_data_product, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_parent_data_product, " "probably due to invalid authz token ",
                             )
            raise

    def get_child_data_products(self, authz_token, product_uri):
        """
        Parameters:
         - authz_token
         - product_uri
        """
        try:
            return self.api_server_client_pool.getChildDataProducts(authz_token, product_uri)
        except InvalidRequestException:
            logger.exception("Error occurred in get_child_data_products," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_child_data_products," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_child_data_products, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_child_data_products, " "probably due to invalid authz token ",
                             )
            raise

    def share_resource_with_users(self, authz_token, resource_id, user_permission_list):
        """
        Group Manager and Data Sharing Related API methods


        Parameters:
         - authz_token
         - resource_id
         - user_permission_list
        """
        try:
            return self.api_server_client_pool.shareResourceWithUsers(authz_token, resource_id, user_permission_list)
        except InvalidRequestException:
            logger.exception("Error occurred in share_resource_with_users," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in share_resource_with_users," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in share_resource_with_users, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in share_resource_with_users, " "probably due to invalid authz token ",
                             )
            raise

    def share_resource_with_groups(self, authz_token, resource_id, group_permission_list):
        """
        Parameters:
         - authz_token
         - resourceId
         - groupPermissionList
        """
        try:
            return self.api_server_client_pool.shareResourceWithGroups(authz_token, resource_id, group_permission_list)
        except InvalidRequestException:
            logger.exception("Error occurred in share_resource_with_groups," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in share_resource_with_groups," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in share_resource_with_groups, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in share_resource_with_groups, " "probably due to invalid authz token ",
                             )
            raise

    def revoke_sharing_of_resource_from_users(self, authz_token, resource_id, user_permission_list):
        """
        Parameters:
         - authz_token
         - resource_id
         - user_permission_list
        """
        try:
            return self.api_server_client_pool.revokeSharingOfResourceFromUsers(authz_token, resource_id,
                                                                                user_permission_list)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in revoke_sharing_of_resource_from_users," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in revoke_sharing_of_resource_from_users," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in revoke_sharing_of_resource_from_users, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in revoke_sharing_of_resource_from_users, " "probably due to invalid authz token ",
                )
            raise

    def revoke_sharing_of_resource_from_groups(self, authz_token, resource_id, group_permission_list):
        """
        Parameters:
         - authz_token
         - resourceId
         - groupPermissionList
        """
        try:
            return self.api_server_client_pool.revokeSharingOfResourceFromGroups(authz_token, resource_id,
                                                                                 group_permission_list)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in revoke_sharing_of_resource_from_groups," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in revoke_sharing_of_resource_from_groups," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in revoke_sharing_of_resource_from_groups, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in revoke_sharing_of_resource_from_groups, " "probably due to invalid authz token ",
                )
            raise

    def get_all_accessible_users(self, authz_token, resource_id, permission_type):
        """
        Parameters:
         - authz_token
         - resource_id
         - permission_type
        """
        try:
            return self.api_server_client_pool.getAllAccessibleUsers(authz_token, resource_id, permission_type)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_accessible_users," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_accessible_users," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_accessible_users, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_all_accessible_users, " "probably due to invalid authz token ",
                             )
            raise

    def get_all_accessible_groups(self, authz_token, resource_id, permission_type):
        """
        Parameters:
         - authz_token
         - resource_id
         - permission_type
        """
        try:
            return self.api_server_client_pool.getAllAccessibleGroups(authz_token, resource_id, permission_type)
        except InvalidRequestException:
            logger.exception("Error occurred in get_all_accessible_groups," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_accessible_groups," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_all_accessible_groups, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_all_accessible_groups, " "probably due to invalid authz token ",
                             )
            raise

    def get_all_directly_accessible_users(self, authz_token, resource_id, permission_type):
        """
        Parameters:
         - authz_token
         - resource_id
         - permission_type
        """
        try:
            return self.api_server_client_pool.getAllDirectlyAccessibleUsers(authz_token, resource_id, permission_type)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_directly_accessible_users," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_directly_accessible_users," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_directly_accessible_users, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_directly_accessible_users, " "probably due to invalid authz token ",
                )
            raise

    def get_all_directly_accessible_groups(self, authz_token, resource_id, permission_type):
        """
        Parameters:
         - authz_token
         - resource_id
         - permission_type
        """
        try:
            return self.api_server_client_pool.getAllDirectlyAccessibleGroups(authz_token, resource_id, permission_type)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_all_directly_accessible_groups," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_all_directly_accessible_groups," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_all_directly_accessible_groups, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_all_directly_accessible_groups, " "probably due to invalid authz token ",
                )
            raise

    def user_has_access(self, authz_token, resource_id, permission_type):
        """
        Parameters:
         - authz_token
         - resourceId
         - permissionType
        """
        try:
            return self.api_server_client_pool.userHasAccess(authz_token, resource_id, permission_type)
        except InvalidRequestException:
            logger.exception("Error occurred in user_has_access," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in user_has_access," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in user_has_access, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in user_has_access, " "probably due to invalid authz token ",
                             )
            raise

    def create_group_resource_profile(self, authz_token, group_resource_profile):
        """
        Parameters:
         - authz_token
         - group_resource_profile
        """
        try:
            return self.api_server_client_pool.createGroupResourceProfile(authz_token, group_resource_profile)
        except InvalidRequestException:
            logger.exception("Error occurred in create_group_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in create_group_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in create_group_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in create_group_resource_profile, " "probably due to invalid authz token ",
                             )
            raise

    def update_group_resource_profile(self, authz_token, group_resource_profile):
        """
        Parameters:
         - authz_token
         - group_resource_profile
        """
        try:
            return self.api_server_client_pool.updateGroupResourceProfile(authz_token, group_resource_profile)
        except InvalidRequestException:
            logger.exception("Error occurred in update_group_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in update_group_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in update_group_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in update_group_resource_profile, " "probably due to invalid authz token ",
                             )
            raise

    def get_group_resource_profile(self, authz_token, group_resource_profile_id):
        """
        Parameters:
         - authz_token
         - group_resource_profile_id
        """
        try:
            return self.api_server_client_pool.getGroupResourceProfile(authz_token, group_resource_profile_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_group_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_group_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_group_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_group_resource_profile, " "probably due to invalid authz token ",
                             )
            raise

    def remove_group_resource_profile(self, authz_token, group_resource_profile_id):
        """
        Parameters:
         - authz_token
         - group_resource_profile_id
        """
        try:
            return self.api_server_client_pool.removeGroupResourceProfile(authz_token, group_resource_profile_id)
        except InvalidRequestException:
            logger.exception("Error occurred in remove_group_resource_profile," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in remove_group_resource_profile," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in remove_group_resource_profile, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in remove_group_resource_profile, " "probably due to invalid authz token ",
                             )
            raise

    def get_group_resource_list(self, authz_token, gateway_id):
        """
        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getGroupResourceList(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_group_resource_list," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_group_resource_list," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_group_resource_list, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_group_resource_list, " "probably due to invalid authz token ",
                             )
            raise

    def remove_group_compute_prefs(self, authz_token, compute_resource_id, group_resource_profile_id):
        """
        Parameters:
         - authz_token
         - compute_resource_id
         - group_resource_profile_id
        """
        try:
            return self.api_server_client_pool.removeGroupComputePrefs(authz_token, compute_resource_id,
                                                                       group_resource_profile_id)
        except InvalidRequestException:
            logger.exception("Error occurred in remove_group_compute_prefs," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in remove_group_compute_prefs," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in remove_group_compute_prefs, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in remove_group_compute_prefs, " "probably due to invalid authz token ",
                             )
            raise

    def remove_group_compute_resource_policy(self, authz_token, resource_policy_id):
        """
        Parameters:
         - authz_token
         - resource_policy_id
        """
        try:
            return self.api_server_client_pool.removeGroupComputeResourcePolicy(authz_token, resource_policy_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in remove_group_compute_resource_policy," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in remove_group_compute_resource_policy," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in remove_group_compute_resource_policy, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in remove_group_compute_resource_policy, " "probably due to invalid authz token ",
                )
            raise

    def remove_group_batch_queue_resource_policy(self, authz_token, resource_policy_id):
        """
        Parameters:
         - authz_token
         - resource_policy_id
        """
        try:
            return self.api_server_client_pool.removeGroupBatchQueueResourcePolicy(authz_token, resource_policy_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in remove_group_batch_queue_resource_policy," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in remove_group_batch_queue_resource_policy," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in remove_group_batch_queue_resource_policy, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in remove_group_batch_queue_resource_policy, " "probably due to invalid authz token ",
                )
            raise

    def get_group_compute_resource_preference(self, authz_token, compute_resource_id, group_resource_profile_id):
        """
        Parameters:
         - authz_token
         - compute_resource_id
         - group_resource_profile_id
        """
        try:
            return self.api_server_client_pool.getGroupComputeResourcePreference(authz_token, compute_resource_id,
                                                                                 group_resource_profile_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_group_compute_resource_preference," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_group_compute_resource_preference," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_group_compute_resource_preference, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_group_compute_resource_preference, " "probably due to invalid authz token ",
                )
            raise

    def get_group_compute_resource_policy(self, authz_token, resource_policy_id):
        """
        Parameters:
         - authz_token
         - resource_policy_id
        """
        try:
            return self.api_server_client_pool.getGroupComputeResourcePolicy(authz_token, resource_policy_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_group_compute_resource_policy," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_group_compute_resource_policy," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_group_compute_resource_policy, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_group_compute_resource_policy, " "probably due to invalid authz token ",
                )
            raise

    def get_batch_queue_resource_policy(self, authz_token, resource_policy_id):
        """
        Parameters:
         - authz_token
         - resource_policy_id
        """
        try:
            return self.api_server_client_pool.getBatchQueueResourcePolicy(authz_token, resource_policy_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_batch_queue_resource_policy," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_batch_queue_resource_policy," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_batch_queue_resource_policy, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_batch_queue_resource_policy, " "probably due to invalid authz token ",
                )
            raise

    def get_group_compute_resource_pref_list(self, authz_token, group_resource_profile_id):
        """
        Parameters:
         - authz_token
         - group_resource_profile_id
        """
        try:
            return self.api_server_client_pool.getGroupComputeResourcePrefList(authz_token, group_resource_profile_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_group_compute_resource_pref_list," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_group_compute_resource_pref_list," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_group_compute_resource_pref_list, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_group_compute_resource_pref_list, " "probably due to invalid authz token ",
                )
            raise

    def get_group_batch_queue_resource_policy_list(self, authz_token, group_resource_profile_id):
        """
        Parameters:
         - authz_token
         - group_resource_profile_id
        """
        try:
            return self.api_server_client_pool.getGroupBatchQueueResourcePolicyList(authz_token,
                                                                                    group_resource_profile_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_group_batch_queue_resource_policy_list," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_group_batch_queue_resource_policy_list," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_group_batch_queue_resource_policy_list, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_group_batch_queue_resource_policy_list, " "probably due to invalid authz token ",
                )
            raise

    def get_group_compute_resource_policy_list(self, authz_token, group_resource_profile_id):
        """
        Parameters:
         - authz_token
         - group_resource_profile_id
        """
        try:
            return self.api_server_client_pool.getGroupComputeResourcePolicyList(authz_token, group_resource_profile_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_group_compute_resource_policy_list," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_group_compute_resource_policy_list," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_group_compute_resource_policy_list, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_group_compute_resource_policy_list, " "probably due to invalid authz token ",
                )
            raise

    def get_gateway_groups(self, authz_token):
        """
        GatewayGroups API methods

        Parameters:
         - authz_token
        """
        try:
            return self.api_server_client_pool.getGatewayGroups(authz_token)
        except InvalidRequestException:
            logger.exception("Error occurred in get_gateway_groups," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_gateway_groups," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_gateway_groups, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_gateway_groups, " "probably due to invalid authz token ",
                             )
            raise

    def get_parser(self, authz_token, parser_id, gateway_id):
        """
        Parameters:
         - authz_token
         - parserId
         - gatewayId
        """
        try:
            return self.api_server_client_pool.getParser(authz_token, parser_id, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_parser," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_parser," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_parser, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_parser, " "probably due to invalid authz token ",
                             )
            raise

    def save_parser(self, authz_token, parser):
        """
        Parameters:
         - authz_token
         - parser
        """
        try:
            return self.api_server_client_pool.saveParser(authz_token, parser)
        except InvalidRequestException:
            logger.exception("Error occurred in save_parser," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in save_parser," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in save_parser, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in save_parser, " "probably due to invalid authz token ",
                             )
            raise

    def list_all_parsers(self, authz_token, gateway_id):
        """
        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.listAllParsers(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in list_all_parsers," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in list_all_parsers," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in list_all_parsers, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in list_all_parsers, " "probably due to invalid authz token ",
                             )
            raise

    def remove_parser(self, authz_token, parser_id, gateway_id):
        """
        Parameters:
         - authz_token
         - parser_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.removeParser(authz_token, parser_id, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in remove_parser," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in remove_parser," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in remove_parser, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in remove_parser, " "probably due to invalid authz token ",
                             )
            raise

    def get_parsing_template(self, authz_token, template_id, gateway_id):
        """
        Parameters:
         - authz_token
         - template_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.getParsingTemplate(authz_token, template_id, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in get_parsing_template," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_parsing_template," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in get_parsing_template, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in get_parsing_template, " "probably due to invalid authz token ",
                             )
            raise

    def get_parsing_templates_for_experiment(self, authz_token, experiment_id, gateway_id):
        """
        Parameters:
         - authz_token
         - experimentId
         - gatewayId
        """
        try:
            return self.api_server_client_pool.getParsingTemplatesForExperiment(authz_token, experiment_id, gateway_id)
        except InvalidRequestException:
            logger.exception(
                "Error occurred in get_parsing_templates_for_experiment," " probably due to invalid parameters ",
                )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in get_parsing_templates_for_experiment," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception(
                "Error occurred in get_parsing_templates_for_experiment, " "probably due to server side error ",
               )
            raise
        except AuthorizationException:
            logger.exception(
                "Error occurred in get_parsing_templates_for_experiment, " "probably due to invalid authz token ",
                )
            raise

    def save_parsing_template(self, authz_token, parsing_template):
        """
        Parameters:
         - authz_token
         - parsing_template
        """
        try:
            return self.api_server_client_pool.saveParsingTemplate(authz_token, parsing_template)
        except InvalidRequestException:
            logger.exception("Error occurred in save_parsing_template," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in save_parsing_template," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in save_parsing_template, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in save_parsing_template, " "probably due to invalid authz token ",
                             )
            raise

    def remove_parsing_template(self, authz_token, template_id, gateway_id):
        """
        Parameters:
         - authz_token
         - template_id
         - gateway_id
        """
        try:
            return self.api_server_client_pool.removeParsingTemplate(authz_token, template_id, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in remove_parsing_template," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in remove_parsing_template," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in remove_parsing_template, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in remove_parsing_template, " "probably due to invalid authz token ",
                             )
            raise

    def list_all_parsing_templates(self, authz_token, gateway_id):
        """
        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.api_server_client_pool.listAllParsingTemplates(authz_token, gateway_id)
        except InvalidRequestException:
            logger.exception("Error occurred in list_all_parsing_templates," " probably due to invalid parameters ",
                             )
            raise
        except AiravataClientException:
            logger.exception(
                "Error occurred in list_all_parsing_templates," "probably due to  client mis configuration")
            raise
        except AiravataSystemException:
            logger.exception("Error occurred in list_all_parsing_templates, " "probably due to server side error ",
                            )
            raise
        except AuthorizationException:
            logger.exception("Error occurred in list_all_parsing_templates, " "probably due to invalid authz token ",
                             )
            raise

    def _load_settings(self, configuration_file_location):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.api_server_settings.API_SERVER_HOST = config.get('APIServer', 'API_HOST')
            self.api_server_settings.API_SERVER_PORT = config.getint('APIServer', 'API_PORT')
            self.api_server_settings.API_SERVER_SECURE = config.getboolean('APIServer', 'API_SECURE')
