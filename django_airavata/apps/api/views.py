
from . import serializers
from . import thrift_utils

from rest_framework import status, mixins, pagination
from rest_framework.decorators import api_view
from rest_framework.decorators import detail_route
from rest_framework.decorators import list_route
from rest_framework.views import APIView
from rest_framework.viewsets import GenericViewSet
from rest_framework.response import Response
from rest_framework.reverse import reverse
from rest_framework.parsers import JSONParser
from rest_framework.renderers import JSONRenderer
from rest_framework.utils.urls import replace_query_param, remove_query_param
from rest_framework import status

from django.conf import settings
from django.core.files.storage import FileSystemStorage
from django.http import JsonResponse, Http404
from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt

from airavata.model.appcatalog.appdeployment.ttypes import ApplicationModule, ApplicationDeploymentDescription
from airavata.model.appcatalog.appinterface.ttypes import ApplicationInterfaceDescription
from airavata.model.appcatalog.computeresource.ttypes import ComputeResourceDescription
from airavata.model.credential.store.ttypes import CredentialOwnerType,SummaryType,CredentialSummary
from airavata.model.application.io.ttypes import DataType

from collections import OrderedDict
import logging
import os

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
    default_limit = 10

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

class GroupViewSet(APIBackedViewSet):

    serializer_class = serializers.GroupSerializer
    lookup_field = 'group_id'

    def get_list(self):
        return self.request.profile_service['group_manager'].getGroups(self.authz_token)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getGroup(self.authz_token, self.gateway_id, lookup_value)

    def perform_create(self, serializer):
        group = serializer.save()
        group_id = self.request.profile_service['group_manager'].createGroup(self.authz_token, group)
        group.groupID = group_id

    def perform_update(self, serializer):
        group = serializer.save()
        self.request.airavata_client.updateGroup(self.authz_token, group)


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

    @list_route()
    def list_all(self, request):
        projects = self.request.airavata_client.getUserProjects(self.authz_token, self.gateway_id, self.username, -1, 0)
        serializer = serializers.ProjectSerializer(projects, many=True, context={'request': request})
        return Response(serializer.data)

    @detail_route()
    def experiments(self, request, project_id=None):
        experiments = request.airavata_client.getExperimentsInProject(self.authz_token, project_id, -1, 0)
        serializer = serializers.ExperimentSerializer(experiments, many=True, context={'request': request})
        return Response(serializer.data)


class ExperimentViewSet(APIBackedViewSet):

    serializer_class = serializers.ExperimentSerializer
    lookup_field = 'experiment_id'

    def get_list(self):
        return self.request.airavata_client.getUserExperiments(self.authz_token, self.gateway_id, self.username, -1, 0)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getExperiment(self.authz_token, lookup_value)

    def perform_create(self, serializer):
        experiment = serializer.save()
        experiment.userConfigurationData.storageId =\
            settings.GATEWAY_DATA_STORE_RESOURCE_ID
        # Set the experimentDataDir
        # TODO: move this to a common code location
        project = self.request.airavata_client.getProject(
            self.authz_token, experiment.projectId)
        experiment_data_storage = FileSystemStorage(
            location=settings.GATEWAY_DATA_STORE_DIR)
        exp_dir = os.path.join(
            settings.GATEWAY_DATA_STORE_DIR,
            experiment_data_storage.get_valid_name(self.username),
            experiment_data_storage.get_valid_name(project.name),
            experiment_data_storage.get_valid_name(experiment.experimentName))
        experiment.userConfigurationData.experimentDataDir = exp_dir
        experiment_id = self.request.airavata_client.createExperiment(
            self.authz_token, self.gateway_id, experiment)
        experiment.experimentId = experiment_id

    def perform_update(self, serializer):
        experiment = serializer.save()
        self.request.airavata_client.updateExperiment(self.authz_token, experiment.experimentId, experiment)

    @detail_route(methods=['post'])
    def launch(self, request, experiment_id=None):
        try:
            request.airavata_client.launchExperiment(request.authz_token, experiment_id, self.gateway_id)
            return Response({'success': True})
        except Exception as e:
            return Response({'success': False, 'errorMessage': e.message})

    @detail_route(methods=['get'])
    def jobs(self, request, experiment_id=None):
        jobs = request.airavata_client.getJobDetails(
            self.authz_token, experiment_id)
        serializer = serializers.JobSerializer(
            jobs, many=True, context={'request': request})
        return Response(serializer.data)


