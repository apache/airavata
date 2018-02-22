
from . import views

from django.conf.urls import include, url
from rest_framework import routers

import logging

logger = logging.getLogger(__name__)

router = routers.DefaultRouter()
router.register(r'projects', views.ProjectViewSet, base_name='project')
router.register(r'experiments', views.ExperimentViewSet, base_name='experiment')
router.register(r'full-experiments', views.FullExperimentViewSet, base_name='full-experiment')
router.register(r'experiment-search', views.ExperimentSearchViewSet, base_name='experiment-search')
router.register(r'groups', views.GroupViewSet, base_name='group')
router.register(r'new/application/module', views.RegisterApplicationModule, base_name='register_app_module')
router.register(r'application-interfaces', views.ApplicationInterfaceViewSet, base_name='application-interface')
router.register(r'applications', views.ApplicationModuleViewSet, base_name='application')
router.register(r'application-deployments', views.ApplicationDeploymentViewSet, base_name='application-deployment')
router.register(r'user-profiles', views.UserProfileViewSet,
                base_name='user-profile')

app_name = 'django_airavata_api'
urlpatterns = [
    url(r'^', include(router.urls)),
    url(r'^new/application/module$', views.RegisterApplicationModule.as_view(), name='register_application_module'),
    url(r'^new/application/interface$', views.RegisterApplicationInterface.as_view(), name='register_application_interface'),
    url(r'^new/application/deployment$', views.RegisterApplicationDeployments.as_view(), name='register_application_deployments'),
    url(r'^compute/resources$', views.ComputeResourceList.as_view(), name="compute_resources"),
    url(r'^compute/resource/details$', views.ComputeResourceDetails.as_view(), name="compute_resource_details"),
    url(r'^compute/resource/queues', views.ComputeResourcesQueues.as_view(), name="compute_resource_queues"),
    url(r'^application/interfaces$', views.ApplicationInterfaceList.as_view(), name="app_interfaces"),
    url(r'^application/interface$', views.FetchApplicationInterface.as_view(), name="app_interface"),
    url(r'^application/deployment$', views.FetchApplicationDeployment.as_view(), name="app_deployment"),
    url(r'^credentials/ssh/keys', views.FetchSSHPubKeys.as_view(), name="ssh_keys"),
    url(r'^credentials/ssh/key/delete', views.DeleteSSHPubKey.as_view(), name="ssh_key_deletion"),
    url(r'^credentials/ssh/key/create', views.GenerateRegisterSSHKeys.as_view(), name="ssh_key_creation"),
    url(r'^upload$', views.upload_input_file, name='upload_input_file'),
    url(r'^download', views.download_file, name='download_file'),
]


if logger.isEnabledFor(logging.DEBUG):
    for url in router.urls:
        logger.debug("router url: {}".format(url))
