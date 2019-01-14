import logging

from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.core.exceptions import ObjectDoesNotExist
from django.http import FileResponse, Http404, JsonResponse
from django.urls import reverse
from rest_framework import mixins
from rest_framework.decorators import action, detail_route, list_route
from rest_framework.exceptions import ParseError
from rest_framework.renderers import JSONRenderer
from rest_framework.response import Response
from rest_framework.views import APIView

from airavata.api.error.ttypes import ProjectNotFoundException
from airavata.model.appcatalog.computeresource.ttypes import (
    CloudJobSubmission,
    GlobusJobSubmission,
    LOCALSubmission,
    SSHJobSubmission,
    UnicoreJobSubmission
)
from airavata.model.application.io.ttypes import DataType
from airavata.model.credential.store.ttypes import SummaryType
from airavata.model.data.movement.ttypes import (
    GridFTPDataMovement,
    LOCALDataMovement,
    SCPDataMovement,
    UnicoreDataMovement
)
from airavata.model.group.ttypes import ResourcePermissionType
from django_airavata.apps.api.view_utils import (
    APIBackedViewSet,
    APIResultIterator,
    APIResultPagination,
    GenericAPIBackedViewSet
)

from . import datastore, serializers, thrift_utils

READ_PERMISSION_TYPE = '{}:READ'

log = logging.getLogger(__name__)


