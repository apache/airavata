
import copy
import datetime
import json
import logging
from urllib.parse import quote, urlencode

from django.conf import settings
from django.urls import reverse
from rest_framework import serializers

from airavata.model.appcatalog.appdeployment.ttypes import (
    ApplicationDeploymentDescription,
    ApplicationModule,
    CommandObject,
    SetEnvPaths
)
from airavata.model.appcatalog.appinterface.ttypes import (
    ApplicationInterfaceDescription
)
from airavata.model.appcatalog.computeresource.ttypes import (
    BatchQueue,
    ComputeResourceDescription
)
from airavata.model.appcatalog.gatewayprofile.ttypes import (
    GatewayResourceProfile,
    StoragePreference
)
from airavata.model.appcatalog.groupresourceprofile.ttypes import (
    ComputeResourceReservation,
    GroupComputeResourcePreference,
    GroupResourceProfile
)
from airavata.model.appcatalog.parser.ttypes import Parser
from airavata.model.appcatalog.storageresource.ttypes import (
    StorageResourceDescription
)
from airavata.model.application.io.ttypes import (
    InputDataObjectType,
    OutputDataObjectType
)
from airavata.model.credential.store.ttypes import (
    CredentialSummary,
    SummaryType
)
from airavata.model.data.replica.ttypes import (
    DataProductModel,
    DataReplicaLocationModel
)
from airavata.model.experiment.ttypes import (
    ExperimentModel,
    ExperimentStatistics,
    ExperimentSummaryModel
)
from airavata.model.group.ttypes import GroupModel, ResourcePermissionType
from airavata.model.job.ttypes import JobModel
from airavata.model.status.ttypes import ExperimentStatus
from airavata.model.user.ttypes import UserProfile
from airavata.model.workspace.ttypes import (
    Notification,
    NotificationPriority,
    Project
)

from . import data_products_helper, models, thrift_utils

log = logging.getLogger(__name__)


class FullyEncodedHyperlinkedIdentityField(
        serializers.HyperlinkedIdentityField):
    def get_url(self, obj, view_name, request, format):
        if hasattr(obj, self.lookup_field):
            lookup_value = getattr(obj, self.lookup_field)
        else:
            lookup_value = obj.get(self.lookup_field)
        try:
            encoded_lookup_value = quote(lookup_value, safe="")
        except Exception as e:
            log.warning(
                "Failed to encode lookup_value [{}] for lookup_field "
                "[{}] of object [{}]".format(
                    lookup_value, self.lookup_field, obj))
            raise
        # Bit of a hack. Django's URL reversing does URL encoding but it
        # doesn't encode all characters including some like '/' that are used
        # in URL mappings.
        kwargs = {self.lookup_url_kwarg: "__PLACEHOLDER__"}
        url = self.reverse(view_name, kwargs=kwargs,
                           request=request, format=format)
        return url.replace("__PLACEHOLDER__", encoded_lookup_value)


class UTCPosixTimestampDateTimeField(serializers.DateTimeField):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.default = self.current_time_ms
        self.initial = self.initial_value
        self.required = False

    def to_representation(self, obj):
        # Create datetime instance from milliseconds that is aware of timezon
        dt = datetime.datetime.fromtimestamp(obj / 1000, datetime.timezone.utc)
        return super().to_representation(dt)

    def to_internal_value(self, data):
        dt = super().to_internal_value(data)
        return int(dt.timestamp() * 1000)

    def initial_value(self):
        return self.to_representation(self.current_time_ms())

    def current_time_ms(self):
        return int(datetime.datetime.utcnow().timestamp() * 1000)


class StoredJSONField(serializers.JSONField):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def to_representation(self, value):
        try:
            if value:
                return json.loads(value)
            else:
                return value
        except Exception:
            return value

    def to_internal_value(self, data):
        try:
            return json.dumps(data)
        except (TypeError, ValueError):
            self.fail('invalid')


class OrderedListField(serializers.ListField):

    def __init__(self, *args, **kwargs):
        self.order_by = kwargs.pop('order_by', None)
        super().__init__(*args, **kwargs)

    def to_representation(self, instance):
        rep = super().to_representation(instance)
        if rep is not None:
            rep.sort(key=lambda item: item[self.order_by])
        return rep

    def to_internal_value(self, data):
        validated_data = super().to_internal_value(data)
        # Update order field based on order in array
        items = validated_data if validated_data else []
        for i in range(len(items)):
            items[i][self.order_by] = i
        return validated_data


