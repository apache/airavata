
from . import serializers

from rest_framework import status, mixins
from rest_framework.decorators import api_view
from rest_framework.views import APIView
from rest_framework.viewsets import GenericViewSet
from rest_framework.response import Response
from rest_framework.reverse import reverse

from django.conf import settings
from django.http import JsonResponse, Http404
from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt

# Create your views here.
@api_view(['GET'])
def api_root(request, format=None):
    return Response({
        'projects': reverse('project-list', request=request, format=format),
        'admin': reverse('api_experiment_list', request=request, format=format)
    })

class GenericAPIBackedViewSet(GenericViewSet):

    def get_list(self):
        """
        Subclasses must implement.
        """
        raise NotImplementedError()

    def get_instance(self, lookup_value):
        """
        Subclasses must implement.
        """
        raise NotImplementedError()

    def get_queryset(self):
        return self.get_list()

    def get_object(self):
        lookup_url_kwarg = self.lookup_url_kwarg or self.lookup_field
        lookup_value = self.kwargs[lookup_url_kwarg]
        inst = self.get_instance(lookup_value)
        if inst is None:
            raise Http404
        self.check_object_permissions(self.request, inst)
        return inst

    @property
    def username(self):
        return self.request.user.username

    @property
    def gateway_id(self):
        return settings.GATEWAY_ID

    @property
    def authz_token(self):
        return self.request.authz_token

class CreateUpdateRetrieveListViewSet(mixins.CreateModelMixin,
                                      mixins.RetrieveModelMixin,
                                      mixins.UpdateModelMixin,
                                      mixins.DestroyModelMixin,
                                      mixins.ListModelMixin,
                                      GenericAPIBackedViewSet):
    """
    A viewset that provides default `create()`, `retrieve()`, `update()`,
    `partial_update()` and `list()` actions.

    Subclasses must implement the following:
    * get_list(self)
    * get_instance(self, lookup_value)
    * perform_create(self, serializer) - should return instance with id populated
    * perform_update(self, serializer)
    """
    pass


class ProjectViewSet(CreateUpdateRetrieveListViewSet):

    serializer_class = serializers.ProjectSerializer

    def get_list(self):
        # TODO: support pagination
        return self.request.airavata_client.getUserProjects(self.authz_token, self.gateway_id, self.username, -1, 0)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getProject(self.authz_token, lookup_value)

    def perform_create(self, serializer):
        project = serializer.save()
        project_id = self.request.airavata_client.createProject(self.authz_token, self.gateway_id, project)
        project.projectID = project_id

    def perform_update(self, serializer):
        project = serializer.save()
        self.request.airavata_client.updateProject(self.authz_token, project.projectID, project)


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


class ApplicationList(APIView):

    def get(self,request,format=None):
        gateway_id = settings.GATEWAY_ID
        app_modules=request.airavata_client.getAllAppModules(request.authz_token,gateway_id)
        serializer=serializers.ApplicationModuleSerializer(app_modules,many=True,context={'request':request})
        return Response(serializer.data)

class RegisterApplicationInterface(APIView):

    def post(self,request,format=None):
        gateway_id = settings.GATEWAY_ID

