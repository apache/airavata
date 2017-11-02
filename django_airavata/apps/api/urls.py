
from . import views

from django.conf.urls import include, url
from rest_framework import routers

import logging

logger = logging.getLogger(__name__)

router = routers.DefaultRouter()
router.register(r'projects', views.ProjectViewSet, base_name='project')
router.register(r'new/application/module', views.RegisterApplicationModule, base_name='register_app_module')
router.register(r'experiments', views.ApplicationList, base_name='experiments')
router.register(r'application-interface', views.ApplicationInterfaceViewSet, base_name='application-interface')

app_name = 'django_airavata_api'
urlpatterns = [
    url(r'^', include(router.urls)),
    url(r'^applications/$', views.ApplicationList.as_view(), name='application_module_list'),
    url(r'^new/application/module$', views.RegisterApplicationModule.as_view(), name='register_application_module'),
    url(r'^new/application/interface', views.RegisterApplicationInterface.as_view(), name='register_application_interface'),
    url(r'^new/application/deployment', views.RegisterApplicationDeployments.as_view(), name='register_application_deployments'),
]


if logger.isEnabledFor(logging.DEBUG):
    for url in router.urls:
        logger.debug("router url: {}".format(url))
