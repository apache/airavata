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

import json
import logging
from typing import Optional

import grpc

from airavata_sdk import Settings
from airavata_sdk.transport.utils import (
    create_application_catalog_service_stub,
    create_credential_service_stub,
    create_data_product_service_stub,
    create_experiment_service_stub,
    create_gateway_resource_profile_service_stub,
    create_gateway_service_stub,
    create_group_resource_profile_service_stub,
    create_notification_service_stub,
    create_parser_service_stub,
    create_project_service_stub,
    create_resource_service_stub,
    create_sharing_service_stub,
    create_user_resource_profile_service_stub,
)

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class APIServerClient:
    """Unified facade over the 13 Airavata gRPC services.

    Provides the same public method names as the old Thrift-based client,
    but delegates internally to the appropriate gRPC service stub.
    All methods accept protobuf message types (not Thrift types).
    """

    def __init__(self, access_token: Optional[str] = None, claims: Optional[dict] = None):
        self.settings = Settings()
        host = self.settings.API_SERVER_HOSTNAME
        port = self.settings.API_SERVER_PORT
        secure = self.settings.API_SERVER_SECURE

        target = f"{host}:{port}"
        if secure:
            self.channel = grpc.secure_channel(target, grpc.ssl_channel_credentials())
        else:
            self.channel = grpc.insecure_channel(target)

        # Auth metadata sent with every call
        self._metadata: list[tuple[str, str]] = []
        if access_token:
            self._metadata.append(("authorization", f"Bearer {access_token}"))
        if claims:
            self._metadata.append(("x-claims", json.dumps(claims)))

        # Create all service stubs
        self._experiment = create_experiment_service_stub(self.channel)
        self._project = create_project_service_stub(self.channel)
        self._gateway = create_gateway_service_stub(self.channel)
        self._app_catalog = create_application_catalog_service_stub(self.channel)
        self._resource = create_resource_service_stub(self.channel)
        self._credential = create_credential_service_stub(self.channel)
        self._sharing = create_sharing_service_stub(self.channel)
        self._notification = create_notification_service_stub(self.channel)
        self._data_product = create_data_product_service_stub(self.channel)
        self._gw_profile = create_gateway_resource_profile_service_stub(self.channel)
        self._user_profile = create_user_resource_profile_service_stub(self.channel)
        self._grp_profile = create_group_resource_profile_service_stub(self.channel)
        self._parser = create_parser_service_stub(self.channel)

    def close(self):
        self.channel.close()

    # ----------------------------------------------------------------
    # Lazy imports for request types (avoids top-level import overhead)
    # ----------------------------------------------------------------

    @staticmethod
    def _svc(module_name: str):
        """Import and return a service pb2 module."""
        import importlib
        return importlib.import_module(f"airavata_sdk.generated.services.{module_name}")

    # ================================================================
    # Gateway Service
    # ================================================================

    def is_user_exists(self, gateway_id, user_name):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.IsUserExists(
            pb2.IsUserExistsRequest(gateway_id=gateway_id, user_name=user_name),
            metadata=self._metadata,
        )

    def add_gateway(self, gateway):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.AddGateway(
            pb2.AddGatewayRequest(gateway=gateway),
            metadata=self._metadata,
        )

    def get_all_users_in_gateway(self, gateway_id):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.GetAllUsersInGateway(
            pb2.GetAllUsersInGatewayRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def update_gateway(self, gateway_id, gateway):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.UpdateGateway(
            pb2.UpdateGatewayRequest(gateway_id=gateway_id, gateway=gateway),
            metadata=self._metadata,
        )

    def get_gateway(self, gateway_id):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.GetGateway(
            pb2.GetGatewayRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_gateway(self, gateway_id):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.DeleteGateway(
            pb2.DeleteGatewayRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_gateways(self):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.GetAllGateways(
            pb2.GetAllGatewaysRequest(),
            metadata=self._metadata,
        )

    def is_gateway_exist(self, gateway_id):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.IsGatewayExist(
            pb2.IsGatewayExistRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Notification Service
    # ================================================================

    def create_notification(self, notification):
        pb2 = self._svc("notification_service_pb2")
        return self._notification.CreateNotification(
            pb2.CreateNotificationRequest(notification=notification),
            metadata=self._metadata,
        )

    def update_notification(self, notification):
        pb2 = self._svc("notification_service_pb2")
        return self._notification.UpdateNotification(
            pb2.UpdateNotificationRequest(notification=notification),
            metadata=self._metadata,
        )

    def delete_notification(self, gateway_id, notification_id):
        pb2 = self._svc("notification_service_pb2")
        return self._notification.DeleteNotification(
            pb2.DeleteNotificationRequest(gateway_id=gateway_id, notification_id=notification_id),
            metadata=self._metadata,
        )

    def get_notification(self, gateway_id, notification_id):
        pb2 = self._svc("notification_service_pb2")
        return self._notification.GetNotification(
            pb2.GetNotificationRequest(gateway_id=gateway_id, notification_id=notification_id),
            metadata=self._metadata,
        )

    def get_all_notifications(self, gateway_id):
        pb2 = self._svc("notification_service_pb2")
        return self._notification.GetAllNotifications(
            pb2.GetAllNotificationsRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Credential Service
    # ================================================================

    def generate_and_register_ssh_keys(self, gateway_id, username, description=""):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.GenerateAndRegisterSSHKeys(
            pb2.GenerateAndRegisterSSHKeysRequest(gateway_id=gateway_id, username=username, description=description),
            metadata=self._metadata,
        )

    def register_pwd_credential(self, gateway_id, password_credential):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.RegisterPwdCredential(
            pb2.RegisterPwdCredentialRequest(gateway_id=gateway_id, password_credential=password_credential),
            metadata=self._metadata,
        )

    def get_credential_summary(self, token_id, gateway_id):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.GetCredentialSummary(
            pb2.GetCredentialSummaryRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_credential_summaries(self, gateway_id, type):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.GetAllCredentialSummaries(
            pb2.GetAllCredentialSummariesRequest(gateway_id=gateway_id, type=type),
            metadata=self._metadata,
        )

    def delete_ssh_pub_key(self, token_id, gateway_id):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.DeleteSSHPubKey(
            pb2.DeleteSSHPubKeyRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_pwd_credential(self, token_id, gateway_id):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.DeletePWDCredential(
            pb2.DeletePWDCredentialRequest(token_id=token_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Project Service
    # ================================================================

    def create_project(self, gateway_id, project):
        pb2 = self._svc("project_service_pb2")
        return self._project.CreateProject(
            pb2.CreateProjectRequest(gateway_id=gateway_id, project=project),
            metadata=self._metadata,
        )

    def update_project(self, project_id, project):
        pb2 = self._svc("project_service_pb2")
        return self._project.UpdateProject(
            pb2.UpdateProjectRequest(project_id=project_id, project=project),
            metadata=self._metadata,
        )

    def get_project(self, project_id):
        pb2 = self._svc("project_service_pb2")
        return self._project.GetProject(
            pb2.GetProjectRequest(project_id=project_id),
            metadata=self._metadata,
        )

    def delete_project(self, project_id):
        pb2 = self._svc("project_service_pb2")
        return self._project.DeleteProject(
            pb2.DeleteProjectRequest(project_id=project_id),
            metadata=self._metadata,
        )

    def get_user_projects(self, gateway_id, user_name, limit=-1, offset=0):
        pb2 = self._svc("project_service_pb2")
        return self._project.GetUserProjects(
            pb2.GetUserProjectsRequest(gateway_id=gateway_id, user_name=user_name, limit=limit, offset=offset),
            metadata=self._metadata,
        )

    def search_projects(self, gateway_id, user_name, filters=None, limit=-1, offset=0):
        pb2 = self._svc("project_service_pb2")
        return self._project.SearchProjects(
            pb2.SearchProjectsRequest(gateway_id=gateway_id, user_name=user_name, filters=filters or {}, limit=limit, offset=offset),
            metadata=self._metadata,
        )

    # ================================================================
    # Experiment Service
    # ================================================================

    def search_experiments(self, gateway_id, user_name, filters=None, limit=-1, offset=0):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.SearchExperiments(
            pb2.SearchExperimentsRequest(gateway_id=gateway_id, user_name=user_name, filters=filters or {}, limit=limit, offset=offset),
            metadata=self._metadata,
        )

    def get_experiment_statistics(self, gateway_id, from_time, to_time, user_name="", application_name="", resource_host_name="", limit=0, offset=0):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperimentStatistics(
            pb2.GetExperimentStatisticsRequest(
                gateway_id=gateway_id, from_time=from_time, to_time=to_time,
                user_name=user_name, application_name=application_name,
                resource_host_name=resource_host_name, limit=limit, offset=offset,
            ),
            metadata=self._metadata,
        )

    def get_experiments_in_project(self, project_id, limit=-1, offset=0):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperimentsInProject(
            pb2.GetExperimentsInProjectRequest(project_id=project_id, limit=limit, offset=offset),
            metadata=self._metadata,
        )

    def get_user_experiments(self, gateway_id, user_name, limit=-1, offset=0):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetUserExperiments(
            pb2.GetUserExperimentsRequest(gateway_id=gateway_id, user_name=user_name, limit=limit, offset=offset),
            metadata=self._metadata,
        )

    def create_experiment(self, gateway_id, experiment):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.CreateExperiment(
            pb2.CreateExperimentRequest(gateway_id=gateway_id, experiment=experiment),
            metadata=self._metadata,
        )

    def delete_experiment(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.DeleteExperiment(
            pb2.DeleteExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_experiment(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperiment(
            pb2.GetExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_experiment_by_admin(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperimentByAdmin(
            pb2.GetExperimentByAdminRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_detailed_experiment_tree(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetDetailedExperimentTree(
            pb2.GetDetailedExperimentTreeRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def update_experiment(self, experiment_id, experiment):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.UpdateExperiment(
            pb2.UpdateExperimentRequest(experiment_id=experiment_id, experiment=experiment),
            metadata=self._metadata,
        )

    def update_experiment_configuration(self, experiment_id, configuration):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.UpdateExperimentConfiguration(
            pb2.UpdateExperimentConfigurationRequest(experiment_id=experiment_id, configuration=configuration),
            metadata=self._metadata,
        )

    def update_resource_scheduling(self, experiment_id, scheduling):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.UpdateResourceScheduling(
            pb2.UpdateResourceSchedulingRequest(experiment_id=experiment_id, scheduling=scheduling),
            metadata=self._metadata,
        )

    def validate_experiment(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.ValidateExperiment(
            pb2.ValidateExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def launch_experiment(self, experiment_id, gateway_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.LaunchExperiment(
            pb2.LaunchExperimentRequest(experiment_id=experiment_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_experiment_status(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperimentStatus(
            pb2.GetExperimentStatusRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_experiment_outputs(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetExperimentOutputs(
            pb2.GetExperimentOutputsRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_intermediate_outputs(self, experiment_id, output_names=None):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.FetchIntermediateOutputs(
            pb2.FetchIntermediateOutputsRequest(experiment_id=experiment_id, output_names=output_names or []),
            metadata=self._metadata,
        )

    def get_intermediate_output_process_status(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetIntermediateOutputProcessStatus(
            pb2.GetIntermediateOutputProcessStatusRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_job_statuses(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetJobStatuses(
            pb2.GetJobStatusesRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_job_details(self, experiment_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.GetJobDetails(
            pb2.GetJobDetailsRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def clone_experiment(self, experiment_id, new_experiment_name="", new_experiment_project_id=""):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.CloneExperiment(
            pb2.CloneExperimentRequest(
                experiment_id=experiment_id,
                new_experiment_name=new_experiment_name,
                new_experiment_project_id=new_experiment_project_id,
            ),
            metadata=self._metadata,
        )

    def clone_experiment_by_admin(self, experiment_id, new_experiment_name="", new_experiment_project_id=""):
        """Clone experiment with admin privileges. Uses the same gRPC RPC as clone_experiment."""
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.CloneExperiment(
            pb2.CloneExperimentRequest(
                experiment_id=experiment_id,
                new_experiment_name=new_experiment_name,
                new_experiment_project_id=new_experiment_project_id,
            ),
            metadata=self._metadata,
        )

    def terminate_experiment(self, experiment_id, gateway_id):
        pb2 = self._svc("experiment_service_pb2")
        return self._experiment.TerminateExperiment(
            pb2.TerminateExperimentRequest(experiment_id=experiment_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Application Catalog Service
    # ================================================================

    def register_application_module(self, gateway_id, application_module):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.RegisterApplicationModule(
            pb2.RegisterApplicationModuleRequest(gateway_id=gateway_id, application_module=application_module),
            metadata=self._metadata,
        )

    def get_application_module(self, app_module_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetApplicationModule(
            pb2.GetApplicationModuleRequest(app_module_id=app_module_id),
            metadata=self._metadata,
        )

    def update_application_module(self, app_module_id, application_module):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.UpdateApplicationModule(
            pb2.UpdateApplicationModuleRequest(app_module_id=app_module_id, application_module=application_module),
            metadata=self._metadata,
        )

    def get_all_app_modules(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetAllAppModules(
            pb2.GetAllAppModulesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_accessible_app_modules(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetAccessibleAppModules(
            pb2.GetAccessibleAppModulesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_application_module(self, app_module_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.DeleteApplicationModule(
            pb2.DeleteApplicationModuleRequest(app_module_id=app_module_id),
            metadata=self._metadata,
        )

    def register_application_deployment(self, gateway_id, application_deployment):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.RegisterApplicationDeployment(
            pb2.RegisterApplicationDeploymentRequest(gateway_id=gateway_id, application_deployment=application_deployment),
            metadata=self._metadata,
        )

    def get_application_deployment(self, app_deployment_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetApplicationDeployment(
            pb2.GetApplicationDeploymentRequest(app_deployment_id=app_deployment_id),
            metadata=self._metadata,
        )

    def update_application_deployment(self, app_deployment_id, application_deployment):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.UpdateApplicationDeployment(
            pb2.UpdateApplicationDeploymentRequest(app_deployment_id=app_deployment_id, application_deployment=application_deployment),
            metadata=self._metadata,
        )

    def delete_application_deployment(self, app_deployment_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.DeleteApplicationDeployment(
            pb2.DeleteApplicationDeploymentRequest(app_deployment_id=app_deployment_id),
            metadata=self._metadata,
        )

    def get_all_application_deployments(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetAllApplicationDeployments(
            pb2.GetAllApplicationDeploymentsRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_accessible_application_deployments(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetAccessibleApplicationDeployments(
            pb2.GetAccessibleApplicationDeploymentsRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_app_module_deployed_resources(self, app_module_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetAppModuleDeployedResources(
            pb2.GetAppModuleDeployedResourcesRequest(app_module_id=app_module_id),
            metadata=self._metadata,
        )

    def get_application_deployments_for_app_module_and_group_resource_profile(self, app_module_id, group_resource_profile_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetDeploymentsForModuleAndProfile(
            pb2.GetDeploymentsForModuleAndProfileRequest(app_module_id=app_module_id, group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )

    def register_application_interface(self, gateway_id, application_interface):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.RegisterApplicationInterface(
            pb2.RegisterApplicationInterfaceRequest(gateway_id=gateway_id, application_interface=application_interface),
            metadata=self._metadata,
        )

    def clone_application_interface(self, existing_app_interface_id, new_app_module_name, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.CloneApplicationInterface(
            pb2.CloneApplicationInterfaceRequest(
                existing_app_interface_id=existing_app_interface_id,
                new_app_module_name=new_app_module_name,
                gateway_id=gateway_id,
            ),
            metadata=self._metadata,
        )

    def get_application_interface(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetApplicationInterface(
            pb2.GetApplicationInterfaceRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )

    def update_application_interface(self, app_interface_id, application_interface):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.UpdateApplicationInterface(
            pb2.UpdateApplicationInterfaceRequest(app_interface_id=app_interface_id, application_interface=application_interface),
            metadata=self._metadata,
        )

    def delete_application_interface(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.DeleteApplicationInterface(
            pb2.DeleteApplicationInterfaceRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )

    def get_all_application_interface_names(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetAllApplicationInterfaceNames(
            pb2.GetAllApplicationInterfaceNamesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_application_interfaces(self, gateway_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetAllApplicationInterfaces(
            pb2.GetAllApplicationInterfacesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_application_inputs(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetApplicationInputs(
            pb2.GetApplicationInputsRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )

    def get_application_outputs(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetApplicationOutputs(
            pb2.GetApplicationOutputsRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )

    def get_available_app_interface_compute_resources(self, app_interface_id):
        pb2 = self._svc("application_catalog_service_pb2")
        return self._app_catalog.GetAvailableComputeResources(
            pb2.GetAvailableComputeResourcesRequest(app_interface_id=app_interface_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Resource Service (compute, storage, submissions, data movement)
    # ================================================================

    def register_compute_resource(self, compute_resource):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.RegisterComputeResource(
            pb2.RegisterComputeResourceRequest(compute_resource=compute_resource),
            metadata=self._metadata,
        )

    def get_compute_resource(self, compute_resource_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetComputeResource(
            pb2.GetComputeResourceRequest(compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def get_all_compute_resource_names(self):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetAllComputeResourceNames(
            pb2.GetAllComputeResourceNamesRequest(),
            metadata=self._metadata,
        )

    def update_compute_resource(self, compute_resource_id, compute_resource):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateComputeResource(
            pb2.UpdateComputeResourceRequest(compute_resource_id=compute_resource_id, compute_resource=compute_resource),
            metadata=self._metadata,
        )

    def delete_compute_resource(self, compute_resource_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.DeleteComputeResource(
            pb2.DeleteComputeResourceRequest(compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def register_storage_resource(self, storage_resource):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.RegisterStorageResource(
            pb2.RegisterStorageResourceRequest(storage_resource=storage_resource),
            metadata=self._metadata,
        )

    def get_storage_resource(self, storage_resource_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetStorageResource(
            pb2.GetStorageResourceRequest(storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    def get_all_storage_resource_names(self):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetAllStorageResourceNames(
            pb2.GetAllStorageResourceNamesRequest(),
            metadata=self._metadata,
        )

    def update_storage_resource(self, storage_resource_id, storage_resource):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateStorageResource(
            pb2.UpdateStorageResourceRequest(storage_resource_id=storage_resource_id, storage_resource=storage_resource),
            metadata=self._metadata,
        )

    def delete_storage_resource(self, storage_resource_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.DeleteStorageResource(
            pb2.DeleteStorageResourceRequest(storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    def get_resource_storage_info(self, storage_resource_id):
        """Get storage resource info. Delegates to GetStorageResource."""
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetStorageResource(
            pb2.GetStorageResourceRequest(storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    # --- Job Submission ---

    def add_local_submission_details(self, compute_resource_id, priority, local_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.AddLocalSubmission(
            pb2.AddLocalSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, local_submission=local_submission),
            metadata=self._metadata,
        )

    def update_local_submission_details(self, submission_id, local_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateLocalSubmission(
            pb2.UpdateLocalSubmissionRequest(submission_id=submission_id, local_submission=local_submission),
            metadata=self._metadata,
        )

    def get_local_job_submission(self, submission_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetLocalJobSubmission(
            pb2.GetLocalJobSubmissionRequest(submission_id=submission_id),
            metadata=self._metadata,
        )

    def add_ssh_job_submission_details(self, compute_resource_id, priority, ssh_job_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.AddSSHJobSubmission(
            pb2.AddSSHJobSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, ssh_job_submission=ssh_job_submission),
            metadata=self._metadata,
        )

    def add_ssh_fork_job_submission_details(self, compute_resource_id, priority, ssh_job_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.AddSSHForkJobSubmission(
            pb2.AddSSHForkJobSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, ssh_job_submission=ssh_job_submission),
            metadata=self._metadata,
        )

    def get_ssh_job_submission(self, submission_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetSSHJobSubmission(
            pb2.GetSSHJobSubmissionRequest(submission_id=submission_id),
            metadata=self._metadata,
        )

    def add_unicore_job_submission_details(self, compute_resource_id, priority, unicore_job_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.AddUnicoreJobSubmission(
            pb2.AddUnicoreJobSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, unicore_job_submission=unicore_job_submission),
            metadata=self._metadata,
        )

    def get_unicore_job_submission(self, submission_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetUnicoreJobSubmission(
            pb2.GetUnicoreJobSubmissionRequest(submission_id=submission_id),
            metadata=self._metadata,
        )

    def add_cloud_job_submission_details(self, compute_resource_id, priority, cloud_job_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.AddCloudJobSubmission(
            pb2.AddCloudJobSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, cloud_job_submission=cloud_job_submission),
            metadata=self._metadata,
        )

    def get_cloud_job_submission(self, submission_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetCloudJobSubmission(
            pb2.GetCloudJobSubmissionRequest(submission_id=submission_id),
            metadata=self._metadata,
        )

    def update_ssh_job_submission_details(self, submission_id, ssh_job_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateSSHJobSubmission(
            pb2.UpdateSSHJobSubmissionRequest(submission_id=submission_id, ssh_job_submission=ssh_job_submission),
            metadata=self._metadata,
        )

    def update_cloud_job_submission_details(self, submission_id, cloud_job_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateCloudJobSubmission(
            pb2.UpdateCloudJobSubmissionRequest(submission_id=submission_id, cloud_job_submission=cloud_job_submission),
            metadata=self._metadata,
        )

    def update_unicore_job_submission_details(self, submission_id, unicore_job_submission):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateUnicoreJobSubmission(
            pb2.UpdateUnicoreJobSubmissionRequest(submission_id=submission_id, unicore_job_submission=unicore_job_submission),
            metadata=self._metadata,
        )

    # --- Data Movement ---

    def add_local_data_movement_details(self, compute_resource_id, priority, dm_type, local_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.AddLocalDataMovement(
            pb2.AddLocalDataMovementRequest(compute_resource_id=compute_resource_id, priority=priority, dm_type=dm_type, local_data_movement=local_data_movement),
            metadata=self._metadata,
        )

    def update_local_data_movement_details(self, data_movement_id, local_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateLocalDataMovement(
            pb2.UpdateLocalDataMovementRequest(data_movement_id=data_movement_id, local_data_movement=local_data_movement),
            metadata=self._metadata,
        )

    def get_local_data_movement(self, data_movement_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetLocalDataMovement(
            pb2.GetLocalDataMovementRequest(data_movement_id=data_movement_id),
            metadata=self._metadata,
        )

    def add_scp_data_movement_details(self, compute_resource_id, priority, dm_type, scp_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.AddSCPDataMovement(
            pb2.AddSCPDataMovementRequest(compute_resource_id=compute_resource_id, priority=priority, dm_type=dm_type, scp_data_movement=scp_data_movement),
            metadata=self._metadata,
        )

    def update_scp_data_movement_details(self, data_movement_id, scp_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateSCPDataMovement(
            pb2.UpdateSCPDataMovementRequest(data_movement_id=data_movement_id, scp_data_movement=scp_data_movement),
            metadata=self._metadata,
        )

    def get_scp_data_movement(self, data_movement_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetSCPDataMovement(
            pb2.GetSCPDataMovementRequest(data_movement_id=data_movement_id),
            metadata=self._metadata,
        )

    def add_unicore_data_movement_details(self, compute_resource_id, priority, dm_type, unicore_data_movement):
        """Not directly available in gRPC stubs. Maps to GridFTP as placeholder."""
        raise NotImplementedError("Unicore data movement is not available in the gRPC API")

    def update_unicore_data_movement_details(self, data_movement_id, unicore_data_movement):
        raise NotImplementedError("Unicore data movement is not available in the gRPC API")

    def get_unicore_data_movement(self, data_movement_id):
        raise NotImplementedError("Unicore data movement is not available in the gRPC API")

    def add_grid_ftp_data_movement_details(self, compute_resource_id, priority, dm_type, gridftp_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.AddGridFTPDataMovement(
            pb2.AddGridFTPDataMovementRequest(compute_resource_id=compute_resource_id, priority=priority, dm_type=dm_type, gridftp_data_movement=gridftp_data_movement),
            metadata=self._metadata,
        )

    def update_grid_ftp_data_movement_details(self, data_movement_id, gridftp_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateGridFTPDataMovement(
            pb2.UpdateGridFTPDataMovementRequest(data_movement_id=data_movement_id, gridftp_data_movement=gridftp_data_movement),
            metadata=self._metadata,
        )

    def get_grid_ftp_data_movement(self, data_movement_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetGridFTPDataMovement(
            pb2.GetGridFTPDataMovementRequest(data_movement_id=data_movement_id),
            metadata=self._metadata,
        )

    def change_job_submission_priority(self, submission_id, priority):
        raise NotImplementedError("changeJobSubmissionPriority not available in gRPC API; manage via update methods")

    def change_data_movement_priority(self, data_movement_id, priority):
        raise NotImplementedError("changeDataMovementPriority not available in gRPC API; manage via update methods")

    def change_job_submission_priorities(self, priorities):
        raise NotImplementedError("changeJobSubmissionPriorities not available in gRPC API; manage via update methods")

    def change_data_movement_priorities(self, priorities):
        raise NotImplementedError("changeDataMovementPriorities not available in gRPC API; manage via update methods")

    def delete_job_submission_interface(self, compute_resource_id, submission_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.DeleteJobSubmissionInterface(
            pb2.DeleteJobSubmissionInterfaceRequest(compute_resource_id=compute_resource_id, submission_id=submission_id),
            metadata=self._metadata,
        )

    def delete_data_movement_interface(self, compute_resource_id, data_movement_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.DeleteDataMovementInterface(
            pb2.DeleteDataMovementInterfaceRequest(compute_resource_id=compute_resource_id, data_movement_id=data_movement_id),
            metadata=self._metadata,
        )

    def register_resource_job_manager(self, resource_job_manager):
        raise NotImplementedError("registerResourceJobManager not available in gRPC API")

    def update_resource_job_manager(self, resource_job_manager_id, resource_job_manager):
        raise NotImplementedError("updateResourceJobManager not available in gRPC API")

    def get_resource_job_manager(self, resource_job_manager_id):
        raise NotImplementedError("getResourceJobManager not available in gRPC API")

    def delete_resource_job_manager(self, resource_job_manager_id):
        raise NotImplementedError("deleteResourceJobManager not available in gRPC API")

    def delete_batch_queue(self, compute_resource_id, queue_name):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.DeleteBatchQueue(
            pb2.DeleteBatchQueueRequest(compute_resource_id=compute_resource_id, queue_name=queue_name),
            metadata=self._metadata,
        )

    # ================================================================
    # Gateway Resource Profile Service
    # ================================================================

    def register_gateway_resource_profile(self, gateway_resource_profile):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.RegisterGatewayResourceProfile(
            pb2.RegisterGatewayResourceProfileRequest(gateway_resource_profile=gateway_resource_profile),
            metadata=self._metadata,
        )

    def get_gateway_resource_profile(self, gateway_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.GetGatewayResourceProfile(
            pb2.GetGatewayResourceProfileRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def update_gateway_resource_profile(self, gateway_id, gateway_resource_profile):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.UpdateGatewayResourceProfile(
            pb2.UpdateGatewayResourceProfileRequest(gateway_id=gateway_id, gateway_resource_profile=gateway_resource_profile),
            metadata=self._metadata,
        )

    def delete_gateway_resource_profile(self, gateway_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.DeleteGatewayResourceProfile(
            pb2.DeleteGatewayResourceProfileRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def add_gateway_compute_resource_preference(self, gateway_id, compute_resource_id, compute_resource_preference):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.AddComputePreference(
            pb2.AddComputePreferenceRequest(gateway_id=gateway_id, compute_resource_id=compute_resource_id, compute_resource_preference=compute_resource_preference),
            metadata=self._metadata,
        )

    def add_gateway_storage_preference(self, gateway_id, storage_resource_id, storage_preference):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.AddStoragePreference(
            pb2.AddStoragePreferenceRequest(gateway_id=gateway_id, storage_resource_id=storage_resource_id, storage_preference=storage_preference),
            metadata=self._metadata,
        )

    def get_gateway_compute_resource_preference(self, gateway_id, compute_resource_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.GetComputePreference(
            pb2.GetComputePreferenceRequest(gateway_id=gateway_id, compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def get_gateway_storage_preference(self, gateway_id, storage_resource_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.GetStoragePreference(
            pb2.GetStoragePreferenceRequest(gateway_id=gateway_id, storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    def get_all_gateway_compute_resource_preferences(self, gateway_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.GetAllComputePreferences(
            pb2.GetAllComputePreferencesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_gateway_storage_preferences(self, gateway_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.GetAllStoragePreferences(
            pb2.GetAllStoragePreferencesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_gateway_resource_profiles(self):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.GetAllGatewayResourceProfiles(
            pb2.GetAllGatewayResourceProfilesRequest(),
            metadata=self._metadata,
        )

    def update_gateway_compute_resource_preference(self, gateway_id, compute_resource_id, compute_resource_preference):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.UpdateComputePreference(
            pb2.UpdateComputePreferenceRequest(gateway_id=gateway_id, compute_resource_id=compute_resource_id, compute_resource_preference=compute_resource_preference),
            metadata=self._metadata,
        )

    def update_gateway_storage_preference(self, gateway_id, storage_resource_id, storage_preference):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.UpdateStoragePreference(
            pb2.UpdateStoragePreferenceRequest(gateway_id=gateway_id, storage_resource_id=storage_resource_id, storage_preference=storage_preference),
            metadata=self._metadata,
        )

    def delete_gateway_compute_resource_preference(self, gateway_id, compute_resource_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.DeleteComputePreference(
            pb2.DeleteComputePreferenceRequest(gateway_id=gateway_id, compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def delete_gateway_storage_preference(self, gateway_id, storage_resource_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.DeleteStoragePreference(
            pb2.DeleteStoragePreferenceRequest(gateway_id=gateway_id, storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    def get_ssh_account_provisioners(self):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        return self._gw_profile.GetSSHAccountProvisioners(
            pb2.GetSSHAccountProvisionersRequest(),
            metadata=self._metadata,
        )

    def does_user_have_ssh_account(self, compute_resource_id, gateway_id, username):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.DoesUserHaveSSHAccount(
            pb2.DoesUserHaveSSHAccountRequest(compute_resource_id=compute_resource_id, gateway_id=gateway_id, username=username),
            metadata=self._metadata,
        )

    def is_ssh_setup_complete_for_user_compute_resource_preference(self, compute_resource_id, gateway_id, username):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.IsSSHSetupComplete(
            pb2.IsSSHSetupCompleteRequest(compute_resource_id=compute_resource_id, gateway_id=gateway_id, username=username),
            metadata=self._metadata,
        )

    def setup_user_compute_resource_preferences_for_ssh(self, compute_resource_id, gateway_id, username):
        pb2 = self._svc("credential_service_pb2")
        return self._credential.SetupSSHAccount(
            pb2.SetupSSHAccountRequest(compute_resource_id=compute_resource_id, gateway_id=gateway_id, username=username),
            metadata=self._metadata,
        )

    # ================================================================
    # User Resource Profile Service
    # ================================================================

    def register_user_resource_profile(self, user_resource_profile):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.RegisterUserResourceProfile(
            pb2.RegisterUserResourceProfileRequest(user_resource_profile=user_resource_profile),
            metadata=self._metadata,
        )

    def is_user_resource_profile_exists(self, user_id, gateway_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.IsUserResourceProfileExists(
            pb2.IsUserResourceProfileExistsRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_user_resource_profile(self, user_id, gateway_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.GetUserResourceProfile(
            pb2.GetUserResourceProfileRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def update_user_resource_profile(self, user_id, gateway_id, user_resource_profile):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.UpdateUserResourceProfile(
            pb2.UpdateUserResourceProfileRequest(user_id=user_id, gateway_id=gateway_id, user_resource_profile=user_resource_profile),
            metadata=self._metadata,
        )

    def delete_user_resource_profile(self, user_id, gateway_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.DeleteUserResourceProfile(
            pb2.DeleteUserResourceProfileRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def add_user_compute_resource_preference(self, user_id, gateway_id, compute_resource_id, user_compute_resource_preference):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.AddUserComputePreference(
            pb2.AddUserComputePreferenceRequest(user_id=user_id, gateway_id=gateway_id, compute_resource_id=compute_resource_id, user_compute_resource_preference=user_compute_resource_preference),
            metadata=self._metadata,
        )

    def add_user_storage_preference(self, user_id, gateway_id, storage_resource_id, user_storage_preference):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.AddUserStoragePreference(
            pb2.AddUserStoragePreferenceRequest(user_id=user_id, gateway_id=gateway_id, storage_resource_id=storage_resource_id, user_storage_preference=user_storage_preference),
            metadata=self._metadata,
        )

    def get_user_compute_resource_preference(self, user_id, gateway_id, compute_resource_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.GetUserComputePreference(
            pb2.GetUserComputePreferenceRequest(user_id=user_id, gateway_id=gateway_id, compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def get_user_storage_preference(self, user_id, gateway_id, storage_resource_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.GetUserStoragePreference(
            pb2.GetUserStoragePreferenceRequest(user_id=user_id, gateway_id=gateway_id, storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    def get_all_user_compute_resource_preferences(self, user_id, gateway_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.GetAllUserComputePreferences(
            pb2.GetAllUserComputePreferencesRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_user_storage_preferences(self, user_id, gateway_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.GetAllUserStoragePreferences(
            pb2.GetAllUserStoragePreferencesRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_user_resource_profiles(self):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.GetAllUserResourceProfiles(
            pb2.GetAllUserResourceProfilesRequest(),
            metadata=self._metadata,
        )

    def update_user_compute_resource_preference(self, user_id, gateway_id, compute_resource_id, user_compute_resource_preference):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.UpdateUserComputePreference(
            pb2.UpdateUserComputePreferenceRequest(user_id=user_id, gateway_id=gateway_id, compute_resource_id=compute_resource_id, user_compute_resource_preference=user_compute_resource_preference),
            metadata=self._metadata,
        )

    def update_user_storage_preference(self, user_id, gateway_id, storage_resource_id, user_storage_preference):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.UpdateUserStoragePreference(
            pb2.UpdateUserStoragePreferenceRequest(user_id=user_id, gateway_id=gateway_id, storage_resource_id=storage_resource_id, user_storage_preference=user_storage_preference),
            metadata=self._metadata,
        )

    def delete_user_compute_resource_preference(self, user_id, gateway_id, compute_resource_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.DeleteUserComputePreference(
            pb2.DeleteUserComputePreferenceRequest(user_id=user_id, gateway_id=gateway_id, compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def delete_user_storage_preference(self, user_id, gateway_id, storage_resource_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.DeleteUserStoragePreference(
            pb2.DeleteUserStoragePreferenceRequest(user_id=user_id, gateway_id=gateway_id, storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    def get_latest_queue_statuses(self):
        pb2 = self._svc("user_resource_profile_service_pb2")
        return self._user_profile.GetLatestQueueStatuses(
            pb2.GetLatestQueueStatusesRequest(),
            metadata=self._metadata,
        )

    # ================================================================
    # Data Product Service
    # ================================================================

    def register_data_product(self, data_product):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.RegisterDataProduct(
            pb2.RegisterDataProductRequest(data_product=data_product),
            metadata=self._metadata,
        )

    def get_data_product(self, data_product_uri):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.GetDataProduct(
            pb2.GetDataProductRequest(data_product_uri=data_product_uri),
            metadata=self._metadata,
        )

    def register_replica_location(self, replica_location):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.RegisterReplicaLocation(
            pb2.RegisterReplicaLocationRequest(replica_location=replica_location),
            metadata=self._metadata,
        )

    def get_parent_data_product(self, data_product_uri):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.GetParentDataProduct(
            pb2.GetParentDataProductRequest(data_product_uri=data_product_uri),
            metadata=self._metadata,
        )

    def get_child_data_products(self, data_product_uri):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.GetChildDataProducts(
            pb2.GetChildDataProductsRequest(data_product_uri=data_product_uri),
            metadata=self._metadata,
        )

    def update_data_product(self, product_uri, data_product):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.UpdateDataProduct(
            pb2.UpdateDataProductRequest(product_uri=product_uri, data_product=data_product),
            metadata=self._metadata,
        )

    def delete_data_product(self, product_uri):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.DeleteDataProduct(
            pb2.DeleteDataProductRequest(product_uri=product_uri),
            metadata=self._metadata,
        )

    def get_replica_location(self, replica_id):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.GetReplicaLocation(
            pb2.GetReplicaLocationRequest(replica_id=replica_id),
            metadata=self._metadata,
        )

    def update_replica_location(self, replica_id, replica_location):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.UpdateReplicaLocation(
            pb2.UpdateReplicaLocationRequest(replica_id=replica_id, replica_location=replica_location),
            metadata=self._metadata,
        )

    def delete_replica_location(self, replica_id):
        pb2 = self._svc("data_product_service_pb2")
        return self._data_product.DeleteReplicaLocation(
            pb2.DeleteReplicaLocationRequest(replica_id=replica_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Sharing Service
    # ================================================================

    def share_resource_with_users(self, resource_id, user_permissions):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.ShareResourceWithUsers(
            pb2.ShareResourceWithUsersRequest(resource_id=resource_id, user_permissions=user_permissions),
            metadata=self._metadata,
        )

    def share_resource_with_groups(self, resource_id, group_permissions):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.ShareResourceWithGroups(
            pb2.ShareResourceWithGroupsRequest(resource_id=resource_id, group_permissions=group_permissions),
            metadata=self._metadata,
        )

    def revoke_sharing_of_resource_from_users(self, resource_id, user_permissions):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.RevokeFromUsers(
            pb2.RevokeFromUsersRequest(resource_id=resource_id, user_permissions=user_permissions),
            metadata=self._metadata,
        )

    def revoke_sharing_of_resource_from_groups(self, resource_id, group_permissions):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.RevokeFromGroups(
            pb2.RevokeFromGroupsRequest(resource_id=resource_id, group_permissions=group_permissions),
            metadata=self._metadata,
        )

    def get_all_accessible_users(self, resource_id, permission_type):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.GetAllAccessibleUsers(
            pb2.GetAllAccessibleUsersRequest(resource_id=resource_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def get_all_accessible_groups(self, resource_id, permission_type):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.GetAllAccessibleGroups(
            pb2.GetAllAccessibleGroupsRequest(resource_id=resource_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def get_all_directly_accessible_users(self, resource_id, permission_type):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.GetAllDirectlyAccessibleUsers(
            pb2.GetAllDirectlyAccessibleUsersRequest(resource_id=resource_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def get_all_directly_accessible_groups(self, resource_id, permission_type):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.GetAllDirectlyAccessibleGroups(
            pb2.GetAllDirectlyAccessibleGroupsRequest(resource_id=resource_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def user_has_access(self, resource_id, user_id, permission_type):
        pb2 = self._svc("sharing_service_pb2")
        return self._sharing.UserHasAccess(
            pb2.UserHasAccessRequest(resource_id=resource_id, user_id=user_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    # ================================================================
    # Group Resource Profile Service
    # ================================================================

    def create_group_resource_profile(self, group_resource_profile):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.CreateGroupResourceProfile(
            pb2.CreateGroupResourceProfileRequest(group_resource_profile=group_resource_profile),
            metadata=self._metadata,
        )

    def update_group_resource_profile(self, group_resource_profile_id, group_resource_profile):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.UpdateGroupResourceProfile(
            pb2.UpdateGroupResourceProfileRequest(group_resource_profile_id=group_resource_profile_id, group_resource_profile=group_resource_profile),
            metadata=self._metadata,
        )

    def get_group_resource_profile(self, group_resource_profile_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGroupResourceProfile(
            pb2.GetGroupResourceProfileRequest(group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )

    def remove_group_resource_profile(self, group_resource_profile_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.RemoveGroupResourceProfile(
            pb2.RemoveGroupResourceProfileRequest(group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )

    def get_group_resource_list(self):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGroupResourceList(
            pb2.GetGroupResourceListRequest(),
            metadata=self._metadata,
        )

    def remove_group_compute_prefs(self, group_resource_profile_id, compute_resource_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.RemoveGroupComputePrefs(
            pb2.RemoveGroupComputePrefsRequest(group_resource_profile_id=group_resource_profile_id, compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def remove_group_compute_resource_policy(self, resource_policy_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.RemoveGroupComputeResourcePolicy(
            pb2.RemoveGroupComputeResourcePolicyRequest(resource_policy_id=resource_policy_id),
            metadata=self._metadata,
        )

    def remove_group_batch_queue_resource_policy(self, resource_policy_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.RemoveGroupBatchQueueResourcePolicy(
            pb2.RemoveGroupBatchQueueResourcePolicyRequest(resource_policy_id=resource_policy_id),
            metadata=self._metadata,
        )

    def get_group_compute_resource_preference(self, group_resource_profile_id, compute_resource_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGroupComputePreference(
            pb2.GetGroupComputePreferenceRequest(group_resource_profile_id=group_resource_profile_id, compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def get_group_compute_resource_policy(self, resource_policy_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGroupComputeResourcePolicy(
            pb2.GetGroupComputeResourcePolicyRequest(resource_policy_id=resource_policy_id),
            metadata=self._metadata,
        )

    def get_batch_queue_resource_policy(self, resource_policy_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetBatchQueueResourcePolicy(
            pb2.GetBatchQueueResourcePolicyRequest(resource_policy_id=resource_policy_id),
            metadata=self._metadata,
        )

    def get_group_compute_resource_pref_list(self, group_resource_profile_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGroupComputePrefList(
            pb2.GetGroupComputePrefListRequest(group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )

    def get_group_batch_queue_resource_policy_list(self, group_resource_profile_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGroupBatchQueuePolicyList(
            pb2.GetGroupBatchQueuePolicyListRequest(group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )

    def get_group_compute_resource_policy_list(self, group_resource_profile_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGroupComputeResourcePolicyList(
            pb2.GetGroupComputeResourcePolicyListRequest(group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )

    def get_gateway_groups(self):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGatewayGroups(
            pb2.GetGatewayGroupsRequest(),
            metadata=self._metadata,
        )

    # ================================================================
    # Parser Service
    # ================================================================

    def get_parser(self, parser_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.GetParser(
            pb2.GetParserRequest(parser_id=parser_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def save_parser(self, parser):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.SaveParser(
            pb2.SaveParserRequest(parser=parser),
            metadata=self._metadata,
        )

    def list_all_parsers(self, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.ListAllParsers(
            pb2.ListAllParsersRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def remove_parser(self, parser_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.RemoveParser(
            pb2.RemoveParserRequest(parser_id=parser_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_parsing_template(self, template_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.GetParsingTemplate(
            pb2.GetParsingTemplateRequest(template_id=template_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_parsing_templates_for_experiment(self, experiment_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.GetParsingTemplatesForExperiment(
            pb2.GetParsingTemplatesForExperimentRequest(experiment_id=experiment_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def save_parsing_template(self, parsing_template):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.SaveParsingTemplate(
            pb2.SaveParsingTemplateRequest(parsing_template=parsing_template),
            metadata=self._metadata,
        )

    def remove_parsing_template(self, template_id, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.RemoveParsingTemplate(
            pb2.RemoveParsingTemplateRequest(template_id=template_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def list_all_parsing_templates(self, gateway_id):
        pb2 = self._svc("parser_service_pb2")
        return self._parser.ListAllParsingTemplates(
            pb2.ListAllParsingTemplatesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