class GroupSerializer(thrift_utils.create_serializer_class(GroupModel)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:group-detail',
        lookup_field='id',
        lookup_url_kwarg='group_id')
    isAdmin = serializers.SerializerMethodField()
    isOwner = serializers.SerializerMethodField()
    isMember = serializers.SerializerMethodField()
    isGatewayAdminsGroup = serializers.SerializerMethodField()
    isReadOnlyGatewayAdminsGroup = serializers.SerializerMethodField()
    isDefaultGatewayUsersGroup = serializers.SerializerMethodField()

    class Meta:
        required = ('name',)
        read_only = ('ownerId',)

    def create(self, validated_data):
        group = super().create(validated_data)
        group.ownerId = self.context['request'].user.username + \
            "@" + settings.GATEWAY_ID
        return group

    def update(self, instance, validated_data):
        instance.name = validated_data.get('name', instance.name)
        instance.description = validated_data.get(
            'description', instance.description)
        # Calculate added and removed members
        old_members = set(instance.members)
        new_members = set(validated_data.get('members', instance.members))
        removed_members = old_members - new_members
        added_members = new_members - old_members
        instance._removed_members = list(removed_members)
        instance._added_members = list(added_members)
        instance.members = validated_data.get('members', instance.members)
        # Calculate added and removed admins
        old_admins = set(instance.admins)
        new_admins = set(validated_data.get('admins', instance.admins))
        removed_admins = old_admins - new_admins
        added_admins = new_admins - old_admins
        instance._removed_admins = list(removed_admins)
        instance._added_admins = list(added_admins)
        instance.admins = validated_data.get('admins', instance.admins)
        # Add new admins that aren't members to the added_members list
        instance._added_members.extend(list(added_admins - new_members))
        instance.members.extend(list(added_admins - new_members))
        return instance

    def get_isAdmin(self, group):
        request = self.context['request']
        return request.profile_service['group_manager'].hasAdminAccess(
            request.authz_token,
            group.id,
            request.user.username + "@" + settings.GATEWAY_ID)

    def get_isOwner(self, group):
        request = self.context['request']
        return group.ownerId == (request.user.username +
                                 "@" +
                                 settings.GATEWAY_ID)

    def get_isMember(self, group):
        request = self.context['request']
        username = request.user.username + "@" + settings.GATEWAY_ID
        return group.members and username in group.members

    def get_isGatewayAdminsGroup(self, group):
        return group.id == self._gateway_groups()['adminsGroupId']

    def get_isReadOnlyGatewayAdminsGroup(self, group):
        return group.id == self._gateway_groups()['readOnlyAdminsGroupId']

    def get_isDefaultGatewayUsersGroup(self, group):
        return group.id == self._gateway_groups()['defaultGatewayUsersGroupId']

    def _gateway_groups(self):
        request = self.context['request']
        # gateway_groups_middleware sets this session variable
        if 'GATEWAY_GROUPS' in request.session:
            return request.session['GATEWAY_GROUPS']
        else:
            gateway_groups = request.airavata_client.getGatewayGroups(
                request.authz_token)
            return copy.deepcopy(gateway_groups.__dict__)


class ProjectSerializer(
        thrift_utils.create_serializer_class(Project)):

    class Meta:
        required = ('name',)
        read_only = ('owner', 'gatewayId')

    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:project-detail',
        lookup_field='projectID',
        lookup_url_kwarg='project_id')
    experiments = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:project-experiments',
        lookup_field='projectID',
        lookup_url_kwarg='project_id')
    creationTime = UTCPosixTimestampDateTimeField(allow_null=True)
    userHasWriteAccess = serializers.SerializerMethodField()
    isOwner = serializers.SerializerMethodField()

    def create(self, validated_data):
        return Project(**validated_data)

    def update(self, instance, validated_data):
        instance.name = validated_data.get('name', instance.name)
        instance.description = validated_data.get(
            'description', instance.description)
        return instance

    def get_userHasWriteAccess(self, project):
        request = self.context['request']
        return request.airavata_client.userHasAccess(
            request.authz_token, project.projectID,
            ResourcePermissionType.WRITE)

    def get_isOwner(self, project):
        request = self.context['request']
        return project.owner == request.user.username


