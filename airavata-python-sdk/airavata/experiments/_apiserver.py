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

"""airavata.experiments' own thin gRPC client over the raw generated stubs.

The Airavata Python SDK ships only the protoc-generated stubs
(``airavata``) and the Bearer-auth channel helper
(``airavata.auth``); the hand-written ``airavata.clients`` facade is gone.
This module is the small replacement that lives with its sole consumer: it
builds the seven service stubs ``AiravataOperator`` needs from a single
``authenticated_channel`` (whose interceptor injects ``authorization: Bearer
<token>`` on every call, so no per-call metadata threading) and exposes exactly
the request-building wrappers the operator calls. Method names, signatures, and
the returned protobuf responses match the old facade, so the operator's call
sites are unchanged.
"""

from airavata import Settings
from airavata.auth import authenticated_channel
from airavata.services import (
    application_catalog_service_pb2 as appcat_pb2,
    application_catalog_service_pb2_grpc as appcat_grpc,
    data_product_service_pb2 as dp_pb2,
    data_product_service_pb2_grpc as dp_grpc,
    experiment_service_pb2 as exp_pb2,
    experiment_service_pb2_grpc as exp_grpc,
    experiment_set_service_pb2 as set_pb2,
    experiment_set_service_pb2_grpc as set_grpc,
    gateway_resource_profile_service_pb2 as gwp_pb2,
    gateway_resource_profile_service_pb2_grpc as gwp_grpc,
    group_resource_profile_service_pb2 as grp_pb2,
    group_resource_profile_service_pb2_grpc as grp_grpc,
    project_service_pb2 as proj_pb2,
    project_service_pb2_grpc as proj_grpc,
    resource_service_pb2 as res_pb2,
    resource_service_pb2_grpc as res_grpc,
)


