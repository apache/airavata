import Vue from 'vue';
import VueResource from 'vue-resource';
import VueRouter from 'vue-router';

import ExperimentsDashboard from './components/dashboards/ExperimentDashboard.vue';
import AdminDashboard from './components/dashboards/AdminDashboard.vue';
import CredentialStore from './components/dashboards/CredentialStoreDashboard.vue'
import Loading from './components/Loading.vue'
import ComputeResourceDashboard from './components/dashboards/ComputeResourceDashboard'
import ComputeResourcePreferenceDashboard from './components/dashboards/ComputeResourcePreferenceDashboard'
import BootstrapVue from 'bootstrap-vue'
import 'bootstrap-vue/dist/bootstrap-vue.css'

import { components, errors } from 'django-airavata-common-ui'

import router from './router';
import store from './store/store';

errors.GlobalErrorHandler.init();

Vue.config.productionTip = false;

Vue.use(BootstrapVue)
Vue.use(VueResource);
Vue.use(VueRouter);


export function initializeApacheAiravataDashboard(dashboardName) {
  var template = `
    <div class="vmain"><notifications-display/><Loading/> 
        <transition name="fade">
            <router-view>
            </router-view>
        </transition>
    </div>`
  var vueApp = new Vue({
    el: '#app',
    router,
    store,
    template: template,
    components: {
      ExperimentsDashboard,
      AdminDashboard,
      CredentialStore,
      Loading,
      ComputeResourceDashboard,
      ComputeResourcePreferenceDashboard,
      'notifications-display': components.NotificationsDisplay,
    },
    mounted:function () {
      if (this.$router.currentRoute.path === '/') {
        this.$router.push({ name: dashboardName })
      }
    }

  })
  Vue.config.devtools = true
  Vue.config.debug = true
  Vue.config.silent = false
  return vueApp
};


