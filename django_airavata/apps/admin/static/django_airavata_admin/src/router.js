import AdminDashboardHome from './components/dashboards/AdminDashboardHome.vue'
import NewApplication from './components/admin/NewApplication.vue'
import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue'
import ApplicationDetails from'./components/admin/ApplicationDetails.vue'
import ApplicationInterface from './components/admin/ApplicationInterface.vue'
import ApplicationDeployments from  './components/admin/ApplicationDeployments.vue'
import VueRouter from 'vue-router'


const routes=[
  { path: '/new/application', component: NewApplication,name:'newapp',
  children:[
    {
      path:'details',
      component:ApplicationDetails,
      name:'details',

    },{
      path:'interface',
      component:ApplicationInterface,
      name:'interface'
    },{
      path:'deployments',
      component:ApplicationDeployments,
      name:'deployments'

    }
  ]},
  { path: '/', component: AdminDashboardHome },
  {path:'/experiments',component:ExperimentsDashboard}
];
export default new VueRouter({
  routes:routes
});
