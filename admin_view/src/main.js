import Vue from 'vue'
import VueResource from 'vue-resource'
import VueRouter from 'vue-router'

import ExperimentsDashboard from './dashboards/ExperimentDashboard.vue'
import AdminDashboard from './dashboards/AdminDashboard.vue'
import router from './router'
Vue.config.productionTip = false;

Vue.use(VueResource);
Vue.use(VueRouter);



export function initializeApacheAiravataDashboard(dashboardName) {
  return new Vue({
    el: '#app',
    router,
    template: '<' + dashboardName + '/>',
    components: {ExperimentsDashboard, AdminDashboard}

  })
};


