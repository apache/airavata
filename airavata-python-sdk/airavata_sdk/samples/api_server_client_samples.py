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

from airavata_sdk.generated.org.apache.airavata.model.workspace import workspace_pb2
from airavata_sdk.generated.org.apache.airavata.model.experiment import experiment_pb2
from airavata_sdk.generated.org.apache.airavata.model.appcatalog.groupresourceprofile import group_resource_profile_pb2
from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)
# create console handler with a higher log level
handler = logging.StreamHandler()
handler.setLevel(logging.DEBUG)

authenticator = Authenticator()
token = authenticator.get_token_and_user_info_password_flow("default-admin", "123456", "default")

# load APIServerClient with access token
client = APIServerClient(access_token=token)


# check for given gateway exists
def is_gateway_exists():
    try:
        is_exists = client.is_gateway_exist("default")
        print("Gateway exist: " + str(is_exists))
    except Exception:
        logger.exception("Error occurred")


# check if given user exists in given gateway
def is_user_exists():
    try:
        is_exists = client.is_user_exists("default", "default-admin")
        print("User exist: " + str(is_exists))
    except Exception:
        logger.exception("Error occurred")


# adding a new gateway
def add_gateway():
    try:
        gateway = workspace_pb2.Gateway(
            gateway_id="test-gw",
            domain="airavata.org",
            gateway_admin_email="gw@gmail.com",
            gateway_admin_first_name="isuru",
            gateway_admin_last_name="ranawaka",
            gateway_name="test-gw",
            gateway_approval_status=workspace_pb2.REQUESTED,
        )
        gateway_id = client.add_gateway(gateway)
        print("Gateway Id :" + str(gateway_id))
    except Exception:
        logger.exception("Error occurred")


# delete gateway
def delete_gateway():
    try:
        gateway = client.delete_gateway("test-gw")
        print("Gateway deleted ", gateway)
    except Exception:
        logger.exception("Error occurred")


# get all existing gateways
def get_all_gateways():
    try:
        gateway = client.get_all_gateways()
        print("Get all gateways :", gateway)
    except Exception:
        logger.exception("Error occurred")


def create_notification():
    try:
        notification = workspace_pb2.Notification(
            gateway_id="default",
            title="default-gateway-notification",
            notification_message="Hello gateway",
        )
        created_notification = client.create_notification(notification)
        print("Notification Created ", created_notification)
    except Exception:
        logger.exception("Error occurred")


def get_all_notifications():
    try:
        notifications = client.get_all_notifications("default")
        print("Notifications ", notifications)
    except Exception:
        logger.exception("Error occurred")


def create_project():
    try:
        project = workspace_pb2.Project(
            project_id="def1234",
            owner="default-admin",
            gateway_id="default",
            name="defaultProject",
        )
        pro = client.create_project("default", project)
        print("Project created ", pro)
    except Exception:
        logger.exception("Error occurred")


def search_projects():
    try:
        filters = {experiment_pb2.PROJECT_DESCRIPTION: "defaultProject"}
        projects = client.search_projects("default-gateway", "default-admin", filters, limit=10, offset=0)
        print(projects)
    except Exception:
        logger.exception("Error occurred")


def create_experiment():
    try:
        experiment_model = experiment_pb2.ExperimentModel(
            experiment_id="exp123",
            project_id="def1234",
            gateway_id="default",
            experiment_type=experiment_pb2.SINGLE_APPLICATION,
            user_name="default-admin",
            experiment_name="test_exp",
        )
        exp = client.create_experiment("default", experiment_model)
        print("Experiment created ", exp)
    except Exception:
        logger.exception("Error occurred")


def get_experiment():
    try:
        experiment = client.get_experiment("test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014")
        print("Experiment ", experiment)
    except Exception:
        logger.exception("Error occurred")


def create_group_resource_profile():
    try:
        group_resource = group_resource_profile_pb2.GroupResourceProfile(
            gateway_id="default",
            group_resource_profile_id="default_profile",
            group_resource_profile_name="default_profile_1",
        )
        resource = client.create_group_resource_profile(group_resource)
        print("Group resource created ", resource)
    except Exception:
        logger.exception("Error occurred")


def update_experiment():
    try:
        data_model = experiment_pb2.UserConfigurationDataModel(
            group_resource_profile_id="default_profile",
            airavata_auto_schedule=True,
            override_manual_scheduled_params=True,
        )
        experiment = client.get_experiment("test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014")
        experiment.user_configuration_data.CopyFrom(data_model)
        exp = client.update_experiment("test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014", experiment)
        print("Updated Experiment ", exp)
    except Exception:
        logger.exception("Error occurred")


def launch_experiment():
    try:
        status = client.launch_experiment("test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014", "default")
        print("Experiment Status ", status)
    except Exception:
        logger.exception("Error occurred")
