
import json
import logging
import os
from urllib.parse import urlparse

from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.core.files.storage import FileSystemStorage
from django.http import JsonResponse
from django.shortcuts import render
from rest_framework.renderers import JSONRenderer

from airavata.model.application.io.ttypes import DataType
from airavata.model.data.replica.ttypes import (
    DataProductModel,
    DataProductType,
    DataReplicaLocationModel,
    ReplicaLocationCategory,
    ReplicaPersistentType
)
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


experiment_data_storage = FileSystemStorage(
    location=settings.GATEWAY_DATA_STORE_DIR)


@login_required
def upload_input_file(request):
    try:
        # Save input file to username/project name/exp name/filename
        username = request.user.username
        project_id = request.POST['project-id']
        project = request.airavata_client.getProject(
            request.authz_token, project_id)
        exp_name = request.POST['experiment-name']
        input_file = request.FILES['file']
        exp_dir = os.path.join(
            experiment_data_storage.get_valid_name(username),
            experiment_data_storage.get_valid_name(project.name),
            experiment_data_storage.get_valid_name(exp_name))
        file_path = os.path.join(
            exp_dir,
            experiment_data_storage.get_valid_name(input_file.name))
        input_file_name = experiment_data_storage.save(file_path, input_file)
        input_file_fullpath = experiment_data_storage.path(input_file_name)
        # Register DataProductModel with DataReplicaLocationModel
        data_product = DataProductModel()
        data_product.gatewayId = settings.GATEWAY_ID
        data_product.ownerName = username
        data_product.productName = input_file.name
        data_product.dataProductType = DataProductType.FILE
        data_replica_location = DataReplicaLocationModel()
        data_replica_location.storageResourceId = \
            settings.GATEWAY_DATA_STORE_RESOURCE_ID
        data_replica_location.replicaName = \
            "{} gateway data store copy".format(input_file.name)
        data_replica_location.replicaLocationCategory = \
            ReplicaLocationCategory.GATEWAY_DATA_STORE
        data_replica_location.replicaPersistentType = \
            ReplicaPersistentType.TRANSIENT
        hostname = urlparse(request.build_absolute_uri()).hostname
        data_replica_location.filePath = \
            "file://{}:{}".format(hostname, input_file_fullpath)
        data_product.replicaLocations = [data_replica_location]
        data_product_uri = request.airavata_client.registerDataProduct(
            request.authz_token, data_product)
        return JsonResponse({'uploaded': True,
                             'data-product-uri': data_product_uri})
    except Exception as e:
        resp = JsonResponse({'uploaded': False, 'error': str(e)})
        resp.status_code = 500
        return resp
