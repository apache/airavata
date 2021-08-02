
import json
import logging
from urllib.parse import urlparse

from airavata.model.application.io.ttypes import DataType
from airavata_django_portal_sdk import user_storage as user_storage_sdk
from django.contrib.auth.decorators import login_required
from django.shortcuts import render
from django.utils.module_loading import import_string
from rest_framework.renderers import JSONRenderer

from django_airavata.apps.api import models
from django_airavata.apps.api.views import (
    ApplicationModuleViewSet,
    ExperimentSearchViewSet,
    FullExperimentViewSet,
    ProjectViewSet
)

logger = logging.getLogger(__name__)


@login_required
def experiments_list(request):
    request.active_nav_item = 'experiments'

    response = ExperimentSearchViewSet.as_view({'get': 'list'})(request)
    if response.status_code != 200:
        raise Exception("Failed to load experiments list: {}".format(
            response.data['detail']))
    experiments_json = JSONRenderer().render(response.data).decode('utf-8')
    return render(request, 'django_airavata_workspace/experiments_list.html', {
        'bundle_name': 'experiment-list',
        'experiments_data': experiments_json
    })


@login_required
def dashboard(request):
    request.active_nav_item = 'dashboard'
    return render(request, 'django_airavata_workspace/dashboard.html', {
        'bundle_name': 'dashboard',
        'sidebar': True,
    })


@login_required
def projects_list(request):
    request.active_nav_item = 'projects'

    response = ProjectViewSet.as_view({'get': 'list'})(request)
    if response.status_code != 200:
        raise Exception("Failed to load projects list: {}".format(
            response.data['detail']))
    projects_json = JSONRenderer().render(response.data).decode('utf-8')

    return render(request, 'django_airavata_workspace/projects_list.html', {
        'bundle_name': 'project-list',
        'projects_data': projects_json
    })


@login_required
def edit_project(request, project_id):
    request.active_nav_item = 'projects'

    return render(request, 'django_airavata_workspace/edit_project.html', {
        'bundle_name': 'edit-project',
        'project_id': project_id
    })


@login_required
def create_experiment(request, app_module_id):
    request.active_nav_item = 'dashboard'

    # User input files can be passed as query parameters
    # <input name>=<path/to/user_file>
    # and also as data product URIs
    # <input name>=<data product URI>
    app_interface = ApplicationModuleViewSet.as_view(
        {'get': 'application_interface'})(request, app_module_id=app_module_id)
    if app_interface.status_code != 200:
        raise Exception("Failed to load application module data: {}".format(
            app_interface.data['detail']))
    user_input_values = {}
    for app_input in app_interface.data['applicationInputs']:
        if (app_input['type'] ==
                DataType.URI and app_input['name'] in request.GET):
            user_file_value = request.GET[app_input['name']]
            try:
                user_file_url = urlparse(user_file_value)
                if user_file_url.scheme == 'airavata-dp':
                    dp_uri = user_file_value
                    try:
                        data_product = request.airavata_client.getDataProduct(
                            request.authz_token, dp_uri)
                        if user_storage_sdk.exists(request, data_product):
                            user_input_values[app_input['name']] = dp_uri
                    except Exception:
                        logger.exception(
                            f"Failed checking data product uri: {dp_uri}")
            except ValueError:
                logger.exception(f"Invalid user file value: {user_file_value}")
        elif (app_input['type'] == DataType.STRING and
              app_input['name'] in request.GET):
            name = app_input['name']
            user_input_values[name] = request.GET[name]
    context = {
        'bundle_name': 'create-experiment',
        'app_module_id': app_module_id,
        'user_input_values': json.dumps(user_input_values)
    }
    if 'experiment-data-dir' in request.GET:
        context['experiment_data_dir'] = request.GET['experiment-data-dir']

    template_path = 'django_airavata_workspace/create_experiment.html'
    # Apply a custom application template if it exists
    custom_template_path, custom_context = get_custom_template(request, app_module_id)
    if custom_template_path is not None:
        logger.debug(f"Applying custom application template {custom_template_path}")
        template_path = custom_template_path
        context.update(custom_context)

    return render(request, template_path, context)


@login_required
def edit_experiment(request, experiment_id):
    request.active_nav_item = 'experiments'

    experiment = request.airavata_client.getExperiment(request.authz_token, experiment_id)
    applicationInterface = request.airavata_client.getApplicationInterface(request.authz_token, experiment.executionId)
    app_module_id = applicationInterface.applicationModules[0]
    context = {
        'bundle_name': 'edit-experiment',
        'experiment_id': experiment_id,
        'app_module_id': app_module_id,
    }
    template_path = 'django_airavata_workspace/edit_experiment.html'
    # Apply a custom application template if it exists
    custom_template_path, custom_context = get_custom_template(request, app_module_id)
    if custom_template_path is not None:
        logger.debug(f"Applying custom application template {custom_template_path}")
        template_path = custom_template_path
        context.update(custom_context)

    return render(request, template_path, context)


def get_custom_template(request, app_module_id):
    template_path = None
    context = {}
    query = models.ApplicationTemplate.objects.filter(application_module_id=app_module_id)
    if query.exists():
        application_template = query.get()
        template_path = application_template.template_path
        for context_processor in application_template.context_processors.all():
            context_processor = import_string(context_processor.callable_path)
            context.update(context_processor(request))
    return template_path, context


@login_required
def view_experiment(request, experiment_id):
    request.active_nav_item = 'experiments'

    launching = json.loads(request.GET.get('launching', 'false'))
    response = FullExperimentViewSet.as_view(
        {'get': 'retrieve'})(request, experiment_id=experiment_id)
    if response.status_code != 200:
        raise Exception("Failed to load experiment data: {}".format(
            response.data['detail']))
    full_experiment_json = JSONRenderer().render(response.data).decode('utf-8')

    return render(request, 'django_airavata_workspace/view_experiment.html', {
        'bundle_name': 'view-experiment',
        'full_experiment_data': full_experiment_json,
        'launching': json.dumps(launching),
    })


@login_required
def user_storage(request):
    request.active_nav_item = 'storage'
    return render(request, 'django_airavata_workspace/base.html', {
        'bundle_name': 'user-storage'
    })