class ExperimentSearchViewSet(mixins.ListModelMixin, GenericAPIBackedViewSet):

    serializer_class = serializers.ExperimentSummarySerializer
    pagination_class = APIResultPagination
    pagination_viewname = 'django_airavata_api:experiment-search-list'

    def get_list(self):
        view = self

        # TODO: implement support for filters
        class ExperimentSearchResultIterator(APIResultIterator):
            def get_results(self, limit=-1, offset=0):
                return view.request.airavata_client.searchExperiments(
                    view.authz_token, view.gateway_id, view.username, {},
                    limit, offset)
        return ExperimentSearchResultIterator()

    def get_instance(self, lookup_value):
        raise NotImplementedError()


class FullExperimentViewSet(mixins.RetrieveModelMixin,
                            GenericAPIBackedViewSet):

    serializer_class = serializers.FullExperimentSerializer
    lookup_field = 'experiment_id'

    def get_instance(self, lookup_value):
        """Get FullExperiment instance with resolved references."""
        # TODO: move loading experiment and references to airavata_sdk?
        experimentModel = self.request.airavata_client.getExperiment(
            self.authz_token, lookup_value)
        outputDataProducts = [
            self.request.airavata_client.getDataProduct(self.authz_token,
                                                        output.value)
            for output in experimentModel.experimentOutputs
            if (output.value.startswith('airavata-dp')
                and output.type in (DataType.URI,
                                    DataType.STDOUT,
                                    DataType.STDERR))]
        inputDataProducts = [
            self.request.airavata_client.getDataProduct(self.authz_token,
                                                        inp.value)
            for inp in experimentModel.experimentInputs
            if (inp.value.startswith('airavata-dp')
                and inp.type in (DataType.URI,
                                 DataType.STDOUT,
                                 DataType.STDERR))]
        appInterfaceId = experimentModel.executionId
        applicationInterface = self.request.airavata_client\
            .getApplicationInterface(self.authz_token, appInterfaceId)
        appModuleId = applicationInterface.applicationModules[0]
        applicationModule = self.request.airavata_client\
            .getApplicationModule(self.authz_token, appModuleId)
        compute_resource_id = None
        user_conf = experimentModel.userConfigurationData
        if user_conf and user_conf.computationalResourceScheduling:
            comp_res_sched = user_conf.computationalResourceScheduling
            compute_resource_id = comp_res_sched.resourceHostId
        compute_resource = self.request.airavata_client.getComputeResource(
            self.authz_token, compute_resource_id)\
            if compute_resource_id else None
        project = self.request.airavata_client.getProject(
            self.authz_token, experimentModel.projectId)
        job_details = self.request.airavata_client.getJobDetails(
            self.authz_token, lookup_value)
        full_experiment = serializers.FullExperiment(
            experimentModel,
            project=project,
            outputDataProducts=outputDataProducts,
            inputDataProducts=inputDataProducts,
            applicationModule=applicationModule,
            computeResource=compute_resource,
            jobDetails=job_details)
        return full_experiment