class ApplicationModuleSerializer(
        thrift_utils.create_serializer_class(ApplicationModule)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:application-detail',
        lookup_field='appModuleId',
        lookup_url_kwarg='app_module_id')
    applicationInterface = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:application-application-interface',
        lookup_field='appModuleId',
        lookup_url_kwarg='app_module_id')
    applicationDeployments = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:application-application-deployments',
        lookup_field='appModuleId',
        lookup_url_kwarg='app_module_id')
    userHasWriteAccess = serializers.SerializerMethodField()

    class Meta:
        required = ('appModuleName',)

    def get_userHasWriteAccess(self, appDeployment):
        request = self.context['request']
        return request.is_gateway_admin


class InputDataObjectTypeSerializer(
        thrift_utils.create_serializer_class(InputDataObjectType)):

    metaData = StoredJSONField(required=False, allow_null=True)

    class Meta:
        required = ('name',)


class OutputDataObjectTypeSerializer(
        thrift_utils.create_serializer_class(OutputDataObjectType)):

    metaData = StoredJSONField(required=False, allow_null=True)

    class Meta:
        required = ('name',)


class ApplicationInterfaceDescriptionSerializer(
        thrift_utils.create_serializer_class(ApplicationInterfaceDescription)):

    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:application-interface-detail',
        lookup_field='applicationInterfaceId',
        lookup_url_kwarg='app_interface_id')
    applicationInputs = OrderedListField(
        order_by='inputOrder',
        child=InputDataObjectTypeSerializer(),
        allow_null=True)
    applicationOutputs = OutputDataObjectTypeSerializer(many=True)
    userHasWriteAccess = serializers.SerializerMethodField()

    def get_userHasWriteAccess(self, appDeployment):
        request = self.context['request']
        return request.is_gateway_admin


class CommandObjectSerializer(
        thrift_utils.create_serializer_class(CommandObject)):
    pass


class SetEnvPathsSerializer(
        thrift_utils.create_serializer_class(SetEnvPaths)):
    pass


class ApplicationDeploymentDescriptionSerializer(
        thrift_utils.create_serializer_class(
            ApplicationDeploymentDescription)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:application-deployment-detail',
        lookup_field='appDeploymentId',
        lookup_url_kwarg='app_deployment_id')
    # Default values returned in these results have been overridden with app
    # deployment defaults for any that exist
    queues = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:application-deployment-queues',
        lookup_field='appDeploymentId',
        lookup_url_kwarg='app_deployment_id')
    userHasWriteAccess = serializers.SerializerMethodField()
    moduleLoadCmds = OrderedListField(
        order_by='commandOrder',
        child=CommandObjectSerializer(),
        allow_null=True)
    preJobCommands = OrderedListField(
        order_by='commandOrder',
        child=CommandObjectSerializer(),
        allow_null=True)
    postJobCommands = OrderedListField(
        order_by='commandOrder',
        child=CommandObjectSerializer(),
        allow_null=True)
    libPrependPaths = OrderedListField(
        order_by='envPathOrder',
        child=SetEnvPathsSerializer(),
        allow_null=True)
    libAppendPaths = OrderedListField(
        order_by='envPathOrder',
        child=SetEnvPathsSerializer(),
        allow_null=True)
    setEnvironment = OrderedListField(
        order_by='envPathOrder',
        child=SetEnvPathsSerializer(),
        allow_null=True)

    def get_userHasWriteAccess(self, appDeployment):
        request = self.context['request']
        return request.airavata_client.userHasAccess(
            request.authz_token, appDeployment.appDeploymentId,
            ResourcePermissionType.WRITE)


class ComputeResourceDescriptionSerializer(
        thrift_utils.create_serializer_class(ComputeResourceDescription)):
    pass


class BatchQueueSerializer(thrift_utils.create_serializer_class(BatchQueue)):
    pass


class ExperimentStatusSerializer(
        thrift_utils.create_serializer_class(ExperimentStatus)):
    timeOfStateChange = UTCPosixTimestampDateTimeField()


