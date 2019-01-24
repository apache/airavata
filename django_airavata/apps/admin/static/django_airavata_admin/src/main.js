import { entry } from "django-airavata-common-ui";
import VueResource from "vue-resource";
import VueRouter from "vue-router";
import App from "./App.vue";
import router from "./router";


entry(Vue => {
  Vue.config.productionTip = false;

  Vue.use(VueResource);
  Vue.use(VueRouter);

  new Vue({
    render: h => h(App),
    router
  }).$mount("#app");
});
