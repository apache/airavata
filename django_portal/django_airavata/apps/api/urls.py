
from . import views

from django.conf.urls import include, url
from rest_framework import routers
from rest_framework.urlpatterns import format_suffix_patterns

router = routers.SimpleRouter()
router.register(r'projects', views.ProjectViewSet, base_name='project')
router.register(r'new/application/module', views.RegisterApplicationModule, base_name='register_app_module')
router.register(r'experiments', views.ApplicationList, base_name='experiments')

urlpatterns = [
    url(r'^$', views.api_root),
    # url(r'^projects/$', views.ProjectList.as_view(), name='api_project_list'),
    # More specific, longer URLs should come before less specific, shorter ones
    # since the regular expression for project_id allows any character, even '/'
    url(r'^projects/(?P<project_id>.+)/experiments/$', views.ProjectExperimentList.as_view(), name='api_project_experiments_list'),
    # url(r'^projects/(?P<project_id>.+)/$', views.ProjectDetail.as_view(), name='api_project_detail'),
    url(r'^experiments/$', views.ExperimentList.as_view(), name='api_experiment_list'),
    url(r'^', include(router.urls)),
    url(r'^applications/$', views.ApplicationList.as_view(), name='application_module_list'),
    url(r'^new/application/module$', views.RegisterApplicationModule.as_view(), name='register_application_module'),
    url(r'^new/application/interface', views.RegisterApplicationInterface.as_view(), name='register_application_interface'),
    url(r'^new/application/deployment', views.RegisterApplicationDeployments.as_view(), name='register_application_deployments'),
]

urlpatterns = format_suffix_patterns(urlpatterns)