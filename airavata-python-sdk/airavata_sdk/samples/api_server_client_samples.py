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

from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)
# create console handler with a higher log level
handler = logging.StreamHandler()
handler.setLevel(logging.DEBUG)

authenticator = Authenticator()
token = authenticator.get_token_and_user_info_password_flow("default-admin", "admin123", "default")

# load APIServerClient with default configuration
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
        is_exists = client.is_user_exists("default-admin", "default")
        print("User exist: " + str(is_exists))
    except Exception:
        logger.exception("Error occurred")


# adding a new gateway
def add_gateway():
    try:
        gateway = {
            "gatewayId": "test-gw",
            "domain": "airavata.org",
            "gatewayAdminEmail": "gw@gmail.com",
            "gatewayAdminFirstName": "isuru",
            "gatewayAdminLastName": "ranawaka",
            "gatewayName": "test-gw",
            "gatewayApprovalStatus": "REQUESTED",
        }
        gateway_id = client.add_gateway(gateway)
        print("Gateway Id :" + gateway_id)
    except Exception:
        logger.exception("Error occurred")


# delete gateway
def delete_gateway():
    try:
        client.delete_gateway("test-gw")
        print("Gateway deleted")
    except Exception:
        logger.exception("Error occurred")


# get all existing gateways
def get_all_gateways():
    try:
        gateways = client.get_all_gateways()
        print("Get all gateways :", gateways)
    except Exception:
        logger.exception("Error occurred")


def create_notification():
    try:
        notification = {
            "gatewayId": "default",
            "title": "default-gateway-notification",
            "notificationMessage": "Hello gateway",
        }
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
        project = {
            "projectID": "def1234",
            "owner": "default-admin",
            "gatewayId": "default",
            "name": "defaultProject",
        }
        pro = client.create_project(project, "default")
        print("Project created ", pro)
    except Exception:
        logger.exception("Error occurred")


def search_projects():
    try:
        projects = client.search_projects("default-gateway", limit=0, offset=10, projectDescription="defaultProject")
        print(projects)
    except Exception:
        logger.exception("Error occurred")


def create_experiment():
    try:
        experiment = {
            "experimentId": "exp123",
            "projectId": "def1234",
            "gatewayId": "default",
            "experimentType": "SINGLE_APPLICATION",
            "userName": "default-admin",
            "experimentName": "test_exp",
        }
        exp = client.create_experiment(experiment, "default")
        print("Experiment created ", exp)
    except Exception:
        logger.exception("Error occurred")


def get_experiment():
    try:
        experiment = client.get_experiment('test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014')
        print("Experiment ", experiment)
    except Exception:
        logger.exception("Error occurred")


def create_group_resource_profile():
    try:
        group_resource = {
            "gatewayId": "default",
            "groupResourceProfileId": "default_profile",
            "groupResourceProfileName": "default_profile_1",
        }
        resource = client.create_group_resource_profile(group_resource)
        print("Group resource created ", resource)
    except Exception:
        logger.exception("Error occurred")


def update_experiment():
    try:
        experiment = client.get_experiment('test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014')
        experiment["userConfigurationData"] = {
            "groupResourceProfileId": "default_profile",
            "airavataAutoSchedule": True,
            "overrideManualScheduledParams": True,
        }
        exp = client.update_experiment('test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014', experiment)
        print("Updated Experiment ", exp)
    except Exception:
         logger.exception("Error occurred")


def launch_experiment():
    try:
        client.launch_experiment('test_exp_26302f87-c8eb-4d44-8b6b-4a5c7b1ff014', 'default')
        print("Experiment launched")
    except Exception:
        logger.exception("Error occurred")
