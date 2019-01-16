import Vue from "vue";
import VueResource from "vue-resource";
import VueRouter from "vue-router";

import App from "./App.vue";
import BootstrapVue from "bootstrap-vue";
// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import "bootstrap-vue/dist/bootstrap-vue.css";

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
