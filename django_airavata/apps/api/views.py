import json
import logging
import os
from datetime import datetime, timedelta

from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.core.exceptions import ObjectDoesNotExist, PermissionDenied
from django.http import FileResponse, Http404, HttpResponse, JsonResponse
from django.urls import reverse
from rest_framework import mixins
from rest_framework.decorators import action, detail_route, list_route
from rest_framework.exceptions import ParseError
from rest_framework.renderers import JSONRenderer
from rest_framework.response import Response
from rest_framework.views import APIView

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
from airavata.model.experiment.ttypes import ExperimentSearchFields
from airavata.model.group.ttypes import ResourcePermissionType
from airavata.model.user.ttypes import Status
from django_airavata.apps.api.view_utils import (
    APIBackedViewSet,
    APIResultIterator,
    APIResultPagination,
    GenericAPIBackedViewSet,
    IsInAdminsGroupPermission
)
from django_airavata.apps.auth import iam_admin_client
from django_airavata.apps.auth.models import EmailVerification

from . import (
    data_products_helper,
    exceptions,
    helpers,
    models,
    output_views,
    serializers,
    signals,
    thrift_utils,
    tus,
    view_utils
)

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
                group_manager = view.request.profile_service['group_manager']
                groups = group_manager.getGroups(view.authz_token)
                end = offset + limit if limit > 0 else len(groups)
                return groups[offset:end] if groups else []

        return GroupResultsIterator()

    def get_instance(self, lookup_value):
        return self.request.profile_service['group_manager'].getGroup(
            self.authz_token, lookup_value)

    def perform_create(self, serializer):
        group = serializer.save()
        group_id = self.request.profile_service['group_manager'].createGroup(
            self.authz_token, group)
        group.id = group_id
        users_added_to_group = set(group.members) - {group.ownerId}
        self._send_users_added_to_group(users_added_to_group, group)

    def perform_update(self, serializer):
        group = serializer.save()
        group_manager_client = self.request.profile_service['group_manager']
        if len(group._added_members) > 0:
            group_manager_client.addUsersToGroup(
                self.authz_token, group._added_members, group.id)
            self._send_users_added_to_group(group._added_members, group)
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

    def _send_users_added_to_group(self, internal_user_ids, group):
        for internal_user_id in internal_user_ids:
            user_id, gateway_id = internal_user_id.rsplit("@", maxsplit=1)
            user_profile = self.request.profile_service['user_profile'].getUserProfileById(
                self.authz_token, user_id, gateway_id)
            signals.user_added_to_group.send(
                sender=self.__class__,
                user=user_profile,
                groups=[group],
                request=self.request)


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
        self._update_most_recent_project(project_id)

    def perform_update(self, serializer):
        project = serializer.save()
        self.request.airavata_client.updateProject(
            self.authz_token, project.projectID, project)
        self._update_most_recent_project(project.projectID)

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

    def _update_most_recent_project(self, project_id):
        prefs = helpers.WorkspacePreferencesHelper().get(self.request)
        prefs.most_recent_project_id = project_id
        prefs.save()


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
        experiment_id = self.request.airavata_client.createExperiment(
            self.authz_token, self.gateway_id, experiment)
        self._update_workspace_preferences(
            project_id=experiment.projectId,
            group_resource_profile_id=experiment.userConfigurationData.groupResourceProfileId,
            compute_resource_id=experiment.userConfigurationData.computationalResourceScheduling.resourceHostId)
        experiment.experimentId = experiment_id

    def perform_update(self, serializer):
        experiment = serializer.save(
            gatewayId=self.gateway_id,
            userName=self.username)
        self.request.airavata_client.updateExperiment(
            self.authz_token, experiment.experimentId, experiment)
        self._update_workspace_preferences(
            project_id=experiment.projectId,
            group_resource_profile_id=experiment.userConfigurationData.groupResourceProfileId,
            compute_resource_id=experiment.userConfigurationData.computationalResourceScheduling.resourceHostId)

    def _set_storage_id_and_data_dir(self, experiment):
        # Storage ID
        experiment.userConfigurationData.storageId = \
            settings.GATEWAY_DATA_STORE_RESOURCE_ID
        # Create experiment dir and set it on model
        if not experiment.userConfigurationData.experimentDataDir:
            project = self.request.airavata_client.getProject(
                self.authz_token, experiment.projectId)
            exp_dir = data_products_helper.get_experiment_dir(
                self.request, project.name, experiment.experimentName)
            experiment.userConfigurationData.experimentDataDir = exp_dir
        else:
            # get_experiment_dir will also validate that absolute paths are
            # inside the user's storage directory
            exp_dir = data_products_helper.get_experiment_dir(
                self.request,
                path=experiment.userConfigurationData.experimentDataDir)
            experiment.userConfigurationData.experimentDataDir = exp_dir

    def _move_tmp_input_file_uploads_to_data_dir(self, experiment):
        exp_data_dir = experiment.userConfigurationData.experimentDataDir
        for experiment_input in experiment.experimentInputs:
            if experiment_input.type == DataType.URI:
                if experiment_input.value:
                    experiment_input.value = \
                        self._move_if_tmp_input_file_upload(
                            experiment_input.value, exp_data_dir)
            elif experiment_input.type == DataType.URI_COLLECTION:
                data_product_uris = experiment_input.value.split(
                    ",") if experiment_input.value else []
                moved_data_product_uris = []
                for data_product_uri in data_product_uris:
                    moved_data_product_uris.append(
                        self._move_if_tmp_input_file_upload(data_product_uri,
                                                            exp_data_dir))
                experiment_input.value = ",".join(moved_data_product_uris)

    def _move_if_tmp_input_file_upload(
            self, data_product_uri, experiment_data_dir):
        """
        Conditionally moves tmp input file to data dir and returns new dp URI.
        """
        data_product = self.request.airavata_client.getDataProduct(
            self.authz_token, data_product_uri)
        if data_products_helper.is_input_file_upload(
                self.request, data_product):
            moved_data_product = \
                data_products_helper.move_input_file_upload(
                    self.request,
                    data_product,
                    experiment_data_dir)
            return moved_data_product.productUri
        else:
            return data_product_uri

    @detail_route(methods=['post'])
    def launch(self, request, experiment_id=None):
        try:
            experiment = request.airavata_client.getExperiment(
                self.authz_token, experiment_id)
            self._set_storage_id_and_data_dir(experiment)
            self._move_tmp_input_file_uploads_to_data_dir(experiment)
            request.airavata_client.updateExperiment(
                self.authz_token, experiment_id, experiment)
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

    @detail_route(methods=['post'])
    def clone(self, request, experiment_id=None):

        # figure what project to clone into
        experiment = self.request.airavata_client.getExperiment(
            self.authz_token, experiment_id)
        project_id = self._get_writeable_project(experiment)

        # clone experiment
        cloned_experiment_id = request.airavata_client.cloneExperiment(
            self.authz_token, experiment_id,
            "Clone of {}".format(experiment.experimentName), project_id)
        cloned_experiment = request.airavata_client.getExperiment(
            self.authz_token, cloned_experiment_id)

        # Create a copy of the experiment input files
        self._copy_cloned_experiment_input_uris(cloned_experiment)

        # Null out experimentDataDir so a new one will get created at launch
        # time
        cloned_experiment.userConfigurationData.experimentDataDir = None
        request.airavata_client.updateExperiment(
            self.authz_token, cloned_experiment.experimentId, cloned_experiment
        )
        serializer = self.serializer_class(
            cloned_experiment, context={'request': request})
        return Response(serializer.data)

    @detail_route(methods=['post'])
    def cancel(self, request, experiment_id=None):
        try:
            request.airavata_client.terminateExperiment(
                request.authz_token, experiment_id, self.gateway_id)
            return Response({'success': True})
        except Exception as e:
            log.error("Cancel action has thrown the following error: ", e)
            raise e

    def _get_writeable_project(self, experiment):
        # figure what project to clone into:
        # 1) project of this experiment if writeable
        # 2) most recently used project if writeable
        # 3) else, first writeable project
        project_id = experiment.projectId
        if self._can_write(project_id):
            return project_id
        workspace_preferences = models.WorkspacePreferences.objects.filter(
            username=self.username).first()
        if (workspace_preferences and self._can_write(
                workspace_preferences.most_recent_project_id)):
            return workspace_preferences.most_recent_project_id
        user_projects = self.request.airavata_client.getUserProjects(
            self.authz_token, self.gateway_id, self.username, -1, 0)
        for user_project in user_projects:
            if self._can_write(user_project.projectID):
                return user_project.projectID
        raise Exception(
            "Could not find writeable project for user {} in "
            "gateway {}".format(self.username, self.gateway_id))

    def _can_write(self, entity_id):
        return self.request.airavata_client.userHasAccess(
            self.authz_token, entity_id, ResourcePermissionType.WRITE)

    def _copy_cloned_experiment_input_uris(self, cloned_experiment):
        # update the experimentInputs of type URI, copying input files into the
        # tmp input files directory of the data store
        for experiment_input in cloned_experiment.experimentInputs:
            # skip inputs without values
            if not experiment_input.value:
                continue
            if experiment_input.type == DataType.URI:
                cloned_data_product = self._copy_experiment_input_uri(
                    experiment_input.value)
                if cloned_data_product is None:
                    log.warning("Setting cloned input {} to null".format(
                        experiment_input.name))
                    experiment_input.value = None
                else:
                    experiment_input.value = cloned_data_product.productUri
            elif experiment_input.type == DataType.URI_COLLECTION:
                data_product_uris = experiment_input.value.split(
                    ",") if experiment_input.value else []
                cloned_data_product_uris = []
                for data_product_uri in data_product_uris:
                    cloned_data_product = self._copy_experiment_input_uri(
                        data_product_uri)
                    if cloned_data_product is None:
                        log.warning(
                            "Omitting a cloned input value for {}".format(
                                experiment_input.name))
                    else:
                        cloned_data_product_uris.append(
                            cloned_data_product.productUri)
                experiment_input.value = ",".join(cloned_data_product_uris)

    def _copy_experiment_input_uri(
            self,
            data_product_uri):
        source_data_product = self.request.airavata_client.getDataProduct(
            self.authz_token, data_product_uri)
        if data_products_helper.exists(self.request, source_data_product):
            return data_products_helper.copy_input_file_upload(
                self.request, source_data_product)
        else:
            log.warning("Could not find file for source data "
                        "product {}".format(source_data_product))
            return None

    def _update_workspace_preferences(self, project_id,
                                      group_resource_profile_id,
                                      compute_resource_id):
        prefs = helpers.WorkspacePreferencesHelper().get(self.request)
        prefs.most_recent_project_id = project_id
        prefs.most_recent_group_resource_profile_id = group_resource_profile_id
        prefs.most_recent_compute_resource_id = compute_resource_id
        prefs.save()