class APIServerClient:
    """The subset of Airavata gRPC services ``AiravataOperator`` uses, over an
    ``authenticated_channel``. Each method builds the request proto and returns
    the full response proto from the stub (callers extract the fields)."""

    def __init__(self, access_token: str):
        s = Settings()
        self.channel = authenticated_channel(
            s.API_SERVER_HOSTNAME,
            s.API_SERVER_PORT,
            access_token,
            secure=s.API_SERVER_SECURE,
        )
        self._experiment = exp_grpc.ExperimentServiceStub(self.channel)
        self._experiment_set = set_grpc.ExperimentSetServiceStub(self.channel)
        self._app_catalog = appcat_grpc.ApplicationCatalogServiceStub(self.channel)
        self._resource = res_grpc.ResourceServiceStub(self.channel)
        self._gw_profile = gwp_grpc.GatewayResourceProfileServiceStub(self.channel)
        self._grp_profile = grp_grpc.GroupResourceProfileServiceStub(self.channel)
        self._project = proj_grpc.ProjectServiceStub(self.channel)
        self._data_product = dp_grpc.DataProductServiceStub(self.channel)

    def close(self):
        self.channel.close()

    # ---------------------------------------------------------------- Experiment
    def create_experiment(self, gateway_id, experiment):
        return self._experiment.CreateExperiment(
            exp_pb2.CreateExperimentRequest(gateway_id=gateway_id, experiment=experiment)
        )

    def get_experiment(self, experiment_id):
        return self._experiment.GetExperiment(
            exp_pb2.GetExperimentRequest(experiment_id=experiment_id)
        )

    def get_detailed_experiment_tree(self, experiment_id):
        return self._experiment.GetDetailedExperimentTree(
            exp_pb2.GetDetailedExperimentTreeRequest(experiment_id=experiment_id)
        )

    def get_experiment_status(self, experiment_id):
        return self._experiment.GetExperimentStatus(
            exp_pb2.GetExperimentStatusRequest(experiment_id=experiment_id)
        )

    def get_job_statuses(self, experiment_id):
        return self._experiment.GetJobStatuses(
            exp_pb2.GetJobStatusesRequest(experiment_id=experiment_id)
        )

    def launch_experiment(self, experiment_id, gateway_id):
        return self._experiment.LaunchExperiment(
            exp_pb2.LaunchExperimentRequest(experiment_id=experiment_id, gateway_id=gateway_id)
        )

    def terminate_experiment(self, experiment_id, gateway_id):
        return self._experiment.TerminateExperiment(
            exp_pb2.TerminateExperimentRequest(experiment_id=experiment_id, gateway_id=gateway_id)
        )

    # ------------------------------------------------------- Application Catalog
    def get_all_application_interfaces(self, gateway_id):
        return self._app_catalog.GetAllApplicationInterfaces(
            appcat_pb2.GetAllApplicationInterfacesRequest(gateway_id=gateway_id)
        )

    def get_application_inputs(self, app_interface_id):
        return self._app_catalog.GetApplicationInputs(
            appcat_pb2.GetApplicationInputsRequest(app_interface_id=app_interface_id)
        )

    def get_application_outputs(self, app_interface_id):
        return self._app_catalog.GetApplicationOutputs(
            appcat_pb2.GetApplicationOutputsRequest(app_interface_id=app_interface_id)
        )

    def get_application_deployments_for_app_module_and_group_resource_profile(
        self, app_module_id, group_resource_profile_id
    ):
        return self._app_catalog.GetDeploymentsForModuleAndProfile(
            appcat_pb2.GetDeploymentsForModuleAndProfileRequest(
                app_module_id=app_module_id,
                group_resource_profile_id=group_resource_profile_id,
            )
        )

    # -------------------------------------------------------------------- Resource
    def get_compute_resource(self, compute_resource_id):
        return self._resource.GetComputeResource(
            res_pb2.GetComputeResourceRequest(compute_resource_id=compute_resource_id)
        )

    def get_all_compute_resource_names(self):
        return self._resource.GetAllComputeResourceNames(
            res_pb2.GetAllComputeResourceNamesRequest()
        )

    def get_storage_resource(self, storage_resource_id):
        return self._resource.GetStorageResource(
            res_pb2.GetStorageResourceRequest(storage_resource_id=storage_resource_id)
        )

    def get_all_storage_resource_names(self):
        return self._resource.GetAllStorageResourceNames(
            res_pb2.GetAllStorageResourceNamesRequest()
        )

    # ----------------------------------------------- Gateway Resource Profile
    def get_gateway_storage_preference(self, gateway_id, storage_resource_id):
        return self._gw_profile.GetStoragePreference(
            gwp_pb2.GetStoragePreferenceRequest(
                gateway_id=gateway_id, storage_resource_id=storage_resource_id
            )
        )

    # ------------------------------------------------- Group Resource Profile
    def get_group_resource_profile(self, group_resource_profile_id):
        return self._grp_profile.GetGroupResourceProfile(
            grp_pb2.GetGroupResourceProfileRequest(
                group_resource_profile_id=group_resource_profile_id
            )
        )

    def get_group_resource_list(self):
        return self._grp_profile.GetGroupResourceList(
            grp_pb2.GetGroupResourceListRequest()
        )

    # --------------------------------------------------------------------- Project
    def get_user_projects(self, gateway_id, user_name, limit=-1, offset=0):
        return self._project.GetUserProjects(
            proj_pb2.GetUserProjectsRequest(
                gateway_id=gateway_id, user_name=user_name, limit=limit, offset=offset
            )
        )

    # --------------------------------------------------------------- Data Product
    def register_data_product(self, data_product):
        return self._data_product.RegisterDataProduct(
            dp_pb2.RegisterDataProductRequest(data_product=data_product)
        )

    # --------------------------------------------------------- Experiment Set
    def create_experiment_set(self, request):
        """Pass a prebuilt ``CreateExperimentSetRequest`` through to the stub."""
        return self._experiment_set.CreateExperimentSet(request)

    def launch_experiment_set(self, set_id, notification_email=""):
        return self._experiment_set.LaunchExperimentSet(
            set_pb2.LaunchExperimentSetRequest(
                experiment_set_id=set_id,
                notification_email=notification_email,
            )
        )

    def get_experiment_set(self, set_id):
        return self._experiment_set.GetExperimentSet(
            set_pb2.GetExperimentSetRequest(experiment_set_id=set_id)
        )

    def get_experiment_set_status(self, set_id):
        return self._experiment_set.GetExperimentSetStatus(
            set_pb2.GetExperimentSetRequest(experiment_set_id=set_id)
        )

    def list_experiment_sets(self, limit=0, offset=0):
        return self._experiment_set.ListExperimentSets(
            set_pb2.ListExperimentSetsRequest(limit=limit, offset=offset)
        )
