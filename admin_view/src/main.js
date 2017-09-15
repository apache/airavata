import Vue from 'vue';
import VueResource from 'vue-resource';
import VueRouter from 'vue-router';
import Vuex from 'vuex';

import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue'
import AdminDashboard from './components/dashboards/AdminDashboard.vue'
import router from './router'
Vue.config.productionTip = false;

Vue.use(VueResource);
Vue.use(VueRouter);
Vue.use(Vuex);


export function initializeApacheAiravataDashboard(dashboardName) {
  return new Vue({
    el: '#app',
    router,
    template: '<' + dashboardName + '/>',
    components: {ExperimentsDashboard, AdminDashboard}

  })
};