class ExperimentSerializer(
        thrift_utils.create_serializer_class(ExperimentModel)):

    class Meta:
        required = ('projectId', 'experimentType', 'experimentName')
        read_only = ('userName', 'gatewayId')

    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:experiment-detail',
        lookup_field='experimentId',
        lookup_url_kwarg='experiment_id')
    full_experiment = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:full-experiment-detail',
        lookup_field='experimentId',
        lookup_url_kwarg='experiment_id')
    project = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:project-detail',
        lookup_field='projectId',
        lookup_url_kwarg='project_id')
    jobs = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:experiment-jobs',
        lookup_field='experimentId',
        lookup_url_kwarg='experiment_id')
    shared_entity = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:shared-entity-detail',
        lookup_field='experimentId',
        lookup_url_kwarg='entity_id')
    experimentInputs = serializers.ListField(
        child=InputDataObjectTypeSerializer(),
        allow_null=True)
    experimentOutputs = serializers.ListField(
        child=OutputDataObjectTypeSerializer(),
        allow_null=True)
    creationTime = UTCPosixTimestampDateTimeField(allow_null=True)
    experimentStatus = ExperimentStatusSerializer(many=True, allow_null=True)
    userHasWriteAccess = serializers.SerializerMethodField()
    relativeExperimentDataDir = serializers.SerializerMethodField()

    def get_userHasWriteAccess(self, experiment):
        request = self.context['request']
        return request.airavata_client.userHasAccess(
            request.authz_token, experiment.experimentId,
            ResourcePermissionType.WRITE)

    def get_relativeExperimentDataDir(self, experiment):
        if (experiment.userConfigurationData and
                experiment.userConfigurationData.experimentDataDir):
            request = self.context['request']
            data_dir = experiment.userConfigurationData.experimentDataDir
            if data_products_helper.dir_exists(request, data_dir):
                return data_products_helper.get_rel_path(request, data_dir)
            else:
                return None
        else:
            return None


class DataReplicaLocationSerializer(
        thrift_utils.create_serializer_class(DataReplicaLocationModel)):
    creationTime = UTCPosixTimestampDateTimeField()
    lastModifiedTime = UTCPosixTimestampDateTimeField()


class DataProductSerializer(
        thrift_utils.create_serializer_class(DataProductModel)):
    creationTime = UTCPosixTimestampDateTimeField()
    lastModifiedTime = UTCPosixTimestampDateTimeField()
    replicaLocations = DataReplicaLocationSerializer(many=True)
    downloadURL = serializers.SerializerMethodField()
    isInputFileUpload = serializers.SerializerMethodField()

    def get_downloadURL(self, data_product):
        """Getter for downloadURL field."""
        request = self.context['request']
        if data_products_helper.exists(request, data_product):
            return (request.build_absolute_uri(
                reverse('django_airavata_api:download_file')) +
                '?' +
                urlencode({'data-product-uri': data_product.productUri}))
        return None

    def get_isInputFileUpload(self, data_product):
        """Return True if this is an uploaded input file."""
        request = self.context['request']
        return data_products_helper.is_input_file_upload(request, data_product)


# TODO move this into airavata_sdk?
class FullExperiment:
    """Experiment with referenced data models."""

    def __init__(self, experimentModel, project=None, outputDataProducts=None,
                 inputDataProducts=None, applicationModule=None,
                 computeResource=None, jobDetails=None, outputViews=None):
        self.experiment = experimentModel
        self.experimentId = experimentModel.experimentId
        self.project = project
        self.outputDataProducts = outputDataProducts
        self.inputDataProducts = inputDataProducts
        self.applicationModule = applicationModule
        self.computeResource = computeResource
        self.jobDetails = jobDetails
        self.outputViews = outputViews


class JobSerializer(thrift_utils.create_serializer_class(JobModel)):
    creationTime = UTCPosixTimestampDateTimeField()