class GroupViewSet(APIBackedViewSet):
    serializer_class = serializers.GroupSerializer
    lookup_field = 'group_id'
    pagination_class = APIResultPagination
    pagination_viewname = 'django_airavata_api:group-list'

    def get_list(self):
        view = self

        class GroupResultsIterator(APIResultIterator):
            def get_results(self, limit=-1, offset=0):
                groups = view.request.profile_service['group_manager'].getGroups(
                    view.authz_token)
                return groups[offset:offset + limit] if groups else []

        return GroupResultsIterator()

    def get_instance(self, lookup_value):
        return self.request.profile_service['group_manager'].getGroup(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        group = serializer.save()
        group_id = self.request.profile_service['group_manager'].createGroup(
            self.authz_token, group)
        group.id = group_id

    def perform_update(self, serializer):
        group = serializer.save()
        group_manager_client = self.request.profile_service['group_manager']
        if len(group._added_members) > 0:
            group_manager_client.addUsersToGroup(
                self.authz_token, group._added_members, group.id)
        if len(group._removed_members) > 0:
            group_manager_client.removeUsersFromGroup(
                self.authz_token, group._removed_members, group.id)
        if len(group._added_admins) > 0:
            group_manager_client.addGroupAdmins(
                self.authz_token, group.id, group._added_admins)
        if len(group._removed_admins) > 0:
            group_manager_client.removeGroupAdmins(
                self.authz_token, group.id, group._removed_admins)
        group_manager_client.updateGroup(self.authz_token, group)

    def perform_destroy(self, group):
        group_manager_client = self.request.profile_service['group_manager']
        group_manager_client.deleteGroup(
            self.authz_token, group.id, group.ownerId)


class ProjectViewSet(APIBackedViewSet):
    serializer_class = serializers.ProjectSerializer
    lookup_field = 'project_id'
    pagination_class = APIResultPagination
    pagination_viewname = 'django_airavata_api:project-list'

    def get_list(self):
        view = self

        class ProjectResultIterator(APIResultIterator):
            def get_results(self, limit=-1, offset=0):
                return view.request.airavata_client.getUserProjects(
                    view.authz_token, view.gateway_id, view.username, limit, offset)

        return ProjectResultIterator()

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getProject(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        project = serializer.save(
            owner=self.username,
            gatewayId=self.gateway_id)
        project_id = self.request.airavata_client.createProject(
            self.authz_token, self.gateway_id, project)
        project.projectID = project_id

    def perform_update(self, serializer):
        project = serializer.save()
        self.request.airavata_client.updateProject(
            self.authz_token, project.projectID, project)

    @list_route()
    def list_all(self, request):
        projects = self.request.airavata_client.getUserProjects(
            self.authz_token, self.gateway_id, self.username, -1, 0)
        serializer = serializers.ProjectSerializer(
            projects, many=True, context={'request': request})
        return Response(serializer.data)

    @detail_route()
    def experiments(self, request, project_id=None):
        experiments = request.airavata_client.getExperimentsInProject(
            self.authz_token, project_id, -1, 0)
        serializer = serializers.ExperimentSerializer(
            experiments, many=True, context={'request': request})
        return Response(serializer.data)


class ExperimentViewSet(APIBackedViewSet):
    serializer_class = serializers.ExperimentSerializer
    lookup_field = 'experiment_id'

    def get_list(self):
        return self.request.airavata_client.getUserExperiments(
            self.authz_token, self.gateway_id, self.username, -1, 0)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getExperiment(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        experiment = serializer.save(
            gatewayId=self.gateway_id,
            userName=self.username)
        self._set_storage_id_and_data_dir(experiment)
        experiment_id = self.request.airavata_client.createExperiment(
            self.authz_token, self.gateway_id, experiment)
        experiment.experimentId = experiment_id

    def perform_update(self, serializer):
        experiment = serializer.save(
            gatewayId=self.gateway_id,
            userName=self.username)
        # The project or exp name may have changed, so update the exp data dir
        self._set_storage_id_and_data_dir(experiment)
        self.request.airavata_client.updateExperiment(
            self.authz_token, experiment.experimentId, experiment)
        # Process experiment._removed_input_files, removing them from storage
        for removed_input_file in experiment._removed_input_files:
            data_product = self.request.airavata_client.getDataProduct(
                self.authz_token, removed_input_file)
            datastore.delete(data_product)

    def _set_storage_id_and_data_dir(self, experiment):
        # Storage ID
        experiment.userConfigurationData.storageId = \
            settings.GATEWAY_DATA_STORE_RESOURCE_ID
        # Create experiment dir and set it on model
        project = self.request.airavata_client.getProject(
            self.authz_token, experiment.projectId)
        exp_dir = datastore.get_experiment_dir(self.username,
                                               project.name,
                                               experiment.experimentName)
        experiment.userConfigurationData.experimentDataDir = exp_dir

    @detail_route(methods=['post'])
    def launch(self, request, experiment_id=None):
        try:
            request.airavata_client.launchExperiment(
                request.authz_token, experiment_id, self.gateway_id)
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
            if (output.value and
                output.value.startswith('airavata-dp') and
                output.type in (DataType.URI,
                                DataType.STDOUT,
                                DataType.STDERR))]
        inputDataProducts = [
            self.request.airavata_client.getDataProduct(self.authz_token,
                                                        inp.value)
            for inp in experimentModel.experimentInputs
            if (inp.value and
                inp.value.startswith('airavata-dp') and
                inp.type in (DataType.URI,
                             DataType.STDOUT,
                             DataType.STDERR))]
        appInterfaceId = experimentModel.executionId
        applicationInterface = self.request.airavata_client \
            .getApplicationInterface(self.authz_token, appInterfaceId)
        appModuleId = applicationInterface.applicationModules[0]
        applicationModule = self.request.airavata_client \
            .getApplicationModule(self.authz_token, appModuleId)
        compute_resource_id = None
        user_conf = experimentModel.userConfigurationData
        if user_conf and user_conf.computationalResourceScheduling:
            comp_res_sched = user_conf.computationalResourceScheduling
            compute_resource_id = comp_res_sched.resourceHostId
        compute_resource = self.request.airavata_client.getComputeResource(
            self.authz_token, compute_resource_id) \
            if compute_resource_id else None
        try:
            project = self.request.airavata_client.getProject(
                self.authz_token, experimentModel.projectId)
        except ProjectNotFoundException as pnfe:
            # User may not have access to project, only experiment
            project = None
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
        return self.request.airavata_client.getAccessibleAppModules(
            self.authz_token, self.gateway_id)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getApplicationModule(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        app_module = serializer.save()
        app_module_id = self.request.airavata_client.registerApplicationModule(
            self.authz_token, self.gateway_id, app_module)
        app_module.appModuleId = app_module_id

    def perform_update(self, serializer):
        app_module = serializer.save()
        self.request.airavata_client.updateApplicationModule(
            self.authz_token, app_module.appModuleId, app_module)

    def perform_destroy(self, instance):
        self.request.airavata_client.deleteApplicationModule(
            self.authz_token, instance.appModuleId)

    @detail_route()
    def application_interface(self, request, app_module_id):
        all_app_interfaces = request.airavata_client.getAllApplicationInterfaces(
            self.authz_token, self.gateway_id)
        app_interfaces = []
        for app_interface in all_app_interfaces:
            if not app_interface.applicationModules:
                continue
            if app_module_id in app_interface.applicationModules:
                app_interfaces.append(app_interface)
        if len(app_interfaces) == 1:
            serializer = serializers.ApplicationInterfaceDescriptionSerializer(
                app_interfaces[0], context={'request': request})
            return Response(serializer.data)
        elif len(app_interfaces) > 1:
            log.error(
                "More than one application interface found for module {}: {}"
                .format(app_module_id, app_interfaces))
            raise Exception(
                'More than one application interface found for module {}'
                .format(app_module_id)
            )
        else:
            raise Http404("No application interface found for module id {}"
                          .format(app_module_id))

    @detail_route()
    def application_deployments(self, request, app_module_id):
        all_deployments = self.request.airavata_client.getAllApplicationDeployments(
            self.authz_token, self.gateway_id)
        app_deployments = [
            dep for dep in all_deployments if dep.appModuleId == app_module_id]
        serializer = serializers.ApplicationDeploymentDescriptionSerializer(
            app_deployments, many=True, context={'request': request})
        return Response(serializer.data)

    @list_route()
    def list_all(self, request, format=None):
        all_modules = self.request.airavata_client.getAllAppModules(
            self.authz_token, self.gateway_id)
        serializer = self.serializer_class(
            all_modules, many=True, context={'request': request})
        return Response(serializer.data)


class ApplicationInterfaceViewSet(APIBackedViewSet):
    serializer_class = serializers.ApplicationInterfaceDescriptionSerializer
    lookup_field = 'app_interface_id'

    def get_list(self):
        return self.request.airavata_client.getAllApplicationInterfaces(
            self.authz_token, self.gateway_id)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getApplicationInterface(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        application_interface = serializer.save()
        log.debug("application_interface: {}".format(application_interface))
        app_interface_id = self.request.airavata_client.registerApplicationInterface(
            self.authz_token, self.gateway_id, application_interface)
        application_interface.applicationInterfaceId = app_interface_id

    def perform_update(self, serializer):
        application_interface = serializer.save()
        self.request.airavata_client.updateApplicationInterface(
            self.authz_token,
            application_interface.applicationInterfaceId,
            application_interface)

    def perform_destroy(self, instance):
        self.request.airavata_client.deleteApplicationInterface(
            self.authz_token, instance.applicationInterfaceId)

    @detail_route()
    def compute_resources(self, request, app_interface_id):
        compute_resources = request.airavata_client.getAvailableAppInterfaceComputeResources(
            self.authz_token, app_interface_id)
        return Response(compute_resources)


class ApplicationDeploymentViewSet(APIBackedViewSet):
    serializer_class = serializers.ApplicationDeploymentDescriptionSerializer
    lookup_field = 'app_deployment_id'
    lookup_value_regex = '[^/]+'

    def get_list(self):
        app_module_id = self.request.query_params.get('appModuleId', None)
        group_resource_profile_id = self.request.query_params.get(
            'groupResourceProfileId', None)
        if (app_module_id and not group_resource_profile_id)\
                or (not app_module_id and group_resource_profile_id):
            raise ParseError("Query params appModuleId and "
                             "groupResourceProfileId are required together.")
        if app_module_id and group_resource_profile_id:
            return self.request.airavata_client.getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
                self.authz_token, app_module_id, group_resource_profile_id)
        else:
            return self.request.airavata_client.getAccessibleApplicationDeployments(
                self.authz_token, self.gateway_id, ResourcePermissionType.READ)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getApplicationDeployment(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        application_deployment = serializer.save()
        app_deployment_id = self.request.airavata_client.registerApplicationDeployment(
            self.authz_token, self.gateway_id, application_deployment)
        application_deployment.appDeploymentId = app_deployment_id

    def perform_update(self, serializer):
        application_deployment = serializer.save()
        self.request.airavata_client.updateApplicationDeployment(
            self.authz_token, application_deployment.appDeploymentId, application_deployment)

    def perform_destroy(self, instance):
        self.request.airavata_client.deleteApplicationDeployment(
            self.authz_token, instance.appDeploymentId)

    @detail_route()
    def queues(self, request, app_deployment_id):
        """Return queues for this deployment with defaults overridden by deployment defaults if they exist"""
        app_deployment = self.request.airavata_client.getApplicationDeployment(
            self.authz_token, app_deployment_id)
        compute_resource = request.airavata_client.getComputeResource(
            request.authz_token, app_deployment.computeHostId)
        # Override defaults with app deployment defaults
        batch_queues = []
        for batch_queue in compute_resource.batchQueues:
            if app_deployment.defaultQueueName:
                batch_queue.isDefaultQueue = (
                    app_deployment.defaultQueueName == batch_queue.queueName)
            if app_deployment.defaultNodeCount:
                batch_queue.defaultNodeCount = app_deployment.defaultNodeCount
            if app_deployment.defaultCPUCount:
                batch_queue.defaultCPUCount = app_deployment.defaultCPUCount
            if app_deployment.defaultWalltime:
                batch_queue.defaultWalltime = app_deployment.defaultWalltime
            batch_queues.append(batch_queue)
        serializer = serializers.BatchQueueSerializer(
            batch_queues, many=True, context={'request': request})
        return Response(serializer.data)


class ComputeResourceViewSet(mixins.RetrieveModelMixin,
                             GenericAPIBackedViewSet):
    serializer_class = serializers.ComputeResourceDescriptionSerializer
    lookup_field = 'compute_resource_id'
    lookup_value_regex = '[^/]+'

    def get_instance(self, lookup_value, format=None):
        return self.request.airavata_client.getComputeResource(
            self.authz_token, lookup_value)

    @list_route()
    def all_names(self, request, format=None):
        """Return a map of compute resource names keyed by resource id."""
        return Response(
            request.airavata_client.getAllComputeResourceNames(
                request.authz_token))

    @list_route()
    def all_names_list(self, request, format=None):
        """Return a list of compute resource names keyed by resource id."""
        all_names = request.airavata_client.getAllComputeResourceNames(
            request.authz_token)
        return Response([
            {
                'host_id': host_id,
                'host': host,
                'url': request.build_absolute_uri(
                    reverse('django_airavata_api:compute-resource-detail',
                            args=[host_id]))
            } for host_id, host in all_names.items()
        ])

    @detail_route()
    def queues(self, request, compute_resource_id, format=None):
        details = request.airavata_client.getComputeResource(
            request.authz_token, compute_resource_id)
        serializer = self.serializer_class(instance=details,
                                           context={'request': request})
        data = serializer.data
        return Response([queue["queueName"] for queue in data["batchQueues"]])


class LocalJobSubmissionView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        job_submission_id = request.query_params["id"]
        local_job_submission = request.airavata_client.getLocalJobSubmission(
            request.authz_token, job_submission_id)
        return Response(
            thrift_utils.create_serializer(
                LOCALSubmission,
                instance=local_job_submission).data)


class CloudJobSubmissionView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        job_submission_id = request.query_params["id"]
        job_submission = request.airavata_client.getCloudJobSubmission(
            request.authz_token, job_submission_id)
        return Response(
            thrift_utils.create_serializer(
                CloudJobSubmission,
                instance=job_submission).data)


class GlobusJobSubmissionView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        job_submission_id = request.query_params["id"]
        job_submission = request.airavata_client.getClo(
            request.authz_token, job_submission_id)
        return Response(
            thrift_utils.create_serializer(
                GlobusJobSubmission,
                instance=job_submission).data)


class SshJobSubmissionView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        job_submission_id = request.query_params["id"]
        job_submission = request.airavata_client.getSSHJobSubmission(
            request.authz_token, job_submission_id)
        return Response(
            thrift_utils.create_serializer(
                SSHJobSubmission,
                instance=job_submission).data)


class UnicoreJobSubmissionView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        job_submission_id = request.query_params["id"]
        job_submission = request.airavata_client.getUnicoreJobSubmission(
            request.authz_token, job_submission_id)
        return Response(
            thrift_utils.create_serializer(
                UnicoreJobSubmission,
                instance=job_submission).data)


class GridFtpDataMovementView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        data_movement_id = request.query_params["id"]
        data_movement = request.airavata_client.getGridFTPDataMovement(
            request.authz_token, data_movement_id)
        return Response(
            thrift_utils.create_serializer(
                GridFTPDataMovement,
                instance=data_movement).data)


class ScpDataMovementView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        data_movement_id = request.query_params["id"]
        data_movement = request.airavata_client.getSCPDataMovement(
            request.authz_token, data_movement_id)
        return Response(
            thrift_utils.create_serializer(
                SCPDataMovement,
                instance=data_movement).data)


class UnicoreDataMovementView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        data_movement_id = request.query_params["id"]
        data_movement = request.airavata_client.getUnicoreDataMovement(
            request.authz_token, data_movement_id)
        return Response(
            thrift_utils.create_serializer(
                UnicoreDataMovement,
                instance=data_movement).data)


class LocalDataMovementView(APIView):
    renderer_classes = (JSONRenderer,)

    def get(self, request, format=None):
        data_movement_id = request.query_params["id"]
        data_movement = request.airavata_client.getLocalDataMovement(
            request.authz_token, data_movement_id)
        return Response(
            thrift_utils.create_serializer(
                LOCALDataMovement,
                instance=data_movement).data)


class DataProductViewSet(mixins.RetrieveModelMixin,
                         GenericAPIBackedViewSet):
    serializer_class = serializers.DataProductSerializer
    lookup_field = 'product_uri'
    lookup_value_regex = '.*'

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getDataProduct(
            self.request.authz_token, lookup_value)


@login_required
def upload_input_file(request):
    try:
        username = request.user.username
        project_id = request.POST['project-id']
        project = request.airavata_client.getProject(
            request.authz_token, project_id)
        exp_name = request.POST['experiment-name']
        input_file = request.FILES['file']
        data_product = datastore.save(username, project.name, exp_name,
                                      input_file)
        data_product_uri = request.airavata_client.registerDataProduct(
            request.authz_token, data_product)
        return JsonResponse({'uploaded': True,
                             'data-product-uri': data_product_uri})
    except Exception as e:
        log.error("Failed to upload file", exc_info=True)
        resp = JsonResponse({'uploaded': False, 'error': str(e)})
        resp.status_code = 500
        return resp


@login_required
def download_file(request):
    # TODO check that user has access to this file using sharing API
    data_product_uri = request.GET.get('data-product-uri', '')
    data_product = None
    try:
        data_product = request.airavata_client.getDataProduct(
            request.authz_token, data_product_uri)
    except Exception as e:
        log.warning("Failed to load DataProduct for {}"
                    .format(data_product_uri), exc_info=True)
        raise Http404("data product does not exist") from e
    try:
        data_file = datastore.open(data_product)
        response = FileResponse(data_file,
                                content_type="application/octet-stream")
        response['Content-Disposition'] = ('attachment; filename="{}"'
                                           .format(data_product.productName))
        return response
    except ObjectDoesNotExist as e:
        raise Http404(str(e)) from e


class UserProfileViewSet(mixins.ListModelMixin, GenericAPIBackedViewSet):
    serializer_class = serializers.UserProfileSerializer

    def get_list(self):
        user_profile_client = self.request.profile_service['user_profile']
        return user_profile_client.getAllUserProfilesInGateway(
            self.authz_token, self.gateway_id, 0, -1)


class GroupResourceProfileViewSet(APIBackedViewSet):
    serializer_class = serializers.GroupResourceProfileSerializer
    lookup_field = 'group_resource_profile_id'

    def get_list(self):
        return self.request.airavata_client.getGroupResourceList(
            self.authz_token, self.gateway_id)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getGroupResourceProfile(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        group_resource_profile = serializer.save()
        group_resource_profile.gatewayId = self.gateway_id
        group_resource_profile_id = self.request.airavata_client.createGroupResourceProfile(
            authzToken=self.authz_token, groupResourceProfile=group_resource_profile)
        group_resource_profile.groupResourceProfileId = group_resource_profile_id

    def perform_update(self, serializer):
        grp = serializer.save()
        for removed_compute_resource_preference \
                in grp._removed_compute_resource_preferences:
            self.request.airavata_client.removeGroupComputePrefs(
                self.authz_token,
                removed_compute_resource_preference.computeResourceId,
                removed_compute_resource_preference.groupResourceProfileId)
        for removed_compute_resource_policy \
                in grp._removed_compute_resource_policies:
            self.request.airavata_client.removeGroupComputeResourcePolicy(
                self.authz_token,
                removed_compute_resource_policy.resourcePolicyId)
        for removed_batch_queue_resource_policy \
                in grp._removed_batch_queue_resource_policies:
            self.request.airavata_client.removeGroupBatchQueueResourcePolicy(
                self.authz_token,
                removed_batch_queue_resource_policy.resourcePolicyId)
        self.request.airavata_client.updateGroupResourceProfile(
            self.authz_token, grp)

    def perform_destroy(self, instance):
        self.request.airavata_client.removeGroupResourceProfile(
            self.authz_token, instance.groupResourceProfileId)


class SharedEntityViewSet(mixins.RetrieveModelMixin,
                          mixins.UpdateModelMixin,
                          GenericAPIBackedViewSet):
    serializer_class = serializers.SharedEntitySerializer
    lookup_field = 'entity_id'
    lookup_value_regex = '[^/]+'

    def get_instance(self, lookup_value):
        users = {}
        # Load accessible users in order of permission precedence: users that
        # have WRITE permission should also have READ
        users.update(self._load_accessible_users(
            lookup_value, ResourcePermissionType.READ))
        users.update(self._load_accessible_users(
            lookup_value, ResourcePermissionType.WRITE))
        owner_ids = self._load_accessible_users(lookup_value,
                                                ResourcePermissionType.OWNER)
        # Assume that there is one and only one owner
        owner_id = list(owner_ids.keys())[0]
        # Remove owner from the users list
        del users[owner_id]
        user_list = []
        for user_id in users:
            user_list.append({'user': self._load_user_profile(user_id),
                              'permissionType': users[user_id]})
        groups = {}
        groups.update(self._load_accessible_groups(
            lookup_value, ResourcePermissionType.READ))
        groups.update(self._load_accessible_groups(
            lookup_value, ResourcePermissionType.WRITE))
        group_list = []
        for group_id in groups:
            group_list.append({'group': self._load_group(group_id),
                               'permissionType': groups[group_id]})
        return {'entityId': lookup_value,
                'userPermissions': user_list,
                'groupPermissions': group_list,
                'owner': self._load_user_profile(owner_id)}

    def _load_accessible_users(self, entity_id, permission_type):
        users = self.request.airavata_client.getAllAccessibleUsers(
            self.authz_token, entity_id, permission_type)
        return {user_id: permission_type for user_id in users}

    def _load_user_profile(self, user_id):
        user_profile_client = self.request.profile_service['user_profile']
        username = user_id[0:user_id.rindex('@')]
        return user_profile_client.getUserProfileById(self.authz_token,
                                                      username,
                                                      settings.GATEWAY_ID)

    def _load_accessible_groups(self, entity_id, permission_type):
        groups = self.request.airavata_client.getAllAccessibleGroups(
            self.authz_token, entity_id, permission_type)
        return {group_id: permission_type for group_id in groups}

    def _load_group(self, group_id):
        group_manager_client = self.request.profile_service['group_manager']
        return group_manager_client.getGroup(self.authz_token, group_id)

    def perform_update(self, serializer):
        shared_entity = serializer.save()
        entity_id = shared_entity['entityId']
        if len(shared_entity['_user_grant_read_permission']) > 0:
            self._share_with_users(
                entity_id, ResourcePermissionType.READ,
                shared_entity['_user_grant_read_permission'])
        if len(shared_entity['_user_grant_write_permission']) > 0:
            self._share_with_users(
                entity_id, ResourcePermissionType.WRITE,
                shared_entity['_user_grant_write_permission'])
        if len(shared_entity['_user_revoke_read_permission']) > 0:
            self._revoke_from_users(
                entity_id, ResourcePermissionType.READ,
                shared_entity['_user_revoke_read_permission'])
        if len(shared_entity['_user_revoke_write_permission']) > 0:
            self._revoke_from_users(
                entity_id, ResourcePermissionType.WRITE,
                shared_entity['_user_revoke_write_permission'])
        if len(shared_entity['_group_grant_read_permission']) > 0:
            self._share_with_groups(
                entity_id, ResourcePermissionType.READ,
                shared_entity['_group_grant_read_permission'])
        if len(shared_entity['_group_grant_write_permission']) > 0:
            self._share_with_groups(
                entity_id, ResourcePermissionType.WRITE,
                shared_entity['_group_grant_write_permission'])
        if len(shared_entity['_group_revoke_read_permission']) > 0:
            self._revoke_from_groups(
                entity_id, ResourcePermissionType.READ,
                shared_entity['_group_revoke_read_permission'])
        if len(shared_entity['_group_revoke_write_permission']) > 0:
            self._revoke_from_groups(
                entity_id, ResourcePermissionType.WRITE,
                shared_entity['_group_revoke_write_permission'])

    def _share_with_users(self, entity_id, permission_type, user_ids):
        self.request.airavata_client.shareResourceWithUsers(
            self.authz_token, entity_id,
            {user_id: permission_type for user_id in user_ids})

    def _revoke_from_users(self, entity_id, permission_type, user_ids):
        self.request.airavata_client.revokeSharingOfResourceFromUsers(
            self.authz_token, entity_id,
            {user_id: permission_type for user_id in user_ids})

    def _share_with_groups(self, entity_id, permission_type, group_ids):
        self.request.airavata_client.shareResourceWithGroups(
            self.authz_token, entity_id,
            {group_id: permission_type for group_id in group_ids})

    def _revoke_from_groups(self, entity_id, permission_type, group_ids):
        self.request.airavata_client.revokeSharingOfResourceFromGroups(
            self.authz_token, entity_id,
            {group_id: permission_type for group_id in group_ids})

    @detail_route(methods=['put'])
    def merge(self, request, entity_id=None):
        # Validate updated sharing settings
        updated = self.get_serializer(data=request.data)
        updated.is_valid(raise_exception=True)
        # Get the existing sharing settings and merge in the updated settings
        existing_instance = self.get_object()
        existing = self.get_serializer(instance=existing_instance)
        merged_data = existing.data
        merged_data['userPermissions'] = existing.data['userPermissions'] + \
            updated.initial_data['userPermissions']
        merged_data['groupPermissions'] = existing.data['groupPermissions'] + \
            updated.initial_data['groupPermissions']
        # Create a merged_serializer from the existing sharing settings and the
        # merged settings. This will calculate all permissions that need to be
        # granted and revoked to go from the exisitng settings to the merged
        # settings.
        merged_serializer = self.get_serializer(
            existing_instance, data=merged_data)
        merged_serializer.is_valid(raise_exception=True)
        self.perform_update(merged_serializer)
        return Response(merged_serializer.data)


class CredentialSummaryViewSet(APIBackedViewSet):
    serializer_class = serializers.CredentialSummarySerializer

    def get_list(self):
        ssh_creds = self.request.airavata_client.getAllCredentialSummaries(
            self.authz_token, SummaryType.SSH)
        pwd_creds = self.request.airavata_client.getAllCredentialSummaries(
            self.authz_token, SummaryType.PASSWD)
        return ssh_creds + pwd_creds

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getCredentialSummary(
            self.authz_token, lookup_value)

    @action(detail=False)
    def ssh(self, request):
        summaries = self.request.airavata_client.getAllCredentialSummaries(
            self.authz_token, SummaryType.SSH
        )
        serializer = self.get_serializer(summaries, many=True)
        return Response(serializer.data)

    @action(detail=False)
    def password(self, request):
        summaries = self.request.airavata_client.getAllCredentialSummaries(
            self.authz_token, SummaryType.PASSWD
        )
        serializer = self.get_serializer(summaries, many=True)
        return Response(serializer.data)

    @action(methods=['post'], detail=False)
    def create_ssh(self, request):
        if 'description' not in request.data:
            raise ParseError("'description' is required in request")
        description = request.data.get('description')
        token_id = self.request.airavata_client.generateAndRegisterSSHKeys(
            request.authz_token, description)
        credential_summary = self.request.airavata_client.getCredentialSummary(
            request.authz_token, token_id)
        serializer = self.get_serializer(credential_summary)
        return Response(serializer.data)

    @action(methods=['post'], detail=False)
    def create_password(self, request):
        if ('username' not in request.data or
            'password' not in request.data or
                'description' not in request.data):
            raise ParseError("'username', 'password' and 'description' "
                             "are all required in request")
        username = request.data.get('username')
        password = request.data.get('password')
        description = request.data.get('description')
        token_id = self.request.airavata_client.registerPwdCredential(
            request.authz_token, username, password, description)
        credential_summary = self.request.airavata_client.getCredentialSummary(
            request.authz_token, token_id)
        serializer = self.get_serializer(credential_summary)
        return Response(serializer.data)

    def perform_destroy(self, instance):
        if instance.type == SummaryType.SSH:
            self.request.airavata_client.deleteSSHPubKey(
                self.authz_token, instance.token)
        elif instance.type == SummaryType.PASSWD:
            self.request.airavata_client.deletePWDCredential(
                self.authz_token, instance.token)


class GatewayResourceProfileViewSet(APIBackedViewSet):
    serializer_class = serializers.GatewayResourceProfileSerializer
    lookup_field = 'gateway_id'
    lookup_value_regex = '[^/]+'

    def get_list(self):
        return self.request.airavata_client.getAllGatewayResourceProfiles(
            self.authz_token)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getGatewayResourceProfile(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        gateway_resource_profile = serializer.save()
        self.request.airavata_client.registerGatewayResourceProfile(
            self.authz_token, gateway_resource_profile)

    def perform_update(self, serializer):
        gateway_resource_profile = serializer.save()
        self.request.airavata_client.updateGatewayResourceProfile(
            self.authz_token,
            gateway_resource_profile.gatewayID,
            gateway_resource_profile)

    def perform_destroy(self, instance):
        self.request.airavata_client.deleteGatewayResourceProfile(
            self.authz_token, instance.gatewayID)


class GetCurrentGatewayResourceProfile(APIView):

    def get(self, request, format=None):
        gateway_resource_profile = \
            request.airavata_client.getGatewayResourceProfile(
                request.authz_token, settings.GATEWAY_ID)
        serializer = serializers.GatewayResourceProfileSerializer(
            gateway_resource_profile, context={'request': request})
        return Response(serializer.data)


class StorageResourceViewSet(mixins.RetrieveModelMixin,
                             GenericAPIBackedViewSet):
    serializer_class = serializers.StorageResourceSerializer
    lookup_field = 'storage_resource_id'
    lookup_value_regex = '[^/]+'

    def get_instance(self, lookup_value, format=None):
        return self.request.airavata_client.getStorageResource(
            self.authz_token, lookup_value)

    @list_route()
    def all_names(self, request, format=None):
        """Return a map of compute resource names keyed by resource id."""
        return Response(
            request.airavata_client.getAllStorageResourceNames(
                request.authz_token))


class StoragePreferenceViewSet(APIBackedViewSet):
    serializer_class = serializers.StoragePreferenceSerializer
    lookup_field = 'storage_resource_id'
    lookup_value_regex = '[^/]+'

    def get_list(self):
        return self.request.airavata_client.getAllGatewayStoragePreferences(
            self.authz_token, settings.GATEWAY_ID)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getGatewayStoragePreference(
            self.authz_token, settings.GATEWAY_ID, lookup_value)

    def perform_create(self, serializer):
        storage_preference = serializer.save()
        self.request.airavata_client.addGatewayStoragePreference(
            self.authz_token,
            settings.GATEWAY_ID,
            storage_preference.storageResourceId,
            storage_preference)

    def perform_update(self, serializer):
        storage_preference = serializer.save()
        self.request.airavata_client.updateGatewayStoragePreference(
            self.authz_token,
            settings.GATEWAY_ID,
            storage_preference.storageResourceId,
            storage_preference)

    def perform_destroy(self, instance):
        self.request.airavata_client.deleteGatewayStoragePreference(
            self.authz_token, settings.GATEWAY_ID, instance.storageResourceId)


class ParserViewSet(mixins.CreateModelMixin,
                    mixins.RetrieveModelMixin,
                    mixins.UpdateModelMixin,
                    mixins.ListModelMixin,
                    GenericAPIBackedViewSet):
    serializer_class = serializers.ParserSerializer
    lookup_field = 'parser_id'
    lookup_value_regex = '[^/]+'

    def get_list(self):
        return self.request.airavata_client.listAllParsers(self.authz_token, settings.GATEWAY_ID)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getParser(
            self.authz_token, lookup_value, settings.GATEWAY_ID)

    def perform_create(self, serializer):
        parser = serializer.save()
        self.request.airavata_client.saveParser(self.authz_token, parser)

    def perform_update(self, serializer):
        parser = serializer.save()
        self.request.airavata_client.saveParser(self.authz_token, parser)
