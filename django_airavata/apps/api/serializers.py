
import copy
import datetime
import json
import logging
from urllib.parse import quote, urlencode

from django.conf import settings
from django.urls import reverse
from rest_framework import serializers

from airavata.model.appcatalog.appdeployment.ttypes import (ApplicationDeploymentDescription,
                                                            ApplicationModule,
                                                            CommandObject,
                                                            SetEnvPaths)
from airavata.model.appcatalog.appinterface.ttypes import \
    ApplicationInterfaceDescription
from airavata.model.appcatalog.computeresource.ttypes \
    import (BatchQueue,
            ComputeResourceDescription)
from airavata.model.appcatalog.groupresourceprofile.ttypes import \
    GroupResourceProfile
from airavata.model.application.io.ttypes import (InputDataObjectType,
                                                  OutputDataObjectType)
from airavata.model.data.replica.ttypes import (DataProductModel,
                                                DataReplicaLocationModel)
from airavata.model.experiment.ttypes import (ExperimentModel,
                                              ExperimentSummaryModel)
from airavata.model.group.ttypes import GroupModel, ResourcePermissionType
from airavata.model.job.ttypes import JobModel
from airavata.model.status.ttypes import ExperimentStatus
from airavata.model.user.ttypes import UserProfile
from airavata.model.workspace.ttypes import Project

from . import datastore
from . import thrift_utils

log = logging.getLogger(__name__)


class FullyEncodedHyperlinkedIdentityField(serializers.HyperlinkedIdentityField):
    def get_url(self, obj, view_name, request, format):
        if hasattr(obj, self.lookup_field):
            lookup_value = getattr(obj, self.lookup_field)
        else:
            lookup_value = obj.get(self.lookup_field)
        encoded_lookup_value = quote(lookup_value, safe="")
        # Bit of a hack. Django's URL reversing does URL encoding but it doesn't
        # encode all characters including some like '/' that are used in URL
        # mappings.
        kwargs = {self.lookup_url_kwarg: "__PLACEHOLDER__"}
        url = self.reverse(view_name, kwargs=kwargs, request=request, format=format)
        return url.replace("__PLACEHOLDER__", encoded_lookup_value)


class UTCPosixTimestampDateTimeField(serializers.DateTimeField):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.default = self.current_time_ms
        self.initial = self.initial_value
        self.required = False

    def to_representation(self, obj):
        # Create datetime instance from milliseconds that is aware of timezon
        dt = datetime.datetime.fromtimestamp(obj/1000, datetime.timezone.utc)
        return super().to_representation(dt)

    def to_internal_value(self, data):
        dt = super().to_internal_value(data)
        return int(dt.timestamp() * 1000)

    def initial_value(self):
        return self.to_representation(self.current_time_ms())

    def current_time_ms(self):
        return int(datetime.datetime.utcnow().timestamp() * 1000)


class GetGatewayUsername(object):

    def __call__(self):
        return self.field.context['request'].user.username

    def set_context(self, field):
        self.field = field


class GetGatewayUserId(object):

    def __call__(self):
        return self.field.context['request'].user.id

    def set_context(self, field):
        self.field = field


class GatewayUsernameDefaultField(serializers.CharField):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.read_only = True
        self.default = GetGatewayUsername()


class GatewayUserIdDefaultField(serializers.CharField):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.read_only = True
        self.default = GetGatewayUserId()


class GatewayIdDefaultField(serializers.CharField):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.read_only = True
        self.default = settings.GATEWAY_ID


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


