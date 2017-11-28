import Vue from 'vue';
import VueResource from 'vue-resource';
import VueRouter from 'vue-router';

import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue';
import AdminDashboard from './components/dashboards/AdminDashboard.vue';
import CredentialStore from './components/dashboards/CredentialStore.vue'

import router from './router';
import store from './store/store';

Vue.config.productionTip = false;

Vue.use(VueResource);
Vue.use(VueRouter);


export function initializeApacheAiravataDashboard(dashboardName) {
  var vueApp= new Vue({
    el: '#app',
    router,
    store,
    template: '<' + dashboardName + '/>',
    components: {ExperimentsDashboard, AdminDashboard,CredentialStore}

  })
  Vue.config.devtools = true
  Vue.config.debug = true
  Vue.config.silent = false
  return vueApp
};


