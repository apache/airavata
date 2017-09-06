import AdminDashboardHome from './dashboards/AdminDashboardHome.vue'
import NewApplication from './components/admin/NewApplication.vue'
import VueRouter from 'vue-router'
const routes=[
  { path: '/admin/new/application', component: NewApplication,name:'newapp' },
  { path: '/', component: AdminDashboardHome }
];
export default new VueRouter({
  routes:routes
});
