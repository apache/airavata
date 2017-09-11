from apache.airavata.model.experiment.ttypes import ExperimentModel
from apache.airavata.model.workspace.ttypes import Project

from rest_framework import serializers

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


class ProjectSerializer(serializers.Serializer):
    projectID = serializers.CharField(required=True)
    name = serializers.CharField(required=True)
    owner = serializers.CharField(required=True)
    gatewayId = serializers.CharField(required=True)
    experiments = FullyEncodedHyperlinkedIdentityField(view_name='api_project_experiments_list', lookup_field='projectID', lookup_url_kwarg='project_id')

    # TODO: maybe just have a get() method to get the deserialized object?
    def create(self, validated_data):
        return Project(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")


class ExperimentSerializer(serializers.Serializer):

    experimentId = serializers.CharField(required=True)
    projectId = serializers.CharField(required=True)
    project = FullyEncodedHyperlinkedIdentityField(view_name='api_project_detail', lookup_field='projectId', lookup_url_kwarg='project_id')
    gatewayId = serializers.CharField(required=True)
    experimentType = serializers.CharField(required=True)
    userName = serializers.CharField(required=True)
    experimentName = serializers.CharField(required=True)

    def create(self, validated_data):
        return ExperimentModel(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")
