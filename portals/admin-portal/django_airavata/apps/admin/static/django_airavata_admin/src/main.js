import { components, entry } from "django-airavata-common-ui";
import VueResource from "vue-resource";
import VueRouter from "vue-router";
import VueFlatPickr from "vue-flatpickr-component";
import App from "./App.vue";
import router from "./router";

import "flatpickr/dist/flatpickr.css";
import createStore from "./store";

entry((Vue) => {
  Vue.config.productionTip = false;

  Vue.use(VueResource);
  Vue.use(VueRouter);
  Vue.use(VueFlatPickr);

  const store = createStore(Vue);

  new Vue({
    store,
    render: (h) => h(components.MainLayout, [h(App)]),
    router,
  }).$mount("#app");
});
