import ApplicationsDashboard from './components/dashboards/ApplicationsDashboard.vue'
import NewApplication from './components/admin/NewApplication.vue'
import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue'
import ApplicationDetails from './components/admin/ApplicationDetails.vue'
import ApplicationInterface from './components/admin/ApplicationInterface.vue'
import ApplicationDeployments from './components/admin/ApplicationDeployments.vue'
import GroupComputeResourcePreference from './components/admin/group_resource_preferences/GroupComputeResourcePreference'
import ComputePreference from './components/admin/group_resource_preferences/ComputePreference'
import ComputeResourcePreferenceDashboard from './components/dashboards/ComputeResourcePreferenceDashboard'
import CredentialStoreDashboard from './components/dashboards/CredentialStoreDashboard'
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
  {path: '/applications', component: ApplicationsDashboard},
  {path: '/experiments', component: ExperimentsDashboard, name: 'experiments_dashboard'},
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