class FullExperimentSerializer(serializers.Serializer):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:full-experiment-detail',
        lookup_field='experimentId',
        lookup_url_kwarg='experiment_id')
    experiment = ExperimentSerializer()
    outputDataProducts = DataProductSerializer(many=True, read_only=True)
    inputDataProducts = DataProductSerializer(many=True, read_only=True)
    applicationModule = ApplicationModuleSerializer(read_only=True)
    computeResource = ComputeResourceDescriptionSerializer(read_only=True)
    project = ProjectSerializer(read_only=True)
    jobDetails = JobSerializer(many=True, read_only=True)
    outputViews = serializers.DictField(read_only=True)

    def create(self, validated_data):
        raise Exception("Not implemented")

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class ExperimentSummarySerializer(
        thrift_utils.create_serializer_class(ExperimentSummaryModel)):
    creationTime = UTCPosixTimestampDateTimeField()
    statusUpdateTime = UTCPosixTimestampDateTimeField()
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:experiment-detail',
        lookup_field='experimentId',
        lookup_url_kwarg='experiment_id')
    project = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:project-detail',
        lookup_field='projectId',
        lookup_url_kwarg='project_id')
    userHasWriteAccess = serializers.SerializerMethodField()

    def get_userHasWriteAccess(self, experiment):
        request = self.context['request']
        return request.airavata_client.userHasAccess(
            request.authz_token, experiment.experimentId,
            ResourcePermissionType.WRITE)


class UserProfileSerializer(
        thrift_utils.create_serializer_class(UserProfile)):
    creationTime = UTCPosixTimestampDateTimeField()
    lastAccessTime = UTCPosixTimestampDateTimeField()


class ComputeResourceReservationSerializer(
        thrift_utils.create_serializer_class(ComputeResourceReservation)):
    startTime = UTCPosixTimestampDateTimeField(allow_null=True)
    endTime = UTCPosixTimestampDateTimeField(allow_null=True)


class GroupComputeResourcePreferenceSerializer(
        thrift_utils.create_serializer_class(GroupComputeResourcePreference)):
    reservations = ComputeResourceReservationSerializer(many=True)


class GroupResourceProfileSerializer(
        thrift_utils.create_serializer_class(GroupResourceProfile)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:group-resource-profile-detail',
        lookup_field='groupResourceProfileId',
        lookup_url_kwarg='group_resource_profile_id')
    creationTime = UTCPosixTimestampDateTimeField(allow_null=True)
    updatedTime = UTCPosixTimestampDateTimeField(allow_null=True)
    userHasWriteAccess = serializers.SerializerMethodField()
    computePreferences = GroupComputeResourcePreferenceSerializer(many=True)

    class Meta:
        required = ('groupResourceProfileName',)

    def update(self, instance, validated_data):
        result = super().update(instance, validated_data)
        result._removed_compute_resource_preferences = []
        result._removed_compute_resource_policies = []
        result._removed_batch_queue_resource_policies = []
        # Find all compute resource preferences that were removed
        for compute_resource_preference in instance.computePreferences:
            existing_compute_resource_preference = next(
                (pref for pref in result.computePreferences
                 if pref.computeResourceId ==
                    compute_resource_preference.computeResourceId),
                None)
            if not existing_compute_resource_preference:
                result._removed_compute_resource_preferences.append(
                    compute_resource_preference)
        # Find all compute resource policies that were removed
        for compute_resource_policy in instance.computeResourcePolicies:
            existing_compute_resource_policy = next(
                (pol for pol in result.computeResourcePolicies
                 if pol.resourcePolicyId ==
                    compute_resource_policy.resourcePolicyId),
                None)
            if not existing_compute_resource_policy:
                result._removed_compute_resource_policies.append(
                    compute_resource_policy)
        # Find all batch queue resource policies that were removed
        for batch_queue_resource_policy in instance.batchQueueResourcePolicies:
            existing_batch_queue_resource_policy_for_update = next(
                (bq for bq in result.batchQueueResourcePolicies
                 if bq.resourcePolicyId ==
                    batch_queue_resource_policy.resourcePolicyId),
                None)
            if not existing_batch_queue_resource_policy_for_update:
                result._removed_batch_queue_resource_policies.append(
                    batch_queue_resource_policy)
        return result

    def get_userHasWriteAccess(self, groupResourceProfile):
        request = self.context['request']
        return request.airavata_client.userHasAccess(
            request.authz_token, groupResourceProfile.groupResourceProfileId,
            ResourcePermissionType.WRITE)


class UserPermissionSerializer(serializers.Serializer):
    user = UserProfileSerializer()
    permissionType = serializers.IntegerField()


class GroupPermissionSerializer(serializers.Serializer):
    group = GroupSerializer()
    permissionType = serializers.IntegerField()


