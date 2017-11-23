from abc import ABCMeta

from apache.airavata.model.experiment.ttypes import ExperimentModel
from apache.airavata.model.workspace.ttypes import Project
from apache.airavata.model.appcatalog.appdeployment.ttypes import ApplicationModule
from apache.airavata.model.appcatalog.appinterface.ttypes import ApplicationInterfaceDescription
from apache.airavata.model.appcatalog.appdeployment.ttypes import ApplicationDeploymentDescription,CommandObject,SetEnvPaths
from apache.airavata.model.application.io.ttypes import InputDataObjectType, OutputDataObjectType
from apache.airavata.model.experiment.ttypes import ExperimentModel
from apache.airavata.model.workspace.ttypes import Project
from apache.airavata.model.appcatalog.appdeployment.ttypes import ApplicationModule
from django.conf import settings

from rest_framework import serializers

import datetime
import copy
from urllib.parse import quote


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
    def to_representation(self, obj):
        dt = datetime.datetime.utcfromtimestamp(obj/1000)
        return super().to_representation(dt)

    def to_internal_value(self, data):
        dt = super().to_internal_value(data)
        return int(dt.timestamp() * 1000)


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
    projectID = serializers.CharField(read_only=True)
    name = serializers.CharField(required=True)
    description = serializers.CharField(required=False)
    owner = GatewayUsernameDefaultField()
    gatewayId = GatewayIdDefaultField()
    experiments = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:project-experiments', lookup_field='projectID', lookup_url_kwarg='project_id')
    creationTime = UTCPosixTimestampDateTimeField()

    def create(self, validated_data):
        return Project(**validated_data)

    def update(self, instance, validated_data):
        instance.name = validated_data.get('name', instance.name)
        instance.description = validated_data.get('description', instance.description)
        return instance


class ExperimentSerializer(serializers.Serializer):

    experimentId = serializers.CharField(read_only=True)
    projectId = serializers.CharField(required=True)
    project = FullyEncodedHyperlinkedIdentityField(view_name='django_airavata_api:project-detail', lookup_field='projectId', lookup_url_kwarg='project_id')
    gatewayId = GatewayIdDefaultField()
    experimentType = serializers.CharField(required=True)
    userName = GatewayUsernameDefaultField()
    experimentName = serializers.CharField(required=True)

    def create(self, validated_data):
        return ExperimentModel(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class ApplicationModuleSerializer(serializers.Serializer):
    appModuleId = serializers.CharField(required=True)
    appModuleName = serializers.CharField(required=True)
    appModuleDescription = serializers.CharField()
    appModuleVersion = serializers.CharField()


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


class ApplicationDeploymentDescriptionSerializer(CustomSerializer):
    appModuleId = serializers.CharField(required=False)
    computeHostId = serializers.CharField(required=False)
    executablePath = serializers.CharField(required=False)
    parallelism = serializers.IntegerField(required=False)
    appDeploymentDescription = serializers.CharField(required=False)
    moduleLoadCmds = serializers.ListSerializer(child=CommandObjectSerializer())
    libPrependPaths = serializers.ListSerializer(child=SetEnvPathsSerializer())
    libAppendPaths = serializers.ListSerializer(child=SetEnvPathsSerializer())
    setEnvironment = serializers.ListSerializer(child=SetEnvPathsSerializer())
    preJobCommands = serializers.ListSerializer(child=CommandObjectSerializer())
    postJobCommands = serializers.ListSerializer(child=CommandObjectSerializer())
    defaultQueueName = serializers.CharField(required=False)
    defaultNodeCount = serializers.IntegerField(required=False)
    defaultCPUCount = serializers.IntegerField(required=False)
    defaultWalltime = serializers.IntegerField(required=False)
    editableByUser = serializers.BooleanField(required=False)

    def create(self, validated_data):
        params=self.process_list_fields(validated_data)
        return ApplicationDeploymentDescription(**params)

    def update(self, instance, validated_data):
        raise Exception("Not Implemented")



class ComputeResourceDescriptionSerializer(CustomSerializer):
    hostName=serializers.CharField()
    hostAliases=serializers.ListField(child=serializers.CharField())
    ipAddresses=serializers.ListField(child=serializers.CharField())
    resourceDescription=serializers.CharField()
    enabled=serializers.BooleanField()
