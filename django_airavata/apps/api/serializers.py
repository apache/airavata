from apache.airavata.model.experiment.ttypes import ExperimentModel
from apache.airavata.model.workspace.ttypes import Project

from rest_framework import serializers



class ProjectSerializer(serializers.Serializer):
    projectID = serializers.CharField(required=True)
    name = serializers.CharField(required=True)
    owner = serializers.CharField(required=True)
    gatewayId = serializers.CharField(required=True)
    experiments = serializers.HyperlinkedIdentityField(view_name='api_project_experiments_list', lookup_field='projectID', lookup_url_kwarg='project_id')

    def create(self, validated_data):
        return Project(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")

class ExperimentSerializer(serializers.Serializer):

    experimentId = serializers.CharField(required=True)
    projectId = serializers.CharField(required=True)
    project = serializers.HyperlinkedIdentityField(view_name='api_project_detail', lookup_field='projectId', lookup_url_kwarg='project_id')
    gatewayId = serializers.CharField(required=True)
    experimentType = serializers.CharField(required=True)
    userName = serializers.CharField(required=True)
    experimentName = serializers.CharField(required=True)

    def create(self, validated_data):
        return ExperimentModel(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")
