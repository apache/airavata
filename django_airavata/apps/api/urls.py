import logging

from django.conf.urls import include, url
from rest_framework import routers

from . import views

logger = logging.getLogger(__name__)

router = routers.DefaultRouter()
router.register(r'projects', views.ProjectViewSet, base_name='project')
router.register(r'experiments', views.ExperimentViewSet,
                base_name='experiment')
router.register(r'full-experiments', views.FullExperimentViewSet,
                base_name='full-experiment')
router.register(r'experiment-search', views.ExperimentSearchViewSet,
                base_name='experiment-search')
router.register(r'groups', views.GroupViewSet, base_name='group')
router.register(r'application-interfaces', views.ApplicationInterfaceViewSet,
                base_name='application-interface')
router.register(r'applications', views.ApplicationModuleViewSet,
                base_name='application')
router.register(r'application-deployments', views.ApplicationDeploymentViewSet,
                base_name='application-deployment')
router.register(r'user-profiles', views.UserProfileViewSet,
                base_name='user-profile')
router.register(r'group-resource-profiles', views.GroupResourceProfileViewSet,
                base_name='group-resource-profile')
router.register(r'shared-entities', views.SharedEntityViewSet,
                base_name='shared-entity')
router.register(r'compute-resources', views.ComputeResourceViewSet,
                base_name='compute-resource')
router.register(r'storage-resources', views.StorageResourceViewSet,
                base_name='storage-resource')
router.register(r'credential-summaries', views.CredentialSummaryViewSet,
                base_name='credential-summary')
router.register(r'gateway-resource-profiles',
                views.GatewayResourceProfileViewSet,
                base_name='gateway-resource-profile')
router.register(r'storage-preferences',
                views.StoragePreferenceViewSet,
                base_name='storage-preference')
router.register(r'parsers', views.ParserViewSet, base_name='parser')
router.register(r'managed-user-profiles', views.ManagedUserViewSet,
                base_name='managed-user-profile')
router.register(r'manage-notifications', views.ManageNotificationViewSet,
                base_name='manage-notifications')

app_name = 'django_airavata_api'
urlpatterns = [
    url(r'^', include(router.urls)),
    url(r'^upload$', views.upload_input_file, name='upload_input_file'),
    url(r'^download', views.download_file, name='download_file'),
    url(r'^delete-file$', views.delete_file, name='delete_file'),
    url(r'^data-products', views.DataProductView.as_view(),
        name='data-products-detail'),
    url(r'^job/submission/local', views.LocalJobSubmissionView.as_view(),
        name="local_job_submission"),
    url(r'^job/submission/cloud', views.CloudJobSubmissionView.as_view(),
        name="cloud_job_submission"),
    url(r'^job/submission/globus', views.GlobusJobSubmissionView.as_view(),
        name="globus_job_submission"),
    url(r'^job/submission/ssh', views.SshJobSubmissionView.as_view(),
        name="ssh_job_submission"),
    url(r'^job/submission/unicore', views.UnicoreJobSubmissionView.as_view(),
        name="unicore_job_submission"),
    url(r'^data/movement/gridftp', views.GridFtpDataMovementView.as_view(),
        name="grid_ftp_data_movement"),
    url(r'^data/movement/local', views.LocalDataMovementView.as_view(),
        name="local_ftp_data_movement"),
    url(r'^data/movement/unicore', views.UnicoreDataMovementView.as_view(),
        name="unicore_ftp_data_movement"),
    url(r'^data/movement/scp', views.ScpDataMovementView.as_view(),
        name="scp_ftp_data_movement"),
    url(r'^gateway-resource-profile',
        views.GetCurrentGatewayResourceProfile.as_view(),
        name="current_gateway_resource_profile"),
    url(r'^workspace-preferences',
        views.WorkspacePreferencesView.as_view(),
        name="workspace-preferences"),
    url(r'^user-storage/~/(?P<path>.*)$',
        views.UserStoragePathView.as_view(),
        name="user-storage-items"),
    url(r'^experiment-statistics',
        views.ExperimentStatisticsView.as_view(),
        name="experiment-statistics"),
    url(r'ack-notifications/<slug:id>/', views.AckNotificationViewSet.as_view(), name="ack-notifications"),
    url(r'ack-notifications/', views.AckNotificationViewSet.as_view(), name="ack-notifications")

]

if logger.isEnabledFor(logging.DEBUG):
    for router_url in router.urls:
        logger.debug("router url: {}".format(router_url))