class ExperimentSearchViewSet(mixins.ListModelMixin, GenericAPIBackedViewSet):
    serializer_class = serializers.ExperimentSummarySerializer
    pagination_class = APIResultPagination
    pagination_viewname = 'django_airavata_api:experiment-search-list'

    def get_list(self):
        view = self

        filters = {}
        for filter_item in self.request.query_params.items():
            if filter_item[0] in ExperimentSearchFields._NAMES_TO_VALUES:
                # Lookup enum value for this ExperimentSearchFields
                search_field = ExperimentSearchFields._NAMES_TO_VALUES[
                    filter_item[0]]
                filters[search_field] = filter_item[1]

        class ExperimentSearchResultIterator(APIResultIterator):
            def get_results(self, limit=-1, offset=0):
                return view.request.airavata_client.searchExperiments(
                    view.authz_token, view.gateway_id, view.username, filters,
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
        outputDataProducts += [
            self.request.airavata_client.getDataProduct(self.authz_token, dp)
            for output in experimentModel.experimentOutputs
            if (output.value and
                output.type == DataType.URI_COLLECTION)
            for dp in output.value.split(',')
            if output.value.startswith('airavata-dp')]
        appInterfaceId = experimentModel.executionId
        try:
            applicationInterface = self.request.airavata_client \
                .getApplicationInterface(self.authz_token, appInterfaceId)
        except Exception as e:
            log.exception("Failed to load app interface")
            applicationInterface = None
        exp_output_views = output_views.get_output_views(
            self.request, experimentModel, applicationInterface)
        inputDataProducts = [
            self.request.airavata_client.getDataProduct(self.authz_token,
                                                        inp.value)
            for inp in experimentModel.experimentInputs
            if (inp.value and
                inp.value.startswith('airavata-dp') and
                inp.type in (DataType.URI,
                             DataType.STDOUT,
                             DataType.STDERR))]
        inputDataProducts += [
            self.request.airavata_client.getDataProduct(self.authz_token, dp)
            for inp in experimentModel.experimentInputs
            if (inp.value and
                inp.type == DataType.URI_COLLECTION)
            for dp in inp.value.split(',')
            if inp.value.startswith('airavata-dp')]
        try:
            if applicationInterface is not None:
                appModuleId = applicationInterface.applicationModules[0]
                applicationModule = self.request.airavata_client \
                    .getApplicationModule(self.authz_token, appModuleId)
            else:
                log.warning(
                    "Cannot log application model since app interface failed to load")
        except Exception as e:
            log.exception("Failed to load app interface/module")
            applicationModule = None

        compute_resource_id = None
        user_conf = experimentModel.userConfigurationData
        if user_conf and user_conf.computationalResourceScheduling:
            comp_res_sched = user_conf.computationalResourceScheduling
            compute_resource_id = comp_res_sched.resourceHostId
        try:
            compute_resource = self.request.airavata_client.getComputeResource(
                self.authz_token, compute_resource_id) \
                if compute_resource_id else None
        except Exception as e:
            log.exception("Failed to load compute resource for {}".format(
                compute_resource_id))
            compute_resource = None
        if self.request.airavata_client.userHasAccess(
                self.authz_token,
                experimentModel.projectId,
                ResourcePermissionType.READ):
            project = self.request.airavata_client.getProject(
                self.authz_token, experimentModel.projectId)
        else:
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
            jobDetails=job_details,
            outputViews=exp_output_views)
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

    @detail_route(methods=['post'])
    def favorite(self, request, app_module_id):
        helper = helpers.WorkspacePreferencesHelper()
        workspace_preferences = helper.get(request)
        try:
            application_preferences = (
                workspace_preferences.applicationpreferences_set.get(
                    application_id=app_module_id))
            application_preferences.favorite = True
            application_preferences.save()
        except ObjectDoesNotExist:
            workspace_preferences.applicationpreferences_set.create(
                username=request.user.username,
                application_id=app_module_id,
                favorite=True)

        return HttpResponse(status=204)

    @detail_route(methods=['post'])
    def unfavorite(self, request, app_module_id):
        helper = helpers.WorkspacePreferencesHelper()
        workspace_preferences = helper.get(request)
        try:
            application_preferences = (
                workspace_preferences.applicationpreferences_set.get(
                    application_id=app_module_id))
            application_preferences.favorite = False
            application_preferences.save()
        except ObjectDoesNotExist:
            workspace_preferences.applicationpreferences_set.create(
                username=request.user.username,
                application_id=app_module_id,
                favorite=False)

        return HttpResponse(status=204)

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
        self._update_input_metadata(application_interface)
        log.debug("application_interface: {}".format(application_interface))
        app_interface_id = self.request.airavata_client.registerApplicationInterface(
            self.authz_token, self.gateway_id, application_interface)
        application_interface.applicationInterfaceId = app_interface_id

    def perform_update(self, serializer):
        application_interface = serializer.save()
        self._update_input_metadata(application_interface)
        self.request.airavata_client.updateApplicationInterface(
            self.authz_token,
            application_interface.applicationInterfaceId,
            application_interface)

    def perform_destroy(self, instance):
        self.request.airavata_client.deleteApplicationInterface(
            self.authz_token, instance.applicationInterfaceId)

    def _update_input_metadata(self, app_interface):
        for app_input in app_interface.applicationInputs:
            if app_input.metaData:
                metadata = json.loads(app_input.metaData)
                # Automatically add {showOptions: {isRequired: true/false}} to
                # toggle isRequired on hidden/shown inputs
                if ("editor" in metadata and
                    "dependencies" in metadata["editor"] and
                        "show" in metadata["editor"]["dependencies"]):
                    if "showOptions" not in metadata["editor"]["dependencies"]:
                        metadata["editor"]["dependencies"]["showOptions"] = {}
                    o = metadata["editor"]["dependencies"]["showOptions"]
                    o["isRequired"] = app_input.isRequired
                    app_input.metaData = json.dumps(metadata)

    @detail_route()
    def compute_resources(self, request, app_interface_id):
        compute_resources = request.airavata_client.getAvailableAppInterfaceComputeResources(
            self.authz_token, app_interface_id)
        return Response(compute_resources)


