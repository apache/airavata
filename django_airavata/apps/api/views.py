
from . import serializers

from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.reverse import reverse

from django.conf import settings
from django.http import JsonResponse
from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt

# Create your views here.
@api_view(['GET'])
def api_root(request, format=None):
    return Response({
        'projects': reverse('api_project_list', request=request, format=format),
        'experiments': reverse('api_experiment_list', request=request, format=format)
    })

class ProjectList(APIView):
    def get(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        projects = request.airavata_client.getUserProjects(request.authz_token, gateway_id, username, -1, 0)
        serializer = serializers.ProjectSerializer(projects, many=True, context={'request': request})
        return Response(serializer.data)
    # TODO: add project creation

class ProjectDetail(APIView):
    def get(self, request, project_id, format=None):
        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        project = request.airavata_client.getProject(request.authz_token, project_id)
        serializer = serializers.ProjectSerializer(project, context={'request': request})
        return Response(serializer.data)
    # TODO: add project update (PUT)

class ExperimentList(APIView):
    def get(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        experiments = request.airavata_client.getUserExperiments(request.authz_token, gateway_id, username, -1, 0)
        serializer = serializers.ExperimentSerializer(experiments, many=True, context={'request': request})
        return Response(serializer.data)

class ProjectExperimentList(APIView):
    def get(self, request, project_id, format=None):
        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        experiments = request.airavata_client.getExperimentsInProject(request.authz_token, project_id, -1, 0)
        serializer = serializers.ExperimentSerializer(experiments, many=True, context={'request': request})
        return Response(serializer.data)
