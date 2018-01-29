
from airavata.model.appcatalog.appdeployment.ttypes import ApplicationModule, ApplicationDeploymentDescription,CommandObject,SetEnvPaths
from airavata.model.appcatalog.appinterface.ttypes import ApplicationInterfaceDescription
from airavata.model.appcatalog.computeresource.ttypes import BatchQueue
from airavata.model.application.io.ttypes import InputDataObjectType, OutputDataObjectType
from airavata.model.data.replica.ttypes import DataProductModel, DataReplicaLocationModel
from airavata.model.experiment.ttypes import ExperimentModel
from airavata.model.job.ttypes import JobModel
from airavata.model.status.ttypes import ExperimentStatus
from airavata.model.workspace.ttypes import Project

from . import thrift_utils

from django.conf import settings

from rest_framework import serializers

import datetime
import copy
from urllib.parse import quote
import logging


log = logging.getLogger(__name__)


class FullyEncodedHyperlinkedIdentityField(serializers.HyperlinkedIdentityField):
    def get_url(self, obj, view_name, request, format):
        lookup_value = getattr(obj, self.lookup_field)
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


class GatewayUsernameDefaultField(serializers.CharField):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.read_only = True
        self.default = GetGatewayUsername()


class GatewayIdDefaultField(serializers.CharField):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.read_only = True
        self.default = settings.GATEWAY_ID


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
    metaData = serializers.CharField(required=False)
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


class ComputeResourceDescriptionSerializer(CustomSerializer):
    hostName=serializers.CharField()
    hostAliases=serializers.ListField(child=serializers.CharField())
    ipAddresses=serializers.ListField(child=serializers.CharField())
    resourceDescription=serializers.CharField()
    enabled=serializers.BooleanField()


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