class SharedEntitySerializer(serializers.Serializer):

    entityId = serializers.CharField(read_only=True)
    userPermissions = UserPermissionSerializer(many=True)
    groupPermissions = GroupPermissionSerializer(many=True)
    owner = UserProfileSerializer(read_only=True)
    isOwner = serializers.SerializerMethodField()
    hasSharingPermission = serializers.SerializerMethodField()

    def create(self, validated_data):
        raise Exception("Not implemented")

    def update(self, instance, validated_data):
        # Compute lists of ids to grant/revoke READ/WRITE/MANAGE_SHARING permission
        existing_user_permissions = {
            user['user'].airavataInternalUserId: user['permissionType']
            for user in instance['userPermissions']}
        new_user_permissions = {
            user['user']['airavataInternalUserId']:
            user['permissionType']
                for user in validated_data['userPermissions']}

        (user_grant_read_permission, user_grant_write_permission, user_grant_manage_sharing_permission,
         user_revoke_read_permission, user_revoke_write_permission, user_revoke_manage_sharing_permission) = \
            self._compute_all_revokes_and_grants(existing_user_permissions,
                                                 new_user_permissions)

        existing_group_permissions = {
            group['group'].id: group['permissionType']
            for group in instance['groupPermissions']}
        new_group_permissions = {
            group['group']['id']: group['permissionType']
            for group in validated_data['groupPermissions']}

        (group_grant_read_permission, group_grant_write_permission, group_grant_manage_sharing_permission,
         group_revoke_read_permission, group_revoke_write_permission, group_revoke_manage_sharing_permission) = \
            self._compute_all_revokes_and_grants(existing_group_permissions,
                                                 new_group_permissions)

        instance['_user_grant_read_permission'] = user_grant_read_permission
        instance['_user_grant_write_permission'] = user_grant_write_permission
        instance['_user_grant_manage_sharing_permission'] = user_grant_manage_sharing_permission
        instance['_user_revoke_read_permission'] = user_revoke_read_permission
        instance['_user_revoke_write_permission'] = user_revoke_write_permission
        instance['_user_revoke_manage_sharing_permission'] = user_revoke_manage_sharing_permission
        instance['_group_grant_read_permission'] = group_grant_read_permission
        instance['_group_grant_write_permission'] = group_grant_write_permission
        instance['_group_grant_manage_sharing_permission'] = group_grant_manage_sharing_permission
        instance['_group_revoke_read_permission'] = group_revoke_read_permission
        instance['_group_revoke_write_permission'] = group_revoke_write_permission
        instance['_group_revoke_manage_sharing_permission'] = group_revoke_manage_sharing_permission
        instance['userPermissions'] = [
            {'user': UserProfile(**data['user']),
             'permissionType': data['permissionType']}
            for data in validated_data.get(
                'userPermissions', instance['userPermissions'])]
        instance['groupPermissions'] = [
            {'group': GroupModel(**data['group']),
             'permissionType': data['permissionType']}
            for data in validated_data.get('groupPermissions', instance['groupPermissions'])]
        return instance

    def _compute_all_revokes_and_grants(self, existing_permissions,
                                        new_permissions):
        grant_read_permission = []
        grant_write_permission = []
        grant_manage_sharing_permission = []
        revoke_read_permission = []
        revoke_write_permission = []
        revoke_manage_sharing_permission = []
        # Union the two sets of user/group ids
        all_ids = existing_permissions.keys() | new_permissions.keys()
        for id in all_ids:
            revokes, grants = self._compute_revokes_and_grants(
                existing_permissions.get(id),
                new_permissions.get(id)
            )
            if ResourcePermissionType.READ in revokes:
                revoke_read_permission.append(id)
            if ResourcePermissionType.WRITE in revokes:
                revoke_write_permission.append(id)
            if ResourcePermissionType.MANAGE_SHARING in revokes:
                revoke_manage_sharing_permission.append(id)
            if ResourcePermissionType.READ in grants:
                grant_read_permission.append(id)
            if ResourcePermissionType.WRITE in grants:
                grant_write_permission.append(id)
            if ResourcePermissionType.MANAGE_SHARING in grants:
                grant_manage_sharing_permission.append(id)
        return (grant_read_permission, grant_write_permission, grant_manage_sharing_permission,
                revoke_read_permission, revoke_write_permission, revoke_manage_sharing_permission)

    def _compute_revokes_and_grants(self, current_permission=None,
                                    new_permission=None):
        read_permissions = set((ResourcePermissionType.READ,))
        write_permissions = set((ResourcePermissionType.READ,
                                 ResourcePermissionType.WRITE))
        manage_share_permissions = set((ResourcePermissionType.READ, ResourcePermissionType.WRITE, ResourcePermissionType.MANAGE_SHARING))
        current_permissions_set = set()
        new_permissions_set = set()
        if current_permission == ResourcePermissionType.READ:
            current_permissions_set = read_permissions
        elif current_permission == ResourcePermissionType.WRITE:
            current_permissions_set = write_permissions
        elif current_permission == ResourcePermissionType.MANAGE_SHARING:
            current_permissions_set = manage_share_permissions
        if new_permission == ResourcePermissionType.READ:
            new_permissions_set = read_permissions
        elif new_permission == ResourcePermissionType.WRITE:
            new_permissions_set = write_permissions
        elif new_permission == ResourcePermissionType.MANAGE_SHARING:
            new_permissions_set = manage_share_permissions

        # return tuple: permissions to revoke and permissions to grant
        return (current_permissions_set - new_permissions_set,
                new_permissions_set - current_permissions_set)

    def get_isOwner(self, shared_entity):
        request = self.context['request']
        return shared_entity['owner'].userId == request.user.username

    def get_hasSharingPermission(self, shared_entity):
        request = self.context['request']
        return request.airavata_client.userHasAccess(
            request.authz_token, shared_entity['entityId'],
            ResourcePermissionType.MANAGE_SHARING)


