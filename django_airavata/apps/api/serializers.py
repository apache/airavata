from apache.airavata.model.workspace.ttypes import Project

from rest_framework import serializers



class ProjectSerializer(serializers.Serializer):
    projectID = serializers.CharField(required=True)
    name = serializers.CharField(required=True)
    owner = serializers.CharField(required=True)
    gatewayId = serializers.CharField(required=True)

    def create(self, validated_data):
        return Project(**validated_data)

    def update(self, instance, validated_data):
        raise Exception("Not implemented")