
from . import serializers

from rest_framework import status, mixins, pagination
from rest_framework.decorators import api_view, detail_route
from rest_framework.views import APIView
from rest_framework.viewsets import GenericViewSet
from rest_framework.response import Response
from rest_framework.reverse import reverse
from rest_framework.parsers import JSONParser
from rest_framework.utils.urls import replace_query_param, remove_query_param

from django.conf import settings
from django.http import JsonResponse, Http404
from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt

from airavata.model.appcatalog.appdeployment.ttypes import ApplicationModule, ApplicationDeploymentDescription
from airavata.model.appcatalog.appinterface.ttypes import ApplicationInterfaceDescription

from collections import OrderedDict
import logging

log = logging.getLogger(__name__)

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



class ReadOnlyAPIBackedViewSet(mixins.RetrieveModelMixin,
                               mixins.ListModelMixin,
                               GenericAPIBackedViewSet):
    """
    A viewset that provides default `retrieve()` and `list()` actions.

    Subclasses must implement the following:
    * get_list(self)
    * get_instance(self, lookup_value)
    """
    pass


class APIBackedViewSet(mixins.CreateModelMixin,
                       mixins.RetrieveModelMixin,
                       mixins.UpdateModelMixin,
                       mixins.DestroyModelMixin,
                       mixins.ListModelMixin,
                       GenericAPIBackedViewSet):
    """
    A viewset that provides default `create()`, `retrieve()`, `update()`,
    `partial_update()`, `destroy()` and `list()` actions.

    Subclasses must implement the following:
    * get_list(self)
    * get_instance(self, lookup_value)
    * perform_create(self, serializer) - should return instance with id populated
    * perform_update(self, serializer)
    * perform_destroy(self, instance)
    """
    pass

class APIResultIterator(object):
    """
    Iterable container over API results which allow limit/offset style slicing.
    """

    limit = -1
    offset = 0

    def get_results(self, limit=-1, offset=0):
        raise NotImplementedError("Subclasses must implement get_results")

    def __iter__(self):
        results = self.get_results(self.limit, self.offset)
        for result in results:
            yield result

    def __getitem__(self, key):
        if isinstance(key, slice):
            self.limit = key.stop - key.start
            self.offset = key.start
            return iter(self)
        else:
            return self.get_results(1, key)

class APIResultPagination(pagination.LimitOffsetPagination):
    """
    Based on DRF's LimitOffsetPagination; Airavata API pagination results don't
    have a known count, so it isn't always possible to know how many pages there
    are.
    """
    def paginate_queryset(self, queryset, request, view=None):
        assert isinstance(queryset, APIResultIterator), "queryset is not an APIResultIterator: {}".format(queryset)
        self.limit = self.get_limit(request)
        if self.limit is None:
            return None

        self.offset = self.get_offset(request)
        self.request = request

        # When a paged view is called from another view (for example, to get the
        # initial data to display), this pagination class needs to know the name
        # of the view being paginated.
        if view and hasattr(view, 'pagination_viewname'):
            self.viewname = view.pagination_viewname

        return list(queryset[self.offset:self.offset + self.limit])

    def get_paginated_response(self, data):
        has_next_link = len(data) >= self.limit
        return Response(OrderedDict([
            ('next', self.get_next_link() if has_next_link else None),
            ('previous', self.get_previous_link()),
            ('results', data),
            ('limit', self.limit),
            ('offset', self.offset)
        ]))

    def get_next_link(self):
        url = self.get_base_url()
        url = replace_query_param(url, self.limit_query_param, self.limit)

        offset = self.offset + self.limit
        return replace_query_param(url, self.offset_query_param, offset)

    def get_previous_link(self):
        if self.offset <= 0:
            return None

        url = self.get_base_url()
        url = replace_query_param(url, self.limit_query_param, self.limit)

        if self.offset - self.limit <= 0:
            return remove_query_param(url, self.offset_query_param)

        offset = self.offset - self.limit
        return replace_query_param(url, self.offset_query_param, offset)

    def get_base_url(self):
        if hasattr(self, 'viewname'):
            return self.request.build_absolute_uri(reverse(self.viewname))
        else:
            return self.request.build_absolute_uri()