class GroupSerializer(serializers.Serializer):
    url = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:group-detail', lookup_field='id', lookup_url_kwarg='group_id')
    id = serializers.CharField(default=GroupModel.thrift_spec[1][4], allow_null=True)
    name = serializers.CharField(required=True)
    description = serializers.CharField(allow_null=True, allow_blank=True)
    ownerId = serializers.CharField(read_only=True)
    members = serializers.ListSerializer(child=serializers.CharField())
    isAdmin = serializers.SerializerMethodField()
    isOwner = serializers.SerializerMethodField()
    isMember = serializers.SerializerMethodField()
    isGatewayAdminsGroup = serializers.SerializerMethodField()
    isReadOnlyGatewayAdminsGroup = serializers.SerializerMethodField()
    isDefaultGatewayUsersGroup = serializers.SerializerMethodField()

    def create(self, validated_data):
        validated_data['ownerId'] = self.context['request'].user.username + "@" + settings.GATEWAY_ID
        return GroupModel(**validated_data)

    def update(self, instance, validated_data):
        instance.name = validated_data.get('name', instance.name)
        instance.description = validated_data.get('description', instance.description)
        # Calculate added and removed members
        old_members = set(instance.members)
        new_members = set(validated_data.get('members', instance.members))
        removed_members = old_members - new_members
        added_members = new_members - old_members
        instance._removed_members = list(removed_members)
        instance._added_members = list(added_members)
        instance.members = validated_data.get('members', instance.members)
        return instance

    def get_isAdmin(self, group):
        request = self.context['request']
        return request.profile_service['group_manager'].hasAdminAccess(
            request.authz_token, group.id, request.user.username + "@" + settings.GATEWAY_ID)

    def get_isOwner(self, group):
        request = self.context['request']
        return group.ownerId == request.user.username + "@" + settings.GATEWAY_ID

    def get_isMember(self, group):
        request = self.context['request']
        username = request.user.username + "@" + settings.GATEWAY_ID
        return group.members and username in group.members

    def get_isGatewayAdminsGroup(self, group):
        request = self.context['request']
        return group.id == self._gateway_groups()['adminsGroupId']

    def get_isReadOnlyGatewayAdminsGroup(self, group):
        request = self.context['request']
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


class ProjectSerializer(serializers.Serializer):
    url = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:project-detail', lookup_field='projectID', lookup_url_kwarg='project_id')
    projectID = serializers.CharField(default=Project.thrift_spec[1][4], read_only=True)
    name = serializers.CharField(required=True)
    description = serializers.CharField(allow_null=True)
    owner = GatewayUsernameDefaultField()
    gatewayId = GatewayIdDefaultField()
    experiments = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:project-experiments', lookup_field='projectID', lookup_url_kwarg='project_id')
    creationTime = UTCPosixTimestampDateTimeField(allow_null=True)

    def create(self, validated_data):
        return Project(**validated_data)

    def update(self, instance, validated_data):
        instance.name = validated_data.get('name', instance.name)
        instance.description = validated_data.get('description', instance.description)
        return instance