class ApplicationDeploymentViewSet(APIBackedViewSet):
    serializer_class = serializers.ApplicationDeploymentDescriptionSerializer
    lookup_field = 'app_deployment_id'

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


class DataProductView(APIView):

    serializer_class = serializers.DataProductSerializer

    def get(self, request, format=None):
        data_product_uri = request.query_params['product-uri']
        data_product = request.airavata_client.getDataProduct(
            request.authz_token, data_product_uri)
        serializer = self.serializer_class(
            data_product, context={'request': request})
        return Response(serializer.data)


@login_required
def upload_input_file(request):
    try:
        input_file = request.FILES['file']
        data_product = data_products_helper.save_input_file_upload(
            request, input_file, content_type=input_file.content_type)
        serializer = serializers.DataProductSerializer(
            data_product, context={'request': request})
        return JsonResponse({'uploaded': True,
                             'data-product': serializer.data})
    except Exception as e:
        log.error("Failed to upload file", exc_info=True)
        resp = JsonResponse({'uploaded': False, 'error': str(e)})
        resp.status_code = 500
        return resp


@login_required
def tus_upload_finish(request):
    uploadURL = request.POST['uploadURL']

    def move_input_file(file_path, file_name, file_type):
        return data_products_helper.move_input_file_upload_from_filepath(
            request, file_path, name=file_name, content_type=file_type)
    try:
        data_product = tus.move_tus_upload(uploadURL, move_input_file)
        serializer = serializers.DataProductSerializer(
            data_product, context={'request': request})
        return JsonResponse({'uploaded': True,
                             'data-product': serializer.data})
    except Exception as e:
        return exceptions.generic_json_exception_response(e, status=400)