class CredentialSummarySerializer(
        thrift_utils.create_serializer_class(CredentialSummary)):
    type = thrift_utils.ThriftEnumField(SummaryType)
    persistedTime = UTCPosixTimestampDateTimeField()
    userHasWriteAccess = serializers.SerializerMethodField()

    def get_userHasWriteAccess(self, credential_summary):
        request = self.context['request']
        return request.airavata_client.userHasAccess(
            request.authz_token, credential_summary.token,
            ResourcePermissionType.WRITE)


class StoragePreferenceSerializer(
        thrift_utils.create_serializer_class(StoragePreference)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:storage-preference-detail',
        lookup_field='storageResourceId',
        lookup_url_kwarg='storage_resource_id')

    def to_representation(self, instance):
        ret = super().to_representation(instance)
        # Convert empty string to null
        if ret['resourceSpecificCredentialStoreToken'] == '':
            ret['resourceSpecificCredentialStoreToken'] = None
        return ret


class GatewayResourceProfileSerializer(
        thrift_utils.create_serializer_class(GatewayResourceProfile)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:gateway-resource-profile-detail',
        lookup_field='gatewayID',
        lookup_url_kwarg='gateway_id')
    storagePreferences = StoragePreferenceSerializer(many=True)
    userHasWriteAccess = serializers.SerializerMethodField()

    def get_userHasWriteAccess(self, gatewayResourceProfile):
        request = self.context['request']
        return request.is_gateway_admin


class StorageResourceSerializer(
        thrift_utils.create_serializer_class(StorageResourceDescription)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:storage-resource-detail',
        lookup_field='storageResourceId',
        lookup_url_kwarg='storage_resource_id')
    creationTime = UTCPosixTimestampDateTimeField()
    updateTime = UTCPosixTimestampDateTimeField()


class ParserSerializer(thrift_utils.create_serializer_class(Parser)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:parser-detail',
        lookup_field='id',
        lookup_url_kwarg='parser_id')


class UserStorageFileSerializer(serializers.Serializer):
    name = serializers.CharField()
    downloadURL = serializers.SerializerMethodField()
    dataProductURI = serializers.CharField(source='data-product-uri')
    createdTime = serializers.DateTimeField(source='created_time')
    size = serializers.IntegerField()
    hidden = serializers.BooleanField()

    def get_downloadURL(self, file):
        """Getter for downloadURL field."""
        request = self.context['request']
        return (request.build_absolute_uri(
            reverse('django_airavata_api:download_file')) +
            '?' +
            urlencode({'data-product-uri': file['data-product-uri']}))


class UserStorageDirectorySerializer(serializers.Serializer):
    name = serializers.CharField()
    path = serializers.CharField()
    createdTime = serializers.DateTimeField(source='created_time')
    size = serializers.IntegerField()
    hidden = serializers.BooleanField()
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:user-storage-items',
        lookup_field='path',
        lookup_url_kwarg='path')


