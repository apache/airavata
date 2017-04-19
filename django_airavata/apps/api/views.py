
from . import serializers

from django.conf import settings
from django.http import JsonResponse
from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt

# Create your views here.
@csrf_exempt
def project_list(request):
    if request.method == 'GET':

        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        projects = request.airavata_client.getUserProjects(request.authz_token, gateway_id, username, -1, 0)
        serializer = serializers.ProjectSerializer(projects, many=True)
        return JsonResponse(serializer.data, safe=False)

@csrf_exempt
def project_detail(request, project_id):
    if request.method == 'GET':

        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        project = request.airavata_client.getProject(request.authz_token, project_id)
        serializer = serializers.ProjectSerializer(project)
        return JsonResponse(serializer.data)