@login_required
def download_file(request):
    # TODO check that user has access to this file using sharing API
    data_product_uri = request.GET.get('data-product-uri', '')
    force_download = 'download' in request.GET
    data_product = None
    try:
        data_product = request.airavata_client.getDataProduct(
            request.authz_token, data_product_uri)
        mime_type = "application/octet-stream"  # default mime-type
        if (data_product.productMetadata and
                'mime-type' in data_product.productMetadata):
            mime_type = data_product.productMetadata['mime-type']
        # 'mime-type' url parameter overrides
        mime_type = request.GET.get('mime-type', mime_type)
    except Exception as e:
        log.warning("Failed to load DataProduct for {}"
                    .format(data_product_uri), exc_info=True)
        raise Http404("data product does not exist") from e
    try:
        data_file = data_products_helper.open_file(request, data_product)
        response = FileResponse(data_file, content_type=mime_type)
        file_name = os.path.basename(data_file.name)
        if mime_type == 'application/octet-stream' or force_download:
            response['Content-Disposition'] = ('attachment; filename="{}"'
                                               .format(file_name))
        else:
            response['Content-Disposition'] = f'filename="{file_name}"'
        return response
    except ObjectDoesNotExist as e:
        raise Http404(str(e)) from e


@login_required
def delete_file(request):
    # TODO check that user has write access to this file using sharing API
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
        if (data_product.gatewayId != settings.GATEWAY_ID or
                data_product.ownerName != request.user.username):
            raise PermissionDenied()
        data_products_helper.delete(request, data_product)
        return HttpResponse(status=204)
    except ObjectDoesNotExist as e:
        raise Http404(str(e)) from e