class UserStoragePathSerializer(serializers.Serializer):
    directories = UserStorageDirectorySerializer(many=True)
    files = UserStorageFileSerializer(many=True)
    parts = serializers.ListField(child=serializers.CharField())
    # uploaded is populated after a file upload
    uploaded = DataProductSerializer(read_only=True)


# ModelSerializers
class ApplicationPreferencesSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.ApplicationPreferences
        exclude = ('id', 'username', 'workspace_preferences')


class WorkspacePreferencesSerializer(serializers.ModelSerializer):
    application_preferences = ApplicationPreferencesSerializer(
        source="applicationpreferences_set", many=True)

    class Meta:
        model = models.WorkspacePreferences
        exclude = ('username',)


class IAMUserProfile(serializers.Serializer):
    airavataInternalUserId = serializers.CharField()
    userId = serializers.CharField()
    gatewayId = serializers.CharField()
    email = serializers.CharField()
    firstName = serializers.CharField()
    lastName = serializers.CharField()
    enabled = serializers.BooleanField()
    emailVerified = serializers.BooleanField()
    airavataUserProfileExists = serializers.BooleanField()
    creationTime = UTCPosixTimestampDateTimeField()
    groups = GroupSerializer(many=True)
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:iam-user-profile-detail',
        lookup_field='userId',
        lookup_url_kwarg='user_id')
    userHasWriteAccess = serializers.SerializerMethodField()

    def update(self, instance, validated_data):
        existing_group_ids = [group.id for group in instance['groups']]
        new_group_ids = [group['id'] for group in validated_data['groups']]
        instance['_added_group_ids'] = list(
            set(new_group_ids) - set(existing_group_ids))
        instance['_removed_group_ids'] = list(
            set(existing_group_ids) - set(new_group_ids))
        return instance

    def get_userHasWriteAccess(self, userProfile):
        request = self.context['request']
        return request.is_gateway_admin


class AckNotificationSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.User_Notifications


class NotificationSerializer(
        thrift_utils.create_serializer_class(Notification)):
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:manage-notifications-detail',
        lookup_field='notificationId',
        lookup_url_kwarg='notification_id')
    priority = thrift_utils.ThriftEnumField(NotificationPriority)
    creationTime = UTCPosixTimestampDateTimeField(allow_null=True)
    publishedTime = UTCPosixTimestampDateTimeField()
    expirationTime = UTCPosixTimestampDateTimeField()
    userHasWriteAccess = serializers.SerializerMethodField()

    def get_userHasWriteAccess(self, userProfile):
        request = self.context['request']
        return request.is_gateway_admin


class ExperimentStatisticsSerializer(
        thrift_utils.create_serializer_class(ExperimentStatistics)):
    allExperiments = ExperimentSummarySerializer(many=True)
    completedExperiments = ExperimentSummarySerializer(many=True)
    failedExperiments = ExperimentSummarySerializer(many=True)
    cancelledExperiments = ExperimentSummarySerializer(many=True)
    createdExperiments = ExperimentSummarySerializer(many=True)
    runningExperiments = ExperimentSummarySerializer(many=True)


class UnverifiedEmailUserProfile(serializers.Serializer):
    userId = serializers.CharField()
    gatewayId = serializers.CharField()
    email = serializers.CharField()
    firstName = serializers.CharField()
    lastName = serializers.CharField()
    enabled = serializers.BooleanField()
    emailVerified = serializers.BooleanField()
    creationTime = UTCPosixTimestampDateTimeField()
    url = FullyEncodedHyperlinkedIdentityField(
        view_name='django_airavata_api:unverified-email-user-profile-detail',
        lookup_field='userId',
        lookup_url_kwarg='user_id')
    userHasWriteAccess = serializers.SerializerMethodField()

    def get_userHasWriteAccess(self, userProfile):
        request = self.context['request']
        return request.is_gateway_admin


class LogRecordSerializer(serializers.Serializer):
    level = serializers.CharField()
    message = serializers.CharField()
    details = StoredJSONField()
    stacktrace = serializers.ListField(child=serializers.CharField())


class SettingsSerializer(serializers.Serializer):
    fileUploadMaxFileSize = serializers.IntegerField()
    tusEndpoint = serializers.CharField()
    pgaUrl = serializers.CharField()