class ApplicationModuleSerializer(serializers.Serializer):
    url = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:application-detail', lookup_field='appModuleId', lookup_url_kwarg='app_module_id')
    appModuleId = serializers.CharField(required=True)
    appModuleName = serializers.CharField(required=True)
    appModuleDescription = serializers.CharField()
    appModuleVersion = serializers.CharField()
    applicationInterface = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:application-application-interface', lookup_field='appModuleId', lookup_url_kwarg='app_module_id')
    applicationDeployments = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:application-application-deployments', lookup_field='appModuleId', lookup_url_kwarg='app_module_id')


    def create(self, validated_data):
        return ApplicationModule(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class InputDataObjectTypeSerializer(serializers.Serializer):
    name = serializers.CharField(required=False)
    value = serializers.CharField(required=False)
    type = serializers.IntegerField(required=False)
    applicationArgument = serializers.CharField(required=False)
    standardInput = serializers.BooleanField(required=False)
    metaData = StoredJSONField(required=False)
    inputOrder = serializers.IntegerField(required=False)
    isRequired = serializers.BooleanField(required=False)
    requiredToAddedToCommandLine = serializers.BooleanField(required=False)
    dataStaged = serializers.BooleanField(required=False)
    storageResourceId = serializers.CharField(required=False)
    isReadOnly = serializers.BooleanField(required=False)

    def create(self, validated_data):
        return InputDataObjectType(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class OutputDataObjectTypeSerializer(serializers.Serializer):
    name = serializers.CharField(required=False)
    value = serializers.CharField(required=False)
    type = serializers.IntegerField(required=False)
    applicationArgument = serializers.CharField(required=False)
    isRequired = serializers.BooleanField(required=False)
    requiredToAddedToCommandLine = serializers.BooleanField(required=False)
    dataMovement = serializers.CharField(required=False)
    location = serializers.CharField(required=False)
    searchQuery = serializers.CharField(required=False)
    outputStreaming = serializers.BooleanField(required=False)
    storageResourceId = serializers.CharField(required=False)

    def create(self, validated_data):
        return OutputDataObjectType(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class CustomSerializer(serializers.Serializer):
    def process_list_fields(self, validated_data):
        fields = self.fields
        params = copy.deepcopy(validated_data)
        for field_name, serializer in fields.items():
            if isinstance(serializer, serializers.ListSerializer):
                   params[field_name] = serializer.create(params[field_name])
        return params


class ApplicationInterfaceDescriptionSerializer(CustomSerializer):
    url = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:application-interface-detail', lookup_field='applicationInterfaceId', lookup_url_kwarg='app_interface_id')
    applicationInterfaceId = serializers.CharField(read_only=True)
    applicationName = serializers.CharField(required=False)
    applicationDescription = serializers.CharField(required=False)
    archiveWorkingDirectory = serializers.BooleanField(required=False)
    hasOptionalFileInputs = serializers.BooleanField(required=False)
    applicationOutputs = serializers.ListSerializer(child=OutputDataObjectTypeSerializer())
    applicationInputs = serializers.ListSerializer(child=InputDataObjectTypeSerializer())
    applicationModules = serializers.ListSerializer(child=serializers.CharField())

    def create(self, validated_data):
        params=self.process_list_fields(validated_data)
        return ApplicationInterfaceDescription(**params)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class CommandObjectSerializer(CustomSerializer):
    command = serializers.CharField()
    commandOrder = serializers.IntegerField()

    def create(self, validated_data):
        return CommandObject(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class SetEnvPathsSerializer(CustomSerializer):
    name=serializers.CharField(required=False)
    value=serializers.CharField(required=False)
    envPathOrder=serializers.IntegerField(required=False)

    def create(self, validated_data):
        return SetEnvPaths(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class ApplicationDeploymentDescriptionSerializer(thrift_utils.create_serializer_class(ApplicationDeploymentDescription)):
    url = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:application-deployment-detail', lookup_field='appDeploymentId', lookup_url_kwarg='app_deployment_id')
    # Default values returned in these results have been overridden with app deployment defaults for any that exist
    queues = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:application-deployment-queues', lookup_field='appDeploymentId', lookup_url_kwarg='app_deployment_id')


class ComputeResourceDescriptionSerializer(thrift_utils.create_serializer_class(ComputeResourceDescription)):
    pass


class BatchQueueSerializer(thrift_utils.create_serializer_class(BatchQueue)):
    pass


class ExperimentStatusSerializer(thrift_utils.create_serializer_class(ExperimentStatus)):
    timeOfStateChange = UTCPosixTimestampDateTimeField()


class ExperimentSerializer(
        thrift_utils.create_serializer_class(ExperimentModel)):

    class Meta:
        required = ('projectId', 'experimentType', 'experimentName')
        read_only = ('experimentId',)

    url = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:experiment-detail', lookup_field='experimentId', lookup_url_kwarg='experiment_id')
    full_experiment = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:full-experiment-detail', lookup_field='experimentId', lookup_url_kwarg='experiment_id')
    project = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:project-detail', lookup_field='projectId', lookup_url_kwarg='project_id')
    jobs = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:experiment-jobs', lookup_field='experimentId', lookup_url_kwarg='experiment_id')
    shared_entity = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:shared-entity-detail', lookup_field='experimentId', lookup_url_kwarg='entity_id')
    userName = GatewayUsernameDefaultField()
    gatewayId = GatewayIdDefaultField()
    creationTime = UTCPosixTimestampDateTimeField(allow_null=True)
    experimentStatus = ExperimentStatusSerializer(many=True, read_only=True)


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

    def get_downloadURL(self, data_product):
        """Getter for downloadURL field."""
        if datastore.exists(data_product):
            request = self.context['request']
            return (request.build_absolute_uri(
                reverse('django_airavata_api:download_file'))
                + '?'
                + urlencode({'data-product-uri': data_product.productUri}))
        return None


# TODO move this into airavata_sdk?
class FullExperiment:
    """Experiment with referenced data models."""

    def __init__(self, experimentModel, project=None, outputDataProducts=None,
                 inputDataProducts=None, applicationModule=None,
                 computeResource=None, jobDetails=None):
        self.experiment = experimentModel
        self.experimentId = experimentModel.experimentId
        self.project = project
        self.outputDataProducts = outputDataProducts
        self.inputDataProducts = inputDataProducts
        self.applicationModule = applicationModule
        self.computeResource = computeResource
        self.jobDetails = jobDetails


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

    def create(self, validated_data):
        raise Exception("Not implemented")

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class ExperimentSummarySerializer(
        thrift_utils.create_serializer_class(ExperimentSummaryModel)):
    creationTime = UTCPosixTimestampDateTimeField()
    statusUpdateTime = UTCPosixTimestampDateTimeField()
    url = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:experiment-detail', lookup_field='experimentId', lookup_url_kwarg='experiment_id')
    project = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:project-detail', lookup_field='projectId', lookup_url_kwarg='project_id')


class UserProfileSerializer(
        thrift_utils.create_serializer_class(UserProfile)):
    creationTime = UTCPosixTimestampDateTimeField()
    lastAccessTime = UTCPosixTimestampDateTimeField()


class GroupResourceProfileSerializer(
        thrift_utils.create_serializer_class(GroupResourceProfile)):
    url = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:group-resource-profile-detail', lookup_field='groupResourceProfileId', lookup_url_kwarg='group_resource_profile_id')
    creationTime = UTCPosixTimestampDateTimeField(allow_null=True)
    updatedTime = UTCPosixTimestampDateTimeField(allow_null=True)


class SharedGroups(serializers.Serializer):
    groupList=serializers.ListField(child=serializers.CharField())
    entityId=serializers.CharField()

    def update(self, instance, validated_data):
        instance["groupList"]=validated_data["groupList"]
        return instance


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

    def create(self, validated_data):
        raise Exception("Not implemented")

    def update(self, instance, validated_data):
        # Compute lists of ids to grant/revoke READ/WRITE
        existing_user_permissions = {user['user'].airavataInternalUserId: user['permissionType']
                                     for user in instance['userPermissions']}
        new_user_permissions = {user['user']['airavataInternalUserId']: user['permissionType']
                                for user in validated_data['userPermissions']}

        (user_grant_read_permission, user_grant_write_permission,
         user_revoke_read_permission, user_revoke_write_permission) = \
            self._compute_all_revokes_and_grants(existing_user_permissions,
                                                 new_user_permissions)

        existing_group_permissions = {
            group['group'].id: group['permissionType']
            for group in instance['groupPermissions']}
        new_group_permissions = {
            group['group']['id']: group['permissionType']
            for group in validated_data['groupPermissions']}

        (group_grant_read_permission, group_grant_write_permission,
         group_revoke_read_permission, group_revoke_write_permission) = \
            self._compute_all_revokes_and_grants(existing_group_permissions,
                                                 new_group_permissions)

        instance['_user_grant_read_permission'] = user_grant_read_permission
        instance['_user_grant_write_permission'] = user_grant_write_permission
        instance['_user_revoke_read_permission'] = user_revoke_read_permission
        instance['_user_revoke_write_permission'] = user_revoke_write_permission
        instance['_group_grant_read_permission'] = group_grant_read_permission
        instance['_group_grant_write_permission'] = group_grant_write_permission
        instance['_group_revoke_read_permission'] = group_revoke_read_permission
        instance['_group_revoke_write_permission'] = group_revoke_write_permission
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
        revoke_read_permission = []
        revoke_write_permission = []
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
            if ResourcePermissionType.READ in grants:
                grant_read_permission.append(id)
            if ResourcePermissionType.WRITE in grants:
                grant_write_permission.append(id)
        return (grant_read_permission, grant_write_permission,
                revoke_read_permission, revoke_write_permission)

    def _compute_revokes_and_grants(self, current_permission=None,
                                    new_permission=None):
        read_permissions = set((ResourcePermissionType.READ,))
        write_permissions = set((ResourcePermissionType.READ,
                                ResourcePermissionType.WRITE))
        current_permissions_set = set()
        new_permissions_set = set()
        if current_permission == ResourcePermissionType.READ:
            current_permissions_set = read_permissions
        elif current_permission == ResourcePermissionType.WRITE:
            current_permissions_set = write_permissions
        if new_permission == ResourcePermissionType.READ:
            new_permissions_set = read_permissions
        elif new_permission == ResourcePermissionType.WRITE:
            new_permissions_set = write_permissions

        # return tuple: permissions to revoke and permissions to grant
        return (current_permissions_set - new_permissions_set,
                new_permissions_set - current_permissions_set)

    def get_isOwner(self, shared_entity):
        request = self.context['request']
        return shared_entity['owner'].userId == request.user.username