class ProjectViewSet(APIBackedViewSet):

    serializer_class = serializers.ProjectSerializer
    lookup_field = 'project_id'
    pagination_class = APIResultPagination
    pagination_viewname = 'django_airavata_api:project-list'

    def get_list(self):
        view = self
        class ProjectResultIterator(APIResultIterator):
            def get_results(self, limit=-1, offset=0):
                return view.request.airavata_client.getUserProjects(view.authz_token, view.gateway_id, view.username, limit, offset)
        return ProjectResultIterator()

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getProject(self.authz_token, lookup_value)

    def perform_create(self, serializer):
        project = serializer.save()
        project_id = self.request.airavata_client.createProject(self.authz_token, self.gateway_id, project)
        project.projectID = project_id

    def perform_update(self, serializer):
        project = serializer.save()
        self.request.airavata_client.updateProject(self.authz_token, project.projectID, project)


    @detail_route()
    def experiments(self, request, project_id=None):
        experiments = request.airavata_client.getExperimentsInProject(self.authz_token, project_id, -1, 0)
        serializer = serializers.ExperimentSerializer(experiments, many=True, context={'request': request})
        return Response(serializer.data)

# TODO: convert to ViewSet
class ExperimentList(APIView):
    def get(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        username = request.user.username

        experiments = request.airavata_client.getUserExperiments(request.authz_token, gateway_id, username, -1, 0)
        serializer = serializers.ExperimentSerializer(experiments, many=True, context={'request': request})
        return Response(serializer.data)


class ApplicationList(APIView):
    def get(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        app_modules = request.airavata_client.getAllAppModules(request.authz_token, gateway_id)
        serializer = serializers.ApplicationModuleSerializer(app_modules, many=True, context={'request': request})
        return Response(serializer.data)


class RegisterApplicationModule(APIView):
    parser_classes = (JSONParser,)

    def post(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        app_module = ApplicationModule(request.data['name'], request.data['version'], request.data['description'])
        response = request.airavata_client.registerApplicationModule(request.authz_token, gateway_id, app_module)
        return Response(response)


class RegisterApplicationInterface(APIView):
    parser_classes = (JSONParser,)

    def post(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        params = request.data
        app_interface_description_serializer = serializers.ApplicationInterfaceDescriptionSerializer(data=params)
        app_interface_description_serializer.is_valid(raise_exception=True)
        app_interface = app_interface_description_serializer.save()
        response = request.airavata_client.registerApplicationInterface(request.authz_token, gateway_id,
                                                                        applicationInterface=app_interface)
        return Response(response)


class RegisterApplicationDeployments(APIView):
    parser_classes = (JSONParser,)

    def post(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        params = request.data
        app_deployment = ApplicationDeploymentDescription(**params)
        response = request.airavata_client.registerApplicationDeployment(request.authz_token, gateway_id,
                                                                         app_deployment)
        return Response(response)

class ApplicationInterfaceViewSet(APIBackedViewSet):

    serializer_class = serializers.ApplicationInterfaceDescriptionSerializer
    lookup_field = 'app_interface_id'

    def get_list(self):
        return self.request.airavata_client.getAllApplicationInterfaces(self.authz_token, self.gateway_id)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getApplicationInterface(self.authz_token, lookup_value)

    def perform_create(self, serializer):
        application_interface = serializer.save()
        log.debug("application_interface: {}".format(application_interface))
        app_interface_id = self.request.airavata_client.registerApplicationInterface(self.authz_token, self.gateway_id, application_interface)
        application_interface.applicationInterfaceId = app_interface_id

    def perform_update(self, serializer):
        application_interface = serializer.save()
        self.request.airavata_client.updateApplicationInterface(self.authz_token, application_interface.applicationInterfaceId, application_interface)
