import AdminDashboardHome from './components/dashboards/AdminDashboardHome.vue'
import NewApplication from './components/admin/NewApplication.vue'
import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue'
import ApplicationDetails from './components/admin/ApplicationDetails.vue'
import ApplicationInterface from './components/admin/ApplicationInterface.vue'
import ApplicationDeployments from './components/admin/ApplicationDeployments.vue'
import ComputeResourceDashboard from './components/dashboards/ComputeResourceDashboard'
import ComputeResourceDashboardHome from './components/dashboards/ComputeResourceDashboardHome'
import ComputeResourceDetails from './components/admin/compute_resource/ComputeResourceDetails'
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
  {path: '/admin', component: AdminDashboardHome},
  {path: '/experiments', component: ExperimentsDashboard},
];
export default new VueRouter({
  routes: routes
});