class ApplicationModuleViewSet(APIBackedViewSet):
    serializer_class = serializers.ApplicationModuleSerializer
    lookup_field = 'app_module_id'

    def get_list(self):
        return self.request.airavata_client.getAllAppModules(self.authz_token, self.gateway_id)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getApplicationModule(self.authz_token, lookup_value)

    def perform_create(self, serializer):
        app_module = serializer.save()
        app_module_id = self.request.airavata_client.registerApplicationModule(self.authz_token, self.gateway_id, app_module)
        app_module.appModuleId = app_module_id

    def perform_update(self, serializer):
        app_module = serializer.save()
        self.request.airavata_client.updateApplicationModule(self.authz_token, app_module.appModuleId, app_module)

    @detail_route()
    def application_interface(self, request, app_module_id):
        all_app_interfaces = request.airavata_client.getAllApplicationInterfaces(self.authz_token, self.gateway_id)
        app_interfaces = []
        for app_interface in all_app_interfaces:
            if not app_interface.applicationModules:
                continue
            if app_module_id in app_interface.applicationModules:
                app_interfaces.append(app_interface)
        if len(app_interfaces) == 1:
            serializer = thrift_utils.create_serializer(
                ApplicationInterfaceDescription, instance=app_interfaces[0], context={'request': request})
            return Response(serializer.data)
        elif len(app_interfaces) > 1:
            log.error("More than one application interface found for module {}: {}".format(app_module_id, app_interfaces))
            return Response({'error': 'More than one application interface found for module id {}'.format(app_module_id)},
                            status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        else:
            return Response({'error': 'No application interface found for module id {}'.format(app_module_id)},
                            status=status.HTTP_404_NOT_FOUND)

    @detail_route()
    def application_deployments(self, request, app_module_id):
        all_deployments = self.request.airavata_client.getAllApplicationDeployments(self.authz_token, self.gateway_id)
        app_deployments = [dep for dep in all_deployments if dep.appModuleId == app_module_id]
        serializer = serializers.ApplicationDeploymentDescriptionSerializer(app_deployments, many=True, context={'request': request})
        return Response(serializer.data)


# TODO convert to APIBackedViewSet
class RegisterApplicationModule(APIView):
    parser_classes = (JSONParser,)

    def post(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        app_module = ApplicationModule(request.data['name'], request.data['version'], request.data['description'])
        response = request.airavata_client.registerApplicationModule(request.authz_token, gateway_id, app_module)
        return Response(response)


# TODO use ApplicationInterfaceViewSet instead
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


# TODO convert to APIBackedViewSet
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

    @detail_route()
    def compute_resources(self, request, app_interface_id):
        compute_resources = request.airavata_client.getAvailableAppInterfaceComputeResources(self.authz_token, app_interface_id)
        return Response(compute_resources)

class ApplicationDeploymentViewSet(APIBackedViewSet):

    serializer_class = serializers.ApplicationDeploymentDescriptionSerializer
    lookup_field = 'app_deployment_id'
    lookup_value_regex = '[^/]+'

    def get_list(self):
        return self.request.airavata_client.getAllApplicationDeployments(self.authz_token, self.gateway_id)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getApplicationDeployment(self.authz_token, lookup_value)

    def perform_create(self, serializer):
        application_deployment = serializer.save()
        app_deployment_id = self.request.airavata_client.registerApplicationDeployment(self.authz_token, self.gateway_id, application_deployment)
        application_deployment.appDeploymentId = app_deployment_id

    def perform_update(self, serializer):
        application_deployment = serializer.save()
        self.request.airavata_client.updateApplicationDeployment(self.authz_token, application_deployment.appDeploymentId, application_deployment)

    @detail_route()
    def queues(self, request, app_deployment_id):
        """Return queues for this deployment with defaults overridden by deployment defaults if they exist"""
        app_deployment = self.request.airavata_client.getApplicationDeployment(self.authz_token, app_deployment_id)
        compute_resource = request.airavata_client.getComputeResource(request.authz_token, app_deployment.computeHostId)
        # Override defaults with app deployment defaults
        batch_queues = []
        for batch_queue in compute_resource.batchQueues:
            if app_deployment.defaultQueueName:
                batch_queue.isDefaultQueue = (app_deployment.defaultQueueName == batch_queue.queueName)
            if app_deployment.defaultNodeCount:
                batch_queue.defaultNodeCount = app_deployment.defaultNodeCount
            if app_deployment.defaultCPUCount:
                batch_queue.defaultCPUCount = app_deployment.defaultCPUCount
            if app_deployment.defaultWalltime:
                batch_queue.defaultWalltime = app_deployment.defaultWalltime
            batch_queues.append(batch_queue)
        serializer = serializers.BatchQueueSerializer(batch_queues, many=True, context={'request': request})
        return Response(serializer.data)

class ComputeResourceList(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        cr = request.airavata_client.getAllComputeResourceNames(request.authz_token)

        return Response([{'host_id': host_id, 'host': host} for host_id, host in cr.items()])


class ComputeResourceDetails(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        details = request.airavata_client.getComputeResource(request.authz_token, request.query_params["id"])
        serializer = thrift_utils.create_serializer(ComputeResourceDescription, instance=details,
                                                                context={'request': request})
        print(details)
        return Response(serializer.data)


class ComputeResourcesQueues(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        details = request.airavata_client.getComputeResource(request.authz_token, request.query_params["id"])
        serializer = thrift_utils.create_serializer(ComputeResourceDescription, instance=details,
                                                                context={'request': request})
        data = serializer.data
        return Response([queue["queueName"] for queue in data["batchQueues"]])


class ApplicationInterfaceList(APIView):
    def get(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        serializer = thrift_utils.create_serializer(ApplicationInterfaceDescription,
                                                                instance=request.airavata_client.getAllApplicationInterfaces(
                                                                    request.authz_token, gateway_id),
                                                                context={'request': request},many=True)
        return Response(serializer.data)

class FetchApplicationInterface(APIView):


    def get(self,request,format=None):
        gateway_id = settings.GATEWAY_ID
        for app_interface in request.airavata_client.getAllApplicationInterfaces(
                                                                    request.authz_token, gateway_id):
            app_modules=app_interface.applicationModules
            if request.query_params["id"] in app_modules:
                return Response(thrift_utils.create_serializer(ApplicationInterfaceDescription,
                                                                instance=app_interface,
                                                                context={'request': request}).data)
        return Response(status=status.HTTP_400_BAD_REQUEST)


class FetchApplicationDeployment(APIView):

    def get(self,request,format=None):
        gateway_id = settings.GATEWAY_ID
        app_deployments=[app_deployment for app_deployment in  request.airavata_client.getAllApplicationDeployments(
                request.authz_token, gateway_id) if request.query_params["id"] == app_deployment.appModuleId]
        serializer=thrift_utils.create_serializer(ApplicationDeploymentDescription,
                                                   instance=app_deployments,
                                                   context={'request': request},many=True)
        return Response(serializer.data)
        #return Response(request.airavata_client.getAppModuleDeployedResources(request.authz_token, request.query_params["id"]))

class FetchSSHPubKeys(APIView):

    def get(self,request,format=None):
        gateway_id = settings.GATEWAY_ID
        serializer=thrift_utils.create_serializer(CredentialSummary,instance=request.airavata_client.getAllCredentialSummaryForGateway (request.authz_token,SummaryType.SSH,gateway_id),context={'request': request},many=True)
        return Response(serializer.data)

class GenerateRegisterSSHKeys(APIView):
    parser_classes = (JSONParser,)
    renderer_classes = (JSONRenderer,)

    def post(self, request, format=None):
        username = request.user.username
        gateway_id = settings.GATEWAY_ID
        data=request.data
        return Response(request.airavata_client.generateAndRegisterSSHKeys (request.authz_token,gateway_id,username,data["description"],CredentialOwnerType.GATEWAY))


class DeleteSSHPubKey(APIView):
    parser_classes = (JSONParser,)
    renderer_classes = (JSONRenderer,)

    def post(self, request, format=None):
        gateway_id = settings.GATEWAY_ID
        return Response(request.airavata_client.deleteSSHPubKey(request.authz_token,request.data['token'],gateway_id))