class UserProfileViewSet(mixins.RetrieveModelMixin,
                         mixins.ListModelMixin,
                         GenericAPIBackedViewSet):
    serializer_class = serializers.UserProfileSerializer

    def get_list(self):
        user_profile_client = self.request.profile_service['user_profile']
        return user_profile_client.getAllUserProfilesInGateway(
            self.authz_token, self.gateway_id, 0, -1)

    def get_instance(self, lookup_value):
        user_profile_client = self.request.profile_service['user_profile']
        return user_profile_client.getUserProfileById(
            self.authz_token, self.request.user.username, self.gateway_id)


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
        # Update the creationTime field on the group resource profile
        new_group_resource_profile = self.request.airavata_client.getGroupResourceProfile(
            self.authz_token, group_resource_profile_id)
        group_resource_profile.creationTime = new_group_resource_profile.creationTime

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

    def get_instance(self, lookup_value):
        users = {}
        # Only load *directly* granted permissions since these are the only
        # ones that can be edited
        # Load accessible users in order of permission precedence: users that
        # have WRITE permission should also have READ
        users.update(self._load_directly_accessible_users(
            lookup_value, ResourcePermissionType.READ))
        users.update(self._load_directly_accessible_users(
            lookup_value, ResourcePermissionType.WRITE))
        users.update(self._load_directly_accessible_users(
            lookup_value, ResourcePermissionType.MANAGE_SHARING))
        owner_ids = self._load_directly_accessible_users(
            lookup_value, ResourcePermissionType.OWNER)
        # Assume that there is one and only one DIRECT owner (there may be one
        # or more INDIRECT cascading owners, which would the owners of the
        # ancestor entities, but getAllDirectlyAccessibleUsers does not return
        # indirectly cascading owners)
        owner_id = list(owner_ids.keys())[0]
        # Remove owner from the users list
        del users[owner_id]
        user_list = []
        for user_id in users:
            user_list.append({'user': self._load_user_profile(user_id),
                              'permissionType': users[user_id]})
        groups = {}
        groups.update(self._load_directly_accessible_groups(
            lookup_value, ResourcePermissionType.READ))
        groups.update(self._load_directly_accessible_groups(
            lookup_value, ResourcePermissionType.WRITE))
        groups.update(self._load_directly_accessible_groups(
            lookup_value, ResourcePermissionType.MANAGE_SHARING))
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

    def _load_directly_accessible_users(self, entity_id, permission_type):
        users = self.request.airavata_client.getAllDirectlyAccessibleUsers(
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

    def _load_directly_accessible_groups(self, entity_id, permission_type):
        groups = self.request.airavata_client.getAllDirectlyAccessibleGroups(
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
        if len(shared_entity['_user_grant_manage_sharing_permission']) > 0:
            self._share_with_users(
                entity_id, ResourcePermissionType.MANAGE_SHARING,
                shared_entity['_user_grant_manage_sharing_permission'])
        if len(shared_entity['_user_revoke_read_permission']) > 0:
            self._revoke_from_users(
                entity_id, ResourcePermissionType.READ,
                shared_entity['_user_revoke_read_permission'])
        if len(shared_entity['_user_revoke_write_permission']) > 0:
            self._revoke_from_users(
                entity_id, ResourcePermissionType.WRITE,
                shared_entity['_user_revoke_write_permission'])
        if len(shared_entity['_user_revoke_manage_sharing_permission']) > 0:
            self._revoke_from_users(
                entity_id, ResourcePermissionType.MANAGE_SHARING,
                shared_entity['_user_revoke_manage_sharing_permission'])
        if len(shared_entity['_group_grant_read_permission']) > 0:
            self._share_with_groups(
                entity_id, ResourcePermissionType.READ,
                shared_entity['_group_grant_read_permission'])
        if len(shared_entity['_group_grant_write_permission']) > 0:
            self._share_with_groups(
                entity_id, ResourcePermissionType.WRITE,
                shared_entity['_group_grant_write_permission'])
        if len(shared_entity['_group_grant_manage_sharing_permission']) > 0:
            self._share_with_groups(
                entity_id, ResourcePermissionType.MANAGE_SHARING,
                shared_entity['_group_grant_manage_sharing_permission'])
        if len(shared_entity['_group_revoke_read_permission']) > 0:
            self._revoke_from_groups(
                entity_id, ResourcePermissionType.READ,
                shared_entity['_group_revoke_read_permission'])
        if len(shared_entity['_group_revoke_write_permission']) > 0:
            self._revoke_from_groups(
                entity_id, ResourcePermissionType.WRITE,
                shared_entity['_group_revoke_write_permission'])
        if len(shared_entity['_group_revoke_manage_sharing_permission']) > 0:
            self._revoke_from_groups(
                entity_id, ResourcePermissionType.MANAGE_SHARING,
                shared_entity['_group_revoke_manage_sharing_permission'])

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

    @detail_route(methods=['get'])
    def all(self, request, entity_id=None):
        """Load direct plus indirectly (inherited) shared permissions."""
        users = {}
        # Load accessible users in order of permission precedence: users that
        # have WRITE permission should also have READ
        users.update(self._load_accessible_users(
            entity_id, ResourcePermissionType.READ))
        users.update(self._load_accessible_users(
            entity_id, ResourcePermissionType.WRITE))
        users.update(self._load_accessible_users(
            entity_id, ResourcePermissionType.MANAGE_SHARING))
        owner_ids = self._load_accessible_users(
            entity_id, ResourcePermissionType.OWNER)
        # Assume that there is one and only one DIRECT owner (there may be one
        # or more INDIRECT cascading owners, which would the owners of the
        # ancestor entities, but getAllAccessibleUsers does not return
        # indirectly cascading owners)
        owner_id = list(owner_ids.keys())[0]
        # Remove owner from the users list
        del users[owner_id]
        user_list = []
        for user_id in users:
            user_list.append({'user': self._load_user_profile(user_id),
                              'permissionType': users[user_id]})
        groups = {}
        groups.update(self._load_accessible_groups(
            entity_id, ResourcePermissionType.READ))
        groups.update(self._load_accessible_groups(
            entity_id, ResourcePermissionType.WRITE))
        groups.update(self._load_accessible_groups(
            entity_id, ResourcePermissionType.MANAGE_SHARING))
        group_list = []
        for group_id in groups:
            group_list.append({'group': self._load_group(group_id),
                               'permissionType': groups[group_id]})
        shared_entity = {'entityId': entity_id,
                         'userPermissions': user_list,
                         'groupPermissions': group_list,
                         'owner': self._load_user_profile(owner_id)}
        serializer = self.serializer_class(
            shared_entity, context={'request': request})
        return Response(serializer.data)


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

    def get_list(self):
        return self.request.airavata_client.listAllParsers(
            self.authz_token, settings.GATEWAY_ID)

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getParser(
            self.authz_token, lookup_value, settings.GATEWAY_ID)

    def perform_create(self, serializer):
        parser = serializer.save()
        self.request.airavata_client.saveParser(self.authz_token, parser)

    def perform_update(self, serializer):
        parser = serializer.save()
        self.request.airavata_client.saveParser(self.authz_token, parser)


class UserStoragePathView(APIView):
    serializer_class = serializers.UserStoragePathSerializer

    def get(self, request, path="/", format=None):
        return self._create_response(request, path)

    def post(self, request, path="/", format=None):
        if not data_products_helper.dir_exists(request, path):
            data_products_helper.create_user_dir(request, path)

        data_product = None
        # Handle direct upload
        if 'file' in request.FILES:
            user_file = request.FILES['file']
            data_product = data_products_helper.save(
                request, path, user_file, content_type=user_file.content_type)
        # Handle a tus upload
        elif 'uploadURL' in request.POST:
            uploadURL = request.POST['uploadURL']

            def move_file(file_path, file_name, file_type):
                return data_products_helper.move_from_filepath(
                    request, file_path, path, name=file_name,
                    content_type=file_type)
            data_product = tus.move_tus_upload(uploadURL, move_file)
        return self._create_response(request, path, uploaded=data_product)

    def delete(self, request, path="/", format=None):
        data_products_helper.delete_dir(request, path)
        return Response(status=204)

    def _create_response(self, request, path, uploaded=None):
        directories, files = data_products_helper.listdir(request, path)
        data = {
            'directories': directories,
            'files': files
        }
        if uploaded is not None:
            data['uploaded'] = uploaded
        data['parts'] = self._split_path(path)
        serializer = self.serializer_class(data, context={'request': request})
        return Response(serializer.data)

    def _split_path(self, path):
        head, tail = os.path.split(path)
        if head != "":
            return self._split_path(head) + [tail]
        elif tail != "":
            return [tail]
        else:
            return []


class WorkspacePreferencesView(APIView):
    serializer_class = serializers.WorkspacePreferencesSerializer

    def get(self, request, format=None):
        helper = helpers.WorkspacePreferencesHelper()
        workspace_preferences = helper.get(request)
        serializer = self.serializer_class(
            workspace_preferences, context={'request': request})
        return Response(serializer.data)


class ManageNotificationViewSet(APIBackedViewSet):
    serializer_class = serializers.NotificationSerializer
    lookup_field = 'notification_id'

    def get_instance(self, lookup_value):
        return self.request.airavata_client.getNotification(
            self.authz_token, settings.GATEWAY_ID, lookup_value)

    def get_list(self):
        return self.request.airavata_client.getAllNotifications(
            self.authz_token, self.gateway_id)

    def perform_destroy(self, instance):
        self.request.airavata_client.deleteNotification(
            self.authz_token, settings.GATEWAY_ID, instance.notificationId)

    def perform_create(self, serializer):
        notification = serializer.save(gatewayId=self.gateway_id)
        notificationId = self.request.airavata_client.createNotification(
            self.authz_token, notification)
        notification.notificationId = notificationId

    def perform_update(self, serializer):
        notification = serializer.save()
        self.request.airavata_client.updateNotification(
            self.authz_token, notification)


class AckNotificationViewSet(APIView):

    def get(self, request, format=None):
        if 'id' in request.GET:
            notification_id = request.GET['id']
            try:
                notification = models.User_Notifications.objects.get(
                    notification_id=notification_id,
                    username=request.user.username)
                notification.is_read = True
                notification.save()
            except ObjectDoesNotExist:
                models.User_Notifications.objects.create(
                    username=request.user.username,
                    notification_id=notification.notificationId)
        return HttpResponse(status=204)


class IAMUserViewSet(mixins.RetrieveModelMixin,
                     mixins.UpdateModelMixin,
                     mixins.ListModelMixin,
                     mixins.DestroyModelMixin,
                     GenericAPIBackedViewSet):
    serializer_class = serializers.IAMUserProfile
    pagination_class = APIResultPagination
    permission_classes = (IsInAdminsGroupPermission,)
    lookup_field = 'user_id'

    def get_list(self):
        search = self.request.GET.get('search', None)

        convert_user_profile = self._convert_user_profile

        class IAMUsersResultIterator(APIResultIterator):
            def get_results(self, limit=-1, offset=0):
                return map(convert_user_profile,
                           iam_admin_client.get_users(offset, limit, search))
        return IAMUsersResultIterator()

    def get_instance(self, lookup_value):
        return self._convert_user_profile(
            iam_admin_client.get_user(lookup_value))

    def perform_update(self, serializer):
        managed_user_profile = serializer.save()
        group_manager_client = self.request.profile_service['group_manager']
        user_profile_client = self.request.profile_service['user_profile']
        user_id = managed_user_profile['airavataInternalUserId']
        added_groups = []
        for group_id in managed_user_profile['_added_group_ids']:
            group = group_manager_client.getGroup(self.authz_token, group_id)
            group_manager_client.addUsersToGroup(
                self.authz_token, [user_id], group_id)
            added_groups.append(group)
        if len(added_groups) > 0:
            user_profile = user_profile_client.getUserProfileById(
                self.authz_token,
                managed_user_profile['userId'],
                settings.GATEWAY_ID)
            signals.user_added_to_group.send(
                sender=self.__class__,
                user=user_profile,
                groups=added_groups,
                request=self.request)
        for group_id in managed_user_profile['_removed_group_ids']:
            group_manager_client.removeUsersFromGroup(
                self.authz_token, [user_id], group_id)

    def perform_destroy(self, instance):
        iam_admin_client.delete_user(instance['userId'])

    @detail_route(methods=['post'])
    def enable(self, request, user_id=None):
        iam_admin_client.enable_user(user_id)
        instance = self.get_instance(user_id)
        serializer = self.serializer_class(instance=instance,
                                           context={'request': request})
        return Response(serializer.data)

    def _convert_user_profile(self, user_profile):
        user_profile_client = self.request.profile_service['user_profile']
        group_manager_client = self.request.profile_service['group_manager']
        airavata_user_profile_exists = user_profile_client.doesUserExist(
            self.authz_token, user_profile.userId, self.gateway_id)
        groups = []
        if airavata_user_profile_exists:
            groups = group_manager_client.getAllGroupsUserBelongs(
                self.authz_token, user_profile.airavataInternalUserId)
        return {
            'airavataInternalUserId': user_profile.airavataInternalUserId,
            'userId': user_profile.userId,
            'gatewayId': user_profile.gatewayId,
            'email': user_profile.emails[0],
            'firstName': user_profile.firstName,
            'lastName': user_profile.lastName,
            'enabled': user_profile.State == Status.ACTIVE,
            'emailVerified': (user_profile.State == Status.CONFIRMED or
                              user_profile.State == Status.ACTIVE),
            'airavataUserProfileExists': airavata_user_profile_exists,
            'creationTime': user_profile.creationTime,
            'groups': groups
        }


class ExperimentStatisticsView(APIView):
    # TODO: restrict to only Admins or Read Only Admins group members
    serializer_class = serializers.ExperimentStatisticsSerializer

    def get(self, request, format=None):
        if 'fromTime' in request.GET:
            from_time = view_utils.convert_utc_iso8601_to_date(
                request.GET['fromTime']).timestamp() * 1000
        else:
            from_time = (datetime.utcnow() -
                         timedelta(days=7)).timestamp() * 1000
        from_time = int(from_time)
        if 'toTime' in request.GET:
            to_time = view_utils.convert_utc_iso8601_to_date(
                request.GET['toTime']).timestamp() * 1000
        else:
            to_time = datetime.utcnow().timestamp() * 1000
        to_time = int(to_time)
        username = request.GET.get('userName', None)
        application_name = request.GET.get('applicationName', None)
        resource_hostname = request.GET.get('resourceHostName', None)
        statistics = request.airavata_client.getExperimentStatistics(
            request.authz_token, settings.GATEWAY_ID, from_time, to_time,
            username, application_name, resource_hostname)
        serializer = self.serializer_class(
            statistics, context={'request': request})
        return Response(serializer.data)


class UnverifiedEmailUserViewSet(mixins.ListModelMixin,
                                 mixins.RetrieveModelMixin,
                                 GenericAPIBackedViewSet):
    serializer_class = serializers.UnverifiedEmailUserProfile
    pagination_class = APIResultPagination
    permission_classes = (IsInAdminsGroupPermission,)
    lookup_field = 'user_id'

    def get_list(self):
        get_users = self._get_unverified_email_user_profiles

        class UnverifiedEmailUsersResultIterator(APIResultIterator):
            def get_results(self, limit=-1, offset=0):
                return get_users(limit, offset)
        return UnverifiedEmailUsersResultIterator()

    def get_instance(self, lookup_value):
        users = self._get_unverified_email_user_profiles(
            limit=1, username=lookup_value)
        if len(users) == 0:
            raise Http404("No unverified email record found for user {}"
                          .format(lookup_value))
        else:
            return users[0]

    def _get_unverified_email_user_profiles(
            self, limit=-1, offset=0, username=None):
        unverified_emails = EmailVerification.objects.filter(
            verified=False).order_by('username').values('username').distinct()
        if username is not None:
            unverified_emails = unverified_emails.filter(username=username)
        if limit > 0:
            unverified_emails = unverified_emails[offset:offset + limit]
        results = []
        for unverified_email in unverified_emails:
            unverified_username = unverified_email['username']
            if iam_admin_client.is_user_exist(unverified_username):
                user_profile = iam_admin_client.get_user(unverified_username)
                if (user_profile.State == Status.CONFIRMED or
                        user_profile.State == Status.ACTIVE):
                    # TODO: test this
                    EmailVerification.objects.filter(
                        username=unverified_username).update(
                        verified=True)
                    continue
                results.append({
                    'userId': user_profile.userId,
                    'gatewayId': user_profile.gatewayId,
                    'email': user_profile.emails[0],
                    'firstName': user_profile.firstName,
                    'lastName': user_profile.lastName,
                    'enabled': user_profile.State == Status.ACTIVE,
                    'emailVerified': (user_profile.State == Status.CONFIRMED or
                                      user_profile.State == Status.ACTIVE),
                    'creationTime': user_profile.creationTime,
                })
            else:
                # Delete the EmailVerification records since that user no
                # longer exists in the IAM service
                EmailVerification.objects.filter(
                    username=unverified_username).delete()
        return results


class LogRecordConsumer(APIView):
    serializer_class = serializers.LogRecordSerializer

    def post(self, request, format=None):
        serializer = self.serializer_class(data=request.data)
        serializer.is_valid(raise_exception=True)
        log_record = serializer.validated_data
        log_level = getattr(logging, log_record['level'], None)
        if log_level is not None:
            stacktrace = "".join(
                map(lambda a: "\n    " + a, log_record['stacktrace']))
            log.log(log_level,
                    "Frontend error: {}: {}\nstacktrace: {}".format(
                        log_record['message'],
                        json.dumps(log_record['details'], indent=4),
                        stacktrace), extra={'request': request})
        return Response(serializer.data)


class SettingsAPIView(APIView):
    serializer_class = serializers.SettingsSerializer

    def get(self, request, format=None):
        data = {
            'fileUploadMaxFileSize': settings.FILE_UPLOAD_MAX_FILE_SIZE,
            'tusEndpoint': settings.TUS_ENDPOINT,
            'pgaUrl': settings.PGA_URL
        }
        serializer = self.serializer_class(
            data, context={'request': request})
        return Response(serializer.data)


class APIServerStatusCheckView(APIView):

    def get(self, request, format=None):
        try:
            request.airavata_client.getUserProjects(request.authz_token,
                                                    settings.GATEWAY_ID,
                                                    request.user.username,
                                                    1,  # limit
                                                    0)  # offset
            data = {
                "apiServerUp": True
            }
        except Exception as e:
            log.debug("API server status check failed: {}".format(str(e)))
            data = {
                "apiServerUp": False
            }
        return Response(data)


def notebook_output_view(request):
    provider_id = request.GET['provider-id']
    experiment_id = request.GET['experiment-id']
    experiment_output_name = request.GET['experiment-output-name']
    data = output_views.generate_data(request,
                                      provider_id,
                                      experiment_output_name,
                                      experiment_id)
    return HttpResponse(data['output'])


def html_output_view(request):
    provider_id = request.GET['provider-id']
    experiment_id = request.GET['experiment-id']
    experiment_output_name = request.GET['experiment-output-name']
    data = output_views.generate_data(request,
                                      provider_id,
                                      experiment_output_name,
                                      experiment_id)
    return JsonResponse(data)


def image_output_view(request):
    provider_id = request.GET['provider-id']
    experiment_id = request.GET['experiment-id']
    experiment_output_name = request.GET['experiment-output-name']
    data = output_views.generate_data(request,
                                      provider_id,
                                      experiment_output_name,
                                      experiment_id)
    # data should contain 'image' as a file-like object or raw bytes with the
    # file data and 'mime-type' with the images mimetype
    return HttpResponse(data['image'], content_type=data['mime-type'])
