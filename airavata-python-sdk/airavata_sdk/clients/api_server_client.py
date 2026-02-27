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
from typing import Any, Optional

from airavata_sdk import Settings
from airavata_sdk.transport.utils import RestClient

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class APIServerClient:
    """Client for the Airavata REST API.

    All methods communicate with the unified REST API at /api/v1/.
    Data is exchanged as plain Python dicts (JSON).
    """

    def __init__(self, access_token: str, base_url: Optional[str] = None):
        self.settings = Settings()
        self.client = RestClient(
            access_token=access_token,
            base_url=base_url or self.settings.API_SERVER_URL,
        )

    # ----------------------------------------------------------------
    # Health & Config
    # ----------------------------------------------------------------

    def health_check(self) -> dict:
        return self.client.get("/health")

    def get_config(self) -> dict:
        return self.client.get("/config")

    # ----------------------------------------------------------------
    # Gateways
    # ----------------------------------------------------------------

    def add_gateway(self, gateway: dict) -> str:
        return self.client.post("/gateways", json=gateway)

    def update_gateway(self, gateway_id: str, gateway: dict) -> dict:
        return self.client.put(f"/gateways/{gateway_id}", json=gateway)

    def get_gateway(self, gateway_id: str) -> dict:
        return self.client.get(f"/gateways/{gateway_id}")

    def delete_gateway(self, gateway_id: str) -> None:
        self.client.delete(f"/gateways/{gateway_id}")

    def get_all_gateways(self) -> list[dict]:
        return self.client.get("/gateways")

    def is_gateway_exist(self, gateway_id: str) -> bool:
        try:
            self.client.get(f"/gateways/{gateway_id}")
            return True
        except Exception:
            return False

    # ----------------------------------------------------------------
    # Users
    # ----------------------------------------------------------------

    def is_user_exists(self, user_id: str, gateway_id: str = None) -> bool:
        return self.client.get(f"/users/{user_id}/exists", params={"gatewayId": gateway_id})

    def get_all_users_in_gateway(self, gateway_id: str) -> list[dict]:
        return self.client.get("/users", params={"gatewayId": gateway_id})

    # ----------------------------------------------------------------
    # Notifications
    # ----------------------------------------------------------------

    def create_notification(self, notification: dict) -> str:
        return self.client.post("/notices", json=notification)

    def update_notification(self, notification_id: str, notification: dict) -> dict:
        return self.client.put(f"/notices/{notification_id}", json=notification)

    def delete_notification(self, notification_id: str) -> None:
        self.client.delete(f"/notices/{notification_id}")

    def get_notification(self, notification_id: str) -> dict:
        return self.client.get(f"/notices/{notification_id}")

    def get_all_notifications(self, gateway_id: str) -> list[dict]:
        return self.client.get("/notices", params={"gatewayId": gateway_id})

    # ----------------------------------------------------------------
    # Credentials
    # ----------------------------------------------------------------

    def generate_and_register_ssh_keys(self, credential: dict) -> str:
        return self.client.post("/credentials/ssh", json=credential)

    def register_pwd_credential(self, credential: dict) -> str:
        return self.client.post("/credentials/password", json=credential)

    def get_credential_summary(self, token: str, gateway_id: str = None) -> dict:
        return self.client.get(f"/credential-summaries/{token}", params={"gatewayId": gateway_id})

    def get_all_credential_summaries(self, gateway_id: str = None, credential_type: str = None) -> list[dict]:
        params = {}
        if gateway_id:
            params["gatewayId"] = gateway_id
        if credential_type:
            params["type"] = credential_type
        return self.client.get("/credential-summaries", params=params)

    def get_ssh_credential(self, token: str, gateway_id: str = None) -> dict:
        return self.client.get(f"/credentials/ssh/{token}", params={"gatewayId": gateway_id})

    def get_password_credential(self, token: str, gateway_id: str = None) -> dict:
        return self.client.get(f"/credentials/password/{token}", params={"gatewayId": gateway_id})

    def delete_ssh_pub_key(self, token: str, gateway_id: str = None) -> None:
        self.client.delete(f"/credentials/{token}", params={"gatewayId": gateway_id})

    def delete_pwd_credential(self, token: str, gateway_id: str = None) -> None:
        self.client.delete(f"/credentials/{token}", params={"gatewayId": gateway_id})

    # ----------------------------------------------------------------
    # Projects
    # ----------------------------------------------------------------

    def create_project(self, project: dict, gateway_id: str = None) -> str:
        return self.client.post("/projects", json=project, params={"gatewayId": gateway_id})

    def update_project(self, project_id: str, project: dict) -> dict:
        return self.client.put(f"/projects/{project_id}", json=project)

    def get_project(self, project_id: str) -> dict:
        return self.client.get(f"/projects/{project_id}")

    def delete_project(self, project_id: str) -> None:
        self.client.delete(f"/projects/{project_id}")

    def get_user_projects(self, gateway_id: str, user_name: str = None, limit: int = -1, offset: int = 0) -> list[dict]:
        params = {"gatewayId": gateway_id, "limit": limit, "offset": offset}
        return self.client.get("/projects", params=params)

    def search_projects(self, gateway_id: str, limit: int = -1, offset: int = 0, **filters) -> list[dict]:
        params = {"gatewayId": gateway_id, "limit": limit, "offset": offset, **filters}
        return self.client.get("/projects", params=params)

    # ----------------------------------------------------------------
    # Experiments
    # ----------------------------------------------------------------

    def create_experiment(self, experiment: dict, gateway_id: str = None) -> str:
        return self.client.post("/experiments", json=experiment)

    def delete_experiment(self, experiment_id: str) -> None:
        self.client.delete(f"/experiments/{experiment_id}")

    def get_experiment(self, experiment_id: str) -> dict:
        return self.client.get(f"/experiments/{experiment_id}")

    def get_experiment_by_admin(self, experiment_id: str) -> dict:
        return self.client.get(f"/experiments/{experiment_id}")

    def get_detailed_experiment_tree(self, experiment_id: str) -> dict:
        return self.client.get(f"/experiments/{experiment_id}")

    def update_experiment(self, experiment_id: str, experiment: dict) -> dict:
        return self.client.put(f"/experiments/{experiment_id}", json=experiment)

    def update_experiment_configuration(self, experiment_id: str, config: dict) -> dict:
        return self.client.put(f"/experiments/{experiment_id}", json=config)

    def validate_experiment(self, experiment_id: str) -> bool:
        return True

    def launch_experiment(self, experiment_id: str, gateway_id: str = None) -> None:
        self.client.post(f"/experiments/{experiment_id}/launch")

    def get_experiment_status(self, experiment_id: str) -> dict:
        experiment = self.client.get(f"/experiments/{experiment_id}")
        return experiment.get("experimentStatus")

    def get_experiment_outputs(self, experiment_id: str) -> list[dict]:
        experiment = self.client.get(f"/experiments/{experiment_id}")
        return experiment.get("experimentOutputs", [])

    def get_intermediate_outputs(self, experiment_id: str) -> list[dict]:
        experiment = self.client.get(f"/experiments/{experiment_id}")
        return experiment.get("experimentOutputs", [])

    def get_job_statuses(self, experiment_id: str) -> dict:
        experiment = self.client.get(f"/experiments/{experiment_id}")
        result = {}
        for process in (experiment.get("processes") or []):
            for task in (process.get("tasks") or []):
                for job in (task.get("jobs") or []):
                    job_id = job.get("jobId", "unknown")
                    statuses = job.get("jobStatuses", [])
                    if statuses:
                        result[job_id] = statuses[-1]
        return result

    def get_job_details(self, experiment_id: str) -> list[dict]:
        experiment = self.client.get(f"/experiments/{experiment_id}")
        jobs = []
        for process in (experiment.get("processes") or []):
            for task in (process.get("tasks") or []):
                jobs.extend(task.get("jobs") or [])
        return jobs

    def clone_experiment(self, experiment_id: str, new_name: str = None, project_id: str = None) -> str:
        params = {}
        if new_name:
            params["newName"] = new_name
        if project_id:
            params["projectId"] = project_id
        return self.client.post(f"/experiments/{experiment_id}/clone", params=params)

    def clone_experiment_by_admin(self, experiment_id: str, new_name: str = None, project_id: str = None) -> str:
        return self.clone_experiment(experiment_id, new_name, project_id)

    def terminate_experiment(self, experiment_id: str, gateway_id: str = None) -> None:
        self.client.post(f"/experiments/{experiment_id}/cancel")

    def search_experiments(self, gateway_id: str, user_name: str = None, limit: int = -1, offset: int = 0, **filters) -> list[dict]:
        params = {"gatewayId": gateway_id, "limit": limit, "offset": offset}
        if user_name:
            params["userName"] = user_name
        params.update(filters)
        return self.client.get("/experiments", params=params)

    def get_experiment_statistics(self, gateway_id: str, **params) -> dict:
        params["gatewayId"] = gateway_id
        return self.client.get("/statistics/experiments", params=params)

    def get_experiments_in_project(self, project_id: str, limit: int = -1, offset: int = 0) -> list[dict]:
        return self.client.get("/experiments", params={"projectId": project_id, "limit": limit, "offset": offset})

    def get_user_experiments(self, gateway_id: str, user_name: str, limit: int = -1, offset: int = 0) -> list[dict]:
        return self.client.get("/experiments", params={"gatewayId": gateway_id, "userName": user_name, "limit": limit, "offset": offset})

    # ----------------------------------------------------------------
    # Application Modules
    # ----------------------------------------------------------------

    def register_application_module(self, module: dict, gateway_id: str = None) -> str:
        return self.client.post("/application-modules", json=module, params={"gatewayId": gateway_id})

    def get_application_module(self, module_id: str) -> dict:
        return self.client.get(f"/application-modules/{module_id}")

    def update_application_module(self, module_id: str, module: dict) -> dict:
        return self.client.put(f"/application-modules/{module_id}", json=module)

    def get_all_app_modules(self, gateway_id: str) -> list[dict]:
        return self.client.get("/application-modules", params={"gatewayId": gateway_id})

    def get_accessible_app_modules(self, gateway_id: str) -> list[dict]:
        return self.get_all_app_modules(gateway_id)

    def delete_application_module(self, module_id: str) -> None:
        self.client.delete(f"/application-modules/{module_id}")

    # ----------------------------------------------------------------
    # Application Interfaces
    # ----------------------------------------------------------------

    def register_application_interface(self, interface: dict, gateway_id: str = None) -> str:
        return self.client.post("/application-interfaces", json=interface, params={"gatewayId": gateway_id})

    def clone_application_interface(self, interface_id: str) -> str:
        iface = self.get_application_interface(interface_id)
        iface.pop("applicationInterfaceId", None)
        return self.register_application_interface(iface)

    def get_application_interface(self, interface_id: str) -> dict:
        return self.client.get(f"/application-interfaces/{interface_id}")

    def update_application_interface(self, interface_id: str, interface: dict) -> dict:
        return self.client.put(f"/application-interfaces/{interface_id}", json=interface)

    def delete_application_interface(self, interface_id: str) -> None:
        self.client.delete(f"/application-interfaces/{interface_id}")

    def get_all_application_interface_names(self, gateway_id: str) -> dict:
        interfaces = self.get_all_application_interfaces(gateway_id)
        return {i["applicationInterfaceId"]: i["applicationName"] for i in interfaces}

    def get_all_application_interfaces(self, gateway_id: str) -> list[dict]:
        return self.client.get("/application-interfaces", params={"gatewayId": gateway_id})

    def get_application_inputs(self, interface_id: str) -> list[dict]:
        return self.client.get(f"/application-interfaces/{interface_id}/inputs")

    def get_application_outputs(self, interface_id: str) -> list[dict]:
        return self.client.get(f"/application-interfaces/{interface_id}/outputs")

    def get_available_app_interface_compute_resources(self, interface_id: str) -> dict:
        iface = self.client.get(f"/application-interfaces/{interface_id}")
        return iface.get("availableComputeResources", {})

    # ----------------------------------------------------------------
    # Application Deployments
    # ----------------------------------------------------------------

    def register_application_deployment(self, deployment: dict, gateway_id: str = None) -> str:
        return self.client.post("/application-deployments", json=deployment, params={"gatewayId": gateway_id})

    def get_application_deployment(self, deployment_id: str) -> dict:
        return self.client.get(f"/application-deployments/{deployment_id}")

    def update_application_deployment(self, deployment_id: str, deployment: dict) -> dict:
        return self.client.put(f"/application-deployments/{deployment_id}", json=deployment)

    def delete_application_deployment(self, deployment_id: str) -> None:
        self.client.delete(f"/application-deployments/{deployment_id}")

    def get_all_application_deployments(self, gateway_id: str) -> list[dict]:
        return self.client.get("/application-deployments", params={"gatewayId": gateway_id})

    def get_accessible_application_deployments(self, gateway_id: str) -> list[dict]:
        return self.get_all_application_deployments(gateway_id)

    def get_app_module_deployed_resources(self, module_id: str) -> list[dict]:
        return self.client.get("/application-deployments", params={"appModuleId": module_id})

    def get_application_deployments_for_app_module_and_group_resource_profile(self, module_id: str, group_profile_id: str) -> list[dict]:
        return self.client.get("/application-deployments", params={"appModuleId": module_id, "groupResourceProfileId": group_profile_id})

    # ----------------------------------------------------------------
    # Compute Resources
    # ----------------------------------------------------------------

    def register_compute_resource(self, resource: dict) -> str:
        return self.client.post("/compute-resources", json=resource)

    def get_compute_resource(self, resource_id: str) -> dict:
        return self.client.get(f"/compute-resources/{resource_id}")

    def get_all_compute_resource_names(self) -> dict:
        resources = self.client.get("/compute-resources")
        return {r["computeResourceId"]: r["hostName"] for r in resources}

    def update_compute_resource(self, resource_id: str, resource: dict) -> dict:
        return self.client.put(f"/compute-resources/{resource_id}", json=resource)

    def delete_compute_resource(self, resource_id: str) -> None:
        self.client.delete(f"/compute-resources/{resource_id}")

    # ----------------------------------------------------------------
    # Storage Resources
    # ----------------------------------------------------------------

    def register_storage_resource(self, resource: dict) -> str:
        return self.client.post("/storage-resources", json=resource)

    def get_storage_resource(self, resource_id: str) -> dict:
        return self.client.get(f"/storage-resources/{resource_id}")

    def get_all_storage_resource_names(self) -> dict:
        resources = self.client.get("/storage-resources")
        return {r["storageResourceId"]: r["hostName"] for r in resources}

    def update_storage_resource(self, resource_id: str, resource: dict) -> dict:
        return self.client.put(f"/storage-resources/{resource_id}", json=resource)

    def delete_storage_resource(self, resource_id: str) -> None:
        self.client.delete(f"/storage-resources/{resource_id}")

    # ----------------------------------------------------------------
    # Artifacts
    # ----------------------------------------------------------------

    def register_artifact(self, artifact: dict) -> str:
        return self.client.post("/artifacts", json=artifact)

    def get_artifact(self, artifact_uri: str) -> dict:
        return self.client.get(f"/artifacts/{artifact_uri}")

    def register_replica_location(self, artifact_uri: str, replica: dict) -> str:
        artifact = self.get_artifact(artifact_uri)
        replicas = artifact.get("replicaLocations", [])
        replicas.append(replica)
        artifact["replicaLocations"] = replicas
        self.client.put(f"/artifacts/{artifact_uri}", json=artifact)
        return artifact_uri

    def get_parent_artifact(self, artifact_uri: str) -> dict:
        return self.client.get(f"/artifacts/{artifact_uri}/parent")

    def get_child_artifacts(self, artifact_uri: str) -> list[dict]:
        return self.client.get(f"/artifacts/{artifact_uri}/children")

    # ----------------------------------------------------------------
    # Sharing
    # ----------------------------------------------------------------

    def share_resource_with_users(self, resource_id: str, users: list[str], permission: str = "READ") -> None:
        for user in users:
            self.client.post("/resource-access", json={
                "resourceId": resource_id,
                "userId": user,
                "permission": permission,
            })

    def share_resource_with_groups(self, resource_id: str, groups: list[str], permission: str = "READ") -> None:
        for group in groups:
            self.client.post("/resource-access", json={
                "resourceId": resource_id,
                "groupId": group,
                "permission": permission,
            })

    def revoke_sharing_of_resource_from_users(self, resource_id: str, users: list[str]) -> None:
        grants = self.client.get("/resource-access", params={"resourceId": resource_id})
        for grant in (grants or []):
            if grant.get("userId") in users:
                self.client.delete(f"/resource-access/{grant['id']}")

    def revoke_sharing_of_resource_from_groups(self, resource_id: str, groups: list[str]) -> None:
        grants = self.client.get("/resource-access", params={"resourceId": resource_id})
        for grant in (grants or []):
            if grant.get("groupId") in groups:
                self.client.delete(f"/resource-access/{grant['id']}")

    def get_all_accessible_users(self, resource_id: str) -> list[dict]:
        return self.client.get("/resource-access", params={"resourceId": resource_id})

    def get_all_accessible_groups(self, resource_id: str) -> list[dict]:
        return self.client.get("/resource-access", params={"resourceId": resource_id})

    def get_all_directly_accessible_users(self, resource_id: str) -> list[dict]:
        return self.get_all_accessible_users(resource_id)

    def get_all_directly_accessible_groups(self, resource_id: str) -> list[dict]:
        return self.get_all_accessible_groups(resource_id)

    def user_has_access(self, resource_id: str, user_id: str, permission: str = "READ") -> bool:
        grants = self.client.get("/resource-access", params={"resourceId": resource_id})
        return any(g.get("userId") == user_id for g in (grants or []))

    # ----------------------------------------------------------------
    # Group Resource Profiles
    # ----------------------------------------------------------------

    def create_group_resource_profile(self, profile: dict) -> str:
        return self.client.post("/group-resource-profiles", json=profile)

    def update_group_resource_profile(self, profile_id: str, profile: dict) -> dict:
        return self.client.put(f"/group-resource-profiles/{profile_id}", json=profile)

    def get_group_resource_profile(self, profile_id: str) -> dict:
        return self.client.get(f"/group-resource-profiles/{profile_id}")

    def remove_group_resource_profile(self, profile_id: str) -> None:
        self.client.delete(f"/group-resource-profiles/{profile_id}")

    def get_group_resource_list(self, gateway_id: str) -> list[dict]:
        return self.client.get("/group-resource-profiles", params={"gatewayId": gateway_id})

    def get_group_compute_resource_pref_list(self, profile_id: str) -> list[dict]:
        return self.client.get(f"/group-resource-profiles/{profile_id}/compute-preferences")

    def get_group_compute_resource_policy_list(self, profile_id: str) -> list[dict]:
        return self.client.get(f"/group-resource-profiles/{profile_id}/compute-policies")

    def get_group_batch_queue_resource_policy_list(self, profile_id: str) -> list[dict]:
        return self.client.get(f"/group-resource-profiles/{profile_id}/batch-queue-policies")

    def get_group_compute_resource_preference(self, profile_id: str, resource_id: str) -> dict:
        prefs = self.get_group_compute_resource_pref_list(profile_id)
        return next((p for p in prefs if p.get("computeResourceId") == resource_id), {})

    def get_group_compute_resource_policy(self, profile_id: str, resource_id: str) -> dict:
        policies = self.get_group_compute_resource_policy_list(profile_id)
        return next((p for p in policies if p.get("computeResourceId") == resource_id), {})

    def get_batch_queue_resource_policy(self, profile_id: str, resource_id: str) -> dict:
        policies = self.get_group_batch_queue_resource_policy_list(profile_id)
        return next((p for p in policies if p.get("computeResourceId") == resource_id), {})

    def remove_group_compute_prefs(self, profile_id: str, resource_id: str) -> None:
        profile = self.get_group_resource_profile(profile_id)
        prefs = profile.get("computePreferences", [])
        profile["computePreferences"] = [p for p in prefs if p.get("computeResourceId") != resource_id]
        self.update_group_resource_profile(profile_id, profile)

    def remove_group_compute_resource_policy(self, profile_id: str, resource_id: str) -> None:
        profile = self.get_group_resource_profile(profile_id)
        policies = profile.get("computeResourcePolicies", [])
        profile["computeResourcePolicies"] = [p for p in policies if p.get("computeResourceId") != resource_id]
        self.update_group_resource_profile(profile_id, profile)

    def remove_group_batch_queue_resource_policy(self, profile_id: str, resource_id: str) -> None:
        profile = self.get_group_resource_profile(profile_id)
        policies = profile.get("batchQueueResourcePolicies", [])
        profile["batchQueueResourcePolicies"] = [p for p in policies if p.get("computeResourceId") != resource_id]
        self.update_group_resource_profile(profile_id, profile)

    # ----------------------------------------------------------------
    # Gateway Resource Profiles
    # ----------------------------------------------------------------

    def register_gateway_resource_profile(self, profile: dict) -> str:
        return self.client.post("/gateway-resource-profile", json=profile)

    def get_gateway_resource_profile(self, gateway_id: str) -> dict:
        return self.client.get(f"/gateway-resource-profile/{gateway_id}")

    def update_gateway_resource_profile(self, gateway_id: str, profile: dict) -> dict:
        return self.client.put(f"/gateway-resource-profile/{gateway_id}", json=profile)

    def delete_gateway_resource_profile(self, gateway_id: str) -> None:
        self.client.delete(f"/gateway-resource-profile/{gateway_id}")

    def add_gateway_compute_resource_preference(self, gateway_id: str, resource_id: str, preference: dict) -> None:
        profile = self.get_gateway_resource_profile(gateway_id)
        prefs = profile.get("computeResourcePreferences", [])
        preference["computeResourceId"] = resource_id
        prefs.append(preference)
        profile["computeResourcePreferences"] = prefs
        self.update_gateway_resource_profile(gateway_id, profile)

    def add_gateway_storage_preference(self, gateway_id: str, storage_id: str, preference: dict) -> None:
        profile = self.get_gateway_resource_profile(gateway_id)
        prefs = profile.get("storagePreferences", [])
        preference["storageResourceId"] = storage_id
        prefs.append(preference)
        profile["storagePreferences"] = prefs
        self.update_gateway_resource_profile(gateway_id, profile)

    def get_gateway_compute_resource_preference(self, gateway_id: str, resource_id: str) -> dict:
        return self.client.get(f"/gateway-resource-profile/{gateway_id}/compute-preferences/{resource_id}")

    def get_gateway_storage_preference(self, gateway_id: str, storage_id: str) -> dict:
        return self.client.get(f"/gateway-resource-profile/{gateway_id}/storage-preferences/{storage_id}")

    def get_all_gateway_compute_resource_preferences(self, gateway_id: str) -> list[dict]:
        return self.client.get(f"/gateway-resource-profile/{gateway_id}/compute-preferences")

    def get_all_gateway_storage_preferences(self, gateway_id: str) -> list[dict]:
        return self.client.get(f"/gateway-resource-profile/{gateway_id}/storage-preferences")

    def get_all_gateway_resource_profiles(self) -> list[dict]:
        return self.client.get("/gateway-resource-profile")

    def update_gateway_compute_resource_preference(self, gateway_id: str, resource_id: str, preference: dict) -> None:
        self.add_gateway_compute_resource_preference(gateway_id, resource_id, preference)

    def update_gateway_storage_preference(self, gateway_id: str, storage_id: str, preference: dict) -> None:
        self.add_gateway_storage_preference(gateway_id, storage_id, preference)

    def delete_gateway_compute_resource_preference(self, gateway_id: str, resource_id: str) -> None:
        self.client.delete(f"/gateway-resource-profile/{gateway_id}/compute-preferences/{resource_id}")

    def delete_gateway_storage_preference(self, gateway_id: str, storage_id: str) -> None:
        self.client.delete(f"/gateway-resource-profile/{gateway_id}/storage-preferences/{storage_id}")

    # ----------------------------------------------------------------
    # User Resource Profiles
    # ----------------------------------------------------------------

    def register_user_resource_profile(self, profile: dict) -> str:
        return self.client.post("/user-resource-profiles", json=profile)

    def is_user_resource_profile_exists(self, user_id: str, gateway_id: str = None) -> bool:
        try:
            self.get_user_resource_profile(user_id, gateway_id)
            return True
        except Exception:
            return False

    def get_user_resource_profile(self, user_id: str, gateway_id: str = None) -> dict:
        return self.client.get(f"/user-resource-profiles/{user_id}", params={"gatewayId": gateway_id})

    def update_user_resource_profile(self, user_id: str, profile: dict, gateway_id: str = None) -> dict:
        return self.client.put(f"/user-resource-profiles/{user_id}", json=profile, params={"gatewayId": gateway_id})

    def delete_user_resource_profile(self, user_id: str, gateway_id: str = None) -> None:
        self.client.delete(f"/user-resource-profiles/{user_id}", params={"gatewayId": gateway_id})

    def add_user_compute_resource_preference(self, user_id: str, resource_id: str, preference: dict) -> None:
        profile = self.get_user_resource_profile(user_id)
        prefs = profile.get("computeResourcePreferences", [])
        preference["computeResourceId"] = resource_id
        prefs.append(preference)
        profile["computeResourcePreferences"] = prefs
        self.update_user_resource_profile(user_id, profile)

    def add_user_storage_preference(self, user_id: str, storage_id: str, preference: dict) -> None:
        profile = self.get_user_resource_profile(user_id)
        prefs = profile.get("storagePreferences", [])
        preference["storageResourceId"] = storage_id
        prefs.append(preference)
        profile["storagePreferences"] = prefs
        self.update_user_resource_profile(user_id, profile)

    def get_user_compute_resource_preference(self, user_id: str, resource_id: str, gateway_id: str = None) -> dict:
        prefs = self.client.get(f"/user-resource-profiles/{user_id}/compute-preferences", params={"gatewayId": gateway_id})
        return next((p for p in prefs if p.get("computeResourceId") == resource_id), {})

    def get_user_storage_preference(self, user_id: str, storage_id: str, gateway_id: str = None) -> dict:
        prefs = self.client.get(f"/user-resource-profiles/{user_id}/storage-preferences", params={"gatewayId": gateway_id})
        return next((p for p in prefs if p.get("storageResourceId") == storage_id), {})

    def get_all_user_compute_resource_preferences(self, user_id: str, gateway_id: str = None) -> list[dict]:
        return self.client.get(f"/user-resource-profiles/{user_id}/compute-preferences", params={"gatewayId": gateway_id})

    def get_all_user_storage_preferences(self, user_id: str, gateway_id: str = None) -> list[dict]:
        return self.client.get(f"/user-resource-profiles/{user_id}/storage-preferences", params={"gatewayId": gateway_id})

    def get_all_user_resource_profiles(self) -> list[dict]:
        return self.client.get("/user-resource-profiles")

    def update_user_compute_resource_preference(self, user_id: str, resource_id: str, preference: dict) -> None:
        self.add_user_compute_resource_preference(user_id, resource_id, preference)

    def update_user_storage_preference(self, user_id: str, storage_id: str, preference: dict) -> None:
        self.add_user_storage_preference(user_id, storage_id, preference)

    def delete_user_compute_resource_preference(self, user_id: str, resource_id: str, gateway_id: str = None) -> None:
        profile = self.get_user_resource_profile(user_id, gateway_id)
        prefs = profile.get("computeResourcePreferences", [])
        profile["computeResourcePreferences"] = [p for p in prefs if p.get("computeResourceId") != resource_id]
        self.update_user_resource_profile(user_id, profile, gateway_id)

    def delete_user_storage_preference(self, user_id: str, storage_id: str, gateway_id: str = None) -> None:
        profile = self.get_user_resource_profile(user_id, gateway_id)
        prefs = profile.get("storagePreferences", [])
        profile["storagePreferences"] = [p for p in prefs if p.get("storageResourceId") != storage_id]
        self.update_user_resource_profile(user_id, profile, gateway_id)

    # ----------------------------------------------------------------
    # Parsing Templates
    # ----------------------------------------------------------------

    def get_parser(self, parser_id: str) -> dict:
        return self.client.get(f"/parsing-templates/{parser_id}")

    def save_parser(self, parser: dict, gateway_id: str = None) -> str:
        return self.client.post("/parsing-templates", json=parser, params={"gatewayId": gateway_id})

    def list_all_parsers(self, gateway_id: str) -> list[dict]:
        return self.client.get("/parsing-templates", params={"gatewayId": gateway_id})

    def remove_parser(self, parser_id: str) -> None:
        self.client.delete(f"/parsing-templates/{parser_id}")

    def get_parsing_template(self, template_id: str) -> dict:
        return self.get_parser(template_id)

    def get_parsing_templates_for_experiment(self, experiment_id: str) -> list[dict]:
        return self.client.get("/parsing-templates", params={"experimentId": experiment_id})

    def save_parsing_template(self, template: dict, gateway_id: str = None) -> str:
        return self.save_parser(template, gateway_id)

    def remove_parsing_template(self, template_id: str) -> None:
        self.remove_parser(template_id)

    def list_all_parsing_templates(self, gateway_id: str) -> list[dict]:
        return self.list_all_parsers(gateway_id)

    # ----------------------------------------------------------------
    # Gateway Groups
    # ----------------------------------------------------------------

    def get_gateway_groups(self, gateway_id: str = None) -> list[dict]:
        return self.client.get("/groups", params={"gatewayId": gateway_id})

    # ----------------------------------------------------------------
    # Processes
    # ----------------------------------------------------------------

    def get_process(self, process_id: str) -> dict:
        return self.client.get(f"/processes/{process_id}")

    def get_processes_for_experiment(self, experiment_id: str) -> list[dict]:
        return self.client.get("/processes", params={"experimentId": experiment_id})

    # ----------------------------------------------------------------
    # Jobs
    # ----------------------------------------------------------------

    def get_job(self, job_id: str, task_id: str = None) -> dict:
        return self.client.get(f"/jobs/{job_id}", params={"taskId": task_id})

    # ----------------------------------------------------------------
    # Workflows
    # ----------------------------------------------------------------

    def get_workflow(self, workflow_id: str) -> dict:
        return self.client.get(f"/workflows/{workflow_id}")

    def register_workflow(self, workflow: dict, experiment_id: str = None) -> str:
        return self.client.post("/workflows", json=workflow, params={"experimentId": experiment_id})

    def update_workflow(self, workflow_id: str, workflow: dict) -> dict:
        return self.client.put(f"/workflows/{workflow_id}", json=workflow)

    def delete_workflow(self, workflow_id: str) -> None:
        self.client.delete(f"/workflows/{workflow_id}")

    def close(self):
        self.client.close()
