
import json
import logging

from django.contrib.auth.decorators import login_required
from django.shortcuts import render
from rest_framework.renderers import JSONRenderer

from airavata.model.application.io.ttypes import DataType
from django_airavata.apps.api import data_products_helper
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
    experiments_json = JSONRenderer().render(response.data)
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
    projects_json = JSONRenderer().render(response.data)

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
    app_interface = ApplicationModuleViewSet.as_view(
        {'get': 'application_interface'})(request, app_module_id=app_module_id)
    if app_interface.status_code != 200:
        raise Exception("Failed to load application module data: {}".format(
            app_interface.data['detail']))
    user_input_files = {}
    for app_input in app_interface.data['applicationInputs']:
        if (app_input['type'] ==
                DataType.URI and app_input['name'] in request.GET):
            user_file_path = request.GET[app_input['name']]
            data_product_uri = data_products_helper.user_file_exists(
                request, user_file_path)
            if data_product_uri is not None:
                user_input_files[app_input['name']] = data_product_uri
    context = {
        'bundle_name': 'create-experiment',
        'app_module_id': app_module_id,
        'user_input_files': json.dumps(user_input_files)
    }
    if 'experiment-data-dir' in request.GET:
        context['experiment_data_dir'] = request.GET['experiment-data-dir']

    return render(request,
                  'django_airavata_workspace/create_experiment.html',
                  context)


@login_required
def edit_experiment(request, experiment_id):
    request.active_nav_item = 'experiments'

    return render(request,
                  'django_airavata_workspace/edit_experiment.html',
                  {'bundle_name': 'edit-experiment',
                   'experiment_id': experiment_id})


@login_required
def view_experiment(request, experiment_id):
    request.active_nav_item = 'experiments'

    launching = json.loads(request.GET.get('launching', 'false'))
    response = FullExperimentViewSet.as_view(
        {'get': 'retrieve'})(request, experiment_id=experiment_id)
    if response.status_code != 200:
        raise Exception("Failed to load experiment data: {}".format(
            response.data['detail']))
    full_experiment_json = JSONRenderer().render(response.data)

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
