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

from airavata.api.error.ttypes import AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException
from airavata.model.appcatalog.groupresourceprofile.ttypes import GroupResourceProfile
from airavata.model.experiment.ttypes import ExperimentModel, ExperimentType, ProjectSearchFields, UserConfigurationDataModel
from airavata.model.workspace.ttypes import Gateway, GatewayApprovalStatus, Notification, Project
from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)
# create console handler with a higher log level
handler = logging.StreamHandler()
handler.setLevel(logging.DEBUG)

authenticator = Authenticator();
token = authenticator.get_token_and_user_info_password_flow("default-admin", "123456", "default")

# load APIServerClient with default configuration
client = APIServerClient()


# load client with given configuration file (e.g customized_settings.ini)

# client = APIServerClient('../transport/settings.ini')


# check for given gateway exists
def is_gateway_exists():
    try:
        is_exists = client.is_gateway_exist(token, "default")
        print("Gateway exist: " + str(is_exists))
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


# check if given user exists in given gateway
def is_user_exists():
    try:
        is_exists = client.is_user_exists(token, "default", "default-admin")
        print("User exist: " + str(is_exists))
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


# adding a new gateway
def add_gateway():
    try:
        gateway = Gateway()
        gateway.gatewayId = "test-gw"
        gateway.domain = "airavata.org"
        gateway.gatewayAdminEmail = "gw@gmail.com"
        gateway.gatewayAdminFirstName = "isuru"
        gateway.gatewayAdminLastName = "ranawaka"
        gateway.gatewayName = "test-gw"
        gateway.gatewayApprovalStatus = GatewayApprovalStatus.REQUESTED
        gateway_id = client.add_gateway(token, gateway)
        print("Gateway Id :" + gateway_id)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


# delete gateway
def delete_gateway():
    try:
        gateway = client.delete_gateway(token, "test-gw")
        print("Gateway deleted ", gateway)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


# get all exisisting gateways
def get_all_gateways():
    try:
        gateway = client.get_all_gateways(token)
        print("Get all gateways :", gateway)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


def create_notification():
    try:
        notification = Notification()
        notification.gatewayId = "default"
        notification.title = "default-gateway-notification"
        notification.notificationMessage = "Hello gateway"
        created_notification = client.create_notification(token, notification)
        print("Notification Created ", created_notification)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


def get_all_notifications():
    try:
        notifications = client.get_all_notifications(token, "default")
        print("Notifications ", notifications)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


def create_project():
    try:
        project = Project()
        project.projectID = "def1234"
        project.owner = "default-admin"
        project.gatewayId = "default"
        project.name = "defaultProject"

        pro = client.create_project(token, "default", project)
        print("Project created ", pro)

    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


def search_projects():
    try:
        filter = {ProjectSearchFields.PROJECT_DESCRIPTION: 'defaultProject'}
        projects = client.search_projects(token, "default-gateway", "default-admin", filter, limit=0, offset=10)
        print(projects)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


def create_experiment():
    try:
        experiment_model = ExperimentModel()
        experiment_model.experimentId = "exp123"
        experiment_model.projectId = "def1234"
        experiment_model.gatewayId = "default"
        experiment_model.experimentType = ExperimentType.SINGLE_APPLICATION
        experiment_model.userName = "default-admin"
        experiment_model.experimentName = "test_exp"
        exp = client.create_experiment(token, "default", experiment_model)
    
        print("Experiment created ", exp)

    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


def get_experiment():
    try:
        experiment = client.get_experiment(token, 'test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014')
        print("Experiment ", experiment);

    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


def create_group_resource_profile():
    try:
        group_resource = GroupResourceProfile()
        group_resource.gatewayId = "default"
        group_resource.groupResourceProfileId = "default_profile"
        group_resource.groupResourceProfileName = "default_profile_1"
        resource = client.create_group_resource_profile(token, group_resource)
        print("Group resource created ", group_resource)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")


def update_experiment():
    try:
        data_model = UserConfigurationDataModel()
        data_model.groupResourceProfileId = "default_profile"
        data_model.airavataAutoSchedule = True
        data_model.overrideManualScheduledParams = True
        experiment = client.get_experiment(token, 'test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014')
        experiment.userConfigurationData = data_model
        exp = client.update_experiment(token, 'test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014', experiment)
        print("Updated Experiment ", exp)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
         logger.exception("Error occurred")


def launch_experiment():
    try:
        status = client.launch_experiment(token, 'test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014', 'default')
        print("Experiment Status ", status)
    except (InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException):
        logger.exception("Error occurred")
