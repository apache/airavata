import ApplicationDeploymentEditor from './components/applications/ApplicationDeploymentEditor.vue'
import ApplicationDeployments from './components/admin/ApplicationDeployments.vue'
import ApplicationDeploymentsList from './components/applications/ApplicationDeploymentsList.vue'
import ApplicationDetails from './components/admin/ApplicationDetails.vue'
import ApplicationEditorContainer from './components/applications/ApplicationEditorContainer.vue'
import ApplicationInterface from './components/admin/ApplicationInterface.vue'
import ApplicationInterfaceEditor from './components/applications/ApplicationInterfaceEditor.vue'
import ApplicationModuleEditor from './components/applications/ApplicationModuleEditor.vue'
import ApplicationsDashboard from './components/dashboards/ApplicationsDashboard.vue'
import ComputePreference from './components/admin/group_resource_preferences/ComputePreference'
import ComputeResourcePreferenceDashboard from './components/dashboards/ComputeResourcePreferenceDashboard'
import CredentialStoreDashboard from './components/dashboards/CredentialStoreDashboard'
import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue'
import GroupComputeResourcePreference from './components/admin/group_resource_preferences/GroupComputeResourcePreference'
import NewApplication from './components/admin/NewApplication.vue'
import VueRouter from 'vue-router'


const routes = [
  {
    path: '/new/application', component: NewApplication, name: 'newapp',
    children: [
      {
        path: 'details',
        component: ApplicationDetails,
        name: 'details',

      }, {
        path: 'interface',
        component: ApplicationInterface,
        name: 'interface'
      }, {
        path: 'deployments',
        component: ApplicationDeployments,
        name: 'deployments'
      }
    ]
  },
  {
    path: '/applications/new', component: ApplicationEditorContainer, name: 'new_application',
    children: [
      // TODO: Maybe only have the module route for a new application, save it
      // and then replace the URL with the module id
      {
        path: '', components: {
          module: ApplicationModuleEditor
        },
        name: 'new_application_module'
      },
    ]
  },
  {
    path: '/applications/:id', component: ApplicationEditorContainer, name: 'application',
    props: true,
    children: [
      {
        path: '', components: {
          module: ApplicationModuleEditor
        },
        name: 'application_module'
      },
      {
        path: 'interface', components: {
          interface: ApplicationInterfaceEditor
        },
        name: 'application_interface'
      },
      {
        path: 'deployments', components: {
          deployments: ApplicationDeploymentsList
        },
        name: 'application_deployments',
        props: {
          deployments: true
        }
      },
      {
        path: 'deployments/new/:hostId', components: {
          deployment: ApplicationDeploymentEditor
        },
        name: 'new_application_deployment',
      },
      {
        path: 'deployments/:deployment_id', components: {
          deployment: ApplicationDeploymentEditor
        },
        name: 'application_deployment',
        props: {
          deployment: true
        }
      },
    ]
  },
  { path: '/applications', component: ApplicationsDashboard },
  { path: '/experiments', component: ExperimentsDashboard, name: 'experiments_dashboard' },
  {
    path: '/group-resource-profiles/new', component: GroupComputeResourcePreference, name: 'new_group_resource_preference',
    props: true
  },
  {
    path: '/group-resource-profiles/:id', component: GroupComputeResourcePreference, name: 'group_resource_preference',
    props: true
  },
  {
    path: '/group-resource-profiles/new/compute-preferences/:host_id', component: ComputePreference, name: 'compute_preference_for_new_group_resource_profile',
    props: true
  },
  {
    path: '/group-resource-profiles/:id/compute-preferences/:host_id', component: ComputePreference, name: 'compute_preference',
    props: true
  },
  {
    path: '/group-resource-profiles',
    component: ComputeResourcePreferenceDashboard,
    name: 'group_resource_preference_dashboard',
  },
  {
    path: '/credential/store',
    component: CredentialStoreDashboard,
    name: 'credential_store'
  },
];
export default new VueRouter({
  mode: 'history',
  base: '/admin/',
  routes: routes,
});
