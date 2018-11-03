import Vue from "vue";
import VueResource from "vue-resource";
import VueRouter from "vue-router";

import App from "./App.vue";
import BootstrapVue from "bootstrap-vue";
// TODO: load the latest bootstrap css globally
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";
import { library as faLibrary } from "@fortawesome/fontawesome-svg-core";
import {
  faGripVertical,
  faEquals,
  faPlus
} from "@fortawesome/free-solid-svg-icons";
import { faClipboard } from "@fortawesome/free-regular-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";

faLibrary.add(faGripVertical, faEquals, faClipboard, faPlus);

Vue.component("font-awesome-icon", FontAwesomeIcon);

import { errors } from "django-airavata-common-ui";

import router from "./router";
import store from "./store/store";

errors.GlobalErrorHandler.init();

Vue.config.productionTip = false;

Vue.use(BootstrapVue);
Vue.use(VueResource);
Vue.use(VueRouter);

new Vue({
  render: h => h(App),
  router,
  store
}).$mount("#app");
