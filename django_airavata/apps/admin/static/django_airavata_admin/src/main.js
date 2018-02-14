
import Vue from 'vue';
import VueResource from 'vue-resource';
import VueRouter from 'vue-router';

import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue';
import AdminDashboard from './components/dashboards/AdminDashboard.vue';
import CredentialStore from './components/dashboards/CredentialStore.vue'
import Loading from './components/Loading.vue'
import ComputeResourceDashboardHome from './components/dashboards/ComputeResourceDashboardHome'
import ComputeResourceDashboard from './components/dashboards/ComputeResourceDashboard'

import router from './router';
import store from './store/store';

Vue.config.productionTip = false;

Vue.use(VueResource);
Vue.use(VueRouter);


export function initializeApacheAiravataDashboard(dashboardName) {
  var template=`<div class="vmain"><Loading/><${dashboardName}/></div>`
  var vueApp= new Vue({
    el: '#app',
    router,
    store,
    template:template ,
    components: {ExperimentsDashboard, AdminDashboard,CredentialStore,Loading,ComputeResourceDashboardHome,ComputeResourceDashboard}

  })
  Vue.config.devtools = true
  Vue.config.debug = true
  Vue.config.silent = false
  return vueApp
};


