import Vue from 'vue';
import VueResource from 'vue-resource';
import VueRouter from 'vue-router';
import Vuex from 'vuex';

import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue';
import AdminDashboard from './components/dashboards/AdminDashboard.vue';

import router from './router';
import storeParams from './store';

Vue.config.productionTip = false;

Vue.use(VueResource);
Vue.use(VueRouter);
Vue.use(Vuex);

var store=new Vuex.Store(storeParams);

export function initializeApacheAiravataDashboard(dashboardName) {
  return new Vue({
    el: '#app',
    router,
    store,
    template: '<' + dashboardName + '/>',
    components: {ExperimentsDashboard, AdminDashboard}

  })
};


