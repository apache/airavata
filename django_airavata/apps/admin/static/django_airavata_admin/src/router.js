import AdminDashboard from './components/dashboards/AdminDashboard.vue'
import NewApplication from './components/admin/NewApplication.vue'
import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue'
import ApplicationDetails from './components/admin/ApplicationDetails.vue'
import ApplicationInterface from './components/admin/ApplicationInterface.vue'
import ApplicationDeployments from './components/admin/ApplicationDeployments.vue'
import GroupComputeResourcePreference from './components/admin/group_resource_preferences/GroupComputeResourcePreference'
import ComputePreferences from './components/admin/group_resource_preferences/ComputePreferences'
import ComputeResourcePreferenceDashboard from './components/dashboards/ComputeResourcePreferenceDashboard'
import CredentialStoreDashboard from './components/dashboards/CredentialStoreDashboard'
import ArrayComponentView from './components/commons/ArrayComponentView'
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
  {path: '/admin', component: AdminDashboard, name: "admin_dashboard"},
  {path: '/experiments', component: ExperimentsDashboard, name: 'experiments_dashboard'},
  {
    path: '/group/resource/preferences', component: GroupComputeResourcePreference, name: 'group_resource_preference',
    props: true
  },
  {
    path: '/group/resource/compute/preferences', component: ComputePreferences, name: 'compute_preferences',
    props: true
  },
  {
    path: '/dashboards/group/resource/preferences',
    component: ComputeResourcePreferenceDashboard,
    name: 'group_resource_preference_dashboard',
  },
  {
    path: '/credential/store',
    component: CredentialStoreDashboard,
    name: 'credential_store'
  },
  {
    path: "/test",
    component: ArrayComponentView,
    name: 'test'
  }
];
export default new VueRouter({
  routes: routes
});
