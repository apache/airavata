import Vue from "vue";
import VueResource from "vue-resource";
import VueRouter from "vue-router";

import ExperimentsDashboard from "./components/dashboards/ExperimentDashboard.vue";
import CredentialStore from "./components/dashboards/CredentialStoreDashboard.vue";
import Loading from "./components/Loading.vue";
import ComputeResourceDashboard from "./components/dashboards/ComputeResourceDashboard";
import ComputeResourcePreferenceDashboard from "./components/dashboards/ComputeResourcePreferenceDashboard";
import BootstrapVue from "bootstrap-vue";
// TODO: load the latest bootstrap css globally
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";
import { library as faLibrary } from "@fortawesome/fontawesome-svg-core";
import { faGripVertical, faEquals } from "@fortawesome/free-solid-svg-icons";
import { faClipboard } from "@fortawesome/free-regular-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";

faLibrary.add(faGripVertical, faEquals, faClipboard);

Vue.component("font-awesome-icon", FontAwesomeIcon);

import { components, errors } from "django-airavata-common-ui";

import router from "./router";
import store from "./store/store";

errors.GlobalErrorHandler.init();

Vue.config.productionTip = false;

Vue.use(BootstrapVue);
Vue.use(VueResource);
Vue.use(VueRouter);

export function initializeApacheAiravataDashboard(dashboardName) {
  var template = `
    <div class="vmain"><notifications-display/><Loading/>
        <transition name="fade">
            <router-view>
            </router-view>
        </transition>
    </div>`;
  var vueApp = new Vue({
    el: "#app",
    router,
    store,
    template: template,
    components: {
      ExperimentsDashboard,
      // AdminDashboard,
      CredentialStore,
      Loading,
      ComputeResourceDashboard,
      ComputeResourcePreferenceDashboard,
      "notifications-display": components.NotificationsDisplay
    }
  });
  Vue.config.devtools = true;
  Vue.config.debug = true;
  Vue.config.silent = false;
  return vueApp;
}
