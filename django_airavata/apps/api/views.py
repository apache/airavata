
from . import serializers

from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response

from django.conf import settings
from django.http import JsonResponse
from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt

# Create your views here.
class ProjectList(APIView):
    def get(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        projects = request.airavata_client.getUserProjects(request.authz_token, gateway_id, username, -1, 0)
        serializer = serializers.ProjectSerializer(projects, many=True)
        return Response(serializer.data)
    # TODO: add project creation

class ProjectDetail(APIView):
    def get(self, request, project_id, format=None):
        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        project = request.airavata_client.getProject(request.authz_token, project_id)
        serializer = serializers.ProjectSerializer(project)
        return Response(serializer.data)
    # TODO: add project update (PUT)
