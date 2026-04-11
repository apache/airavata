import importlib

from airavata_sdk.transport.utils import (
    create_resource_service_stub,
    create_gateway_resource_profile_service_stub,
    create_group_resource_profile_service_stub,
    create_user_resource_profile_service_stub,
)


class ComputeClient:
    """Compute resource catalog, job submissions, gateway/group/user resource profiles."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._resource = create_resource_service_stub(channel)
        self._gw_profile = create_gateway_resource_profile_service_stub(channel)
        self._grp_profile = create_group_resource_profile_service_stub(channel)
        self._user_profile = create_user_resource_profile_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    # ================================================================
    # Resource Service -- compute resources
    # ================================================================

    def register_compute_resource(self, compute_resource):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.RegisterComputeResource(
            pb2.RegisterComputeResourceRequest(compute_resource=compute_resource),
            metadata=self._metadata,
        )
        return response.compute_resource_id

    def get_compute_resource(self, compute_resource_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetComputeResource(
            pb2.GetComputeResourceRequest(compute_resource_id=compute_resource_id),
            metadata=self._metadata,
        )

    def get_all_compute_resource_names(self):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.GetAllComputeResourceNames(
            pb2.GetAllComputeResourceNamesRequest(),
            metadata=self._metadata,
        )
        return dict(response.compute_resource_names)

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

    # --- Job Submission ---

    def add_local_submission_details(self, compute_resource_id, priority, local_submission):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.AddLocalSubmission(
            pb2.AddLocalSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, local_submission=local_submission),
            metadata=self._metadata,
        )
        return response.submission_id

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
        response = self._resource.AddSSHJobSubmission(
            pb2.AddSSHJobSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, ssh_job_submission=ssh_job_submission),
            metadata=self._metadata,
        )
        return response.submission_id

    def add_ssh_fork_job_submission_details(self, compute_resource_id, priority, ssh_job_submission):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.AddSSHForkJobSubmission(
            pb2.AddSSHForkJobSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, ssh_job_submission=ssh_job_submission),
            metadata=self._metadata,
        )
        return response.submission_id

    def get_ssh_job_submission(self, submission_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetSSHJobSubmission(
            pb2.GetSSHJobSubmissionRequest(submission_id=submission_id),
            metadata=self._metadata,
        )

    def add_unicore_job_submission_details(self, compute_resource_id, priority, unicore_job_submission):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.AddUnicoreJobSubmission(
            pb2.AddUnicoreJobSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, unicore_job_submission=unicore_job_submission),
            metadata=self._metadata,
        )
        return response.submission_id

    def get_unicore_job_submission(self, submission_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetUnicoreJobSubmission(
            pb2.GetUnicoreJobSubmissionRequest(submission_id=submission_id),
            metadata=self._metadata,
        )

    def add_cloud_job_submission_details(self, compute_resource_id, priority, cloud_job_submission):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.AddCloudJobSubmission(
            pb2.AddCloudJobSubmissionRequest(compute_resource_id=compute_resource_id, priority=priority, cloud_job_submission=cloud_job_submission),
            metadata=self._metadata,
        )
        return response.submission_id

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

    def delete_job_submission_interface(self, compute_resource_id, submission_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.DeleteJobSubmissionInterface(
            pb2.DeleteJobSubmissionInterfaceRequest(compute_resource_id=compute_resource_id, submission_id=submission_id),
            metadata=self._metadata,
        )

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
        response = self._gw_profile.RegisterGatewayResourceProfile(
            pb2.RegisterGatewayResourceProfileRequest(gateway_resource_profile=gateway_resource_profile),
            metadata=self._metadata,
        )
        return response.gateway_id

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
        response = self._gw_profile.GetAllComputePreferences(
            pb2.GetAllComputePreferencesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.compute_resource_preferences)

    def get_all_gateway_storage_preferences(self, gateway_id):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        response = self._gw_profile.GetAllStoragePreferences(
            pb2.GetAllStoragePreferencesRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.storage_preferences)

    def get_all_gateway_resource_profiles(self):
        pb2 = self._svc("gateway_resource_profile_service_pb2")
        response = self._gw_profile.GetAllGatewayResourceProfiles(
            pb2.GetAllGatewayResourceProfilesRequest(),
            metadata=self._metadata,
        )
        return list(response.gateway_resource_profiles)

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
        response = self._gw_profile.GetSSHAccountProvisioners(
            pb2.GetSSHAccountProvisionersRequest(),
            metadata=self._metadata,
        )
        return list(response.ssh_account_provisioners)

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
        response = self._grp_profile.GetGroupResourceList(
            pb2.GetGroupResourceListRequest(),
            metadata=self._metadata,
        )
        return list(response.group_resource_profiles)

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
        response = self._grp_profile.GetGroupComputePrefList(
            pb2.GetGroupComputePrefListRequest(group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )
        return list(response.group_compute_resource_preferences)

    def get_group_batch_queue_resource_policy_list(self, group_resource_profile_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        response = self._grp_profile.GetGroupBatchQueuePolicyList(
            pb2.GetGroupBatchQueuePolicyListRequest(group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )
        return list(response.batch_queue_resource_policies)

    def get_group_compute_resource_policy_list(self, group_resource_profile_id):
        pb2 = self._svc("group_resource_profile_service_pb2")
        response = self._grp_profile.GetGroupComputeResourcePolicyList(
            pb2.GetGroupComputeResourcePolicyListRequest(group_resource_profile_id=group_resource_profile_id),
            metadata=self._metadata,
        )
        return list(response.compute_resource_policies)

    def get_gateway_groups(self):
        pb2 = self._svc("group_resource_profile_service_pb2")
        return self._grp_profile.GetGatewayGroups(
            pb2.GetGatewayGroupsRequest(),
            metadata=self._metadata,
        )

    # ================================================================
    # User Resource Profile Service
    # ================================================================

    def register_user_resource_profile(self, user_resource_profile):
        pb2 = self._svc("user_resource_profile_service_pb2")
        response = self._user_profile.RegisterUserResourceProfile(
            pb2.RegisterUserResourceProfileRequest(user_resource_profile=user_resource_profile),
            metadata=self._metadata,
        )
        return response.user_id

    def is_user_resource_profile_exists(self, user_id, gateway_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        response = self._user_profile.IsUserResourceProfileExists(
            pb2.IsUserResourceProfileExistsRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return response.exists

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
        response = self._user_profile.GetAllUserComputePreferences(
            pb2.GetAllUserComputePreferencesRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.user_compute_resource_preferences)

    def get_all_user_storage_preferences(self, user_id, gateway_id):
        pb2 = self._svc("user_resource_profile_service_pb2")
        response = self._user_profile.GetAllUserStoragePreferences(
            pb2.GetAllUserStoragePreferencesRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.user_storage_preferences)

    def get_all_user_resource_profiles(self):
        pb2 = self._svc("user_resource_profile_service_pb2")
        response = self._user_profile.GetAllUserResourceProfiles(
            pb2.GetAllUserResourceProfilesRequest(),
            metadata=self._metadata,
        )
        return list(response.user_resource_profiles)

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
        response = self._user_profile.GetLatestQueueStatuses(
            pb2.GetLatestQueueStatusesRequest(),
            metadata=self._metadata,
        )
        return list(response.queue_statuses)
