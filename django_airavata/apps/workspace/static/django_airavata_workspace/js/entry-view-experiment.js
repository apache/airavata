import { components, errors } from "django-airavata-common-ui";
import { mapActions } from "vuex";
import ExperimentSummary from "./components/experiment/ExperimentSummary.vue";
import store from "./store";

import Vue from "vue";
import BootstrapVue from "bootstrap-vue";
import AsyncComputed from "vue-async-computed";

errors.GlobalErrorHandler.init();

// This is imported globally on the website (see main.js) so no need to include
// it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import "bootstrap-vue/dist/bootstrap-vue.css";

// Common Vue configuration
Vue.use(BootstrapVue);
Vue.use(AsyncComputed);

// TODO: fix entry so that we can still get common configuration when using Vuex
// entry((Vue) => {
new Vue({
  store,
  render(h) {
    return h(components.MainLayout, [h(ExperimentSummary)]);
  },
  async beforeMount() {
    const fullExperimentData = JSON.parse(this.$el.dataset.fullExperimentData);
    this.setInitialFullExperimentData({ fullExperimentData });
    if ("launching" in this.$el.dataset) {
      const launching = JSON.parse(this.$el.dataset.launching);
      this.setLaunching({ launching });
    }
  },
  methods: {
    ...mapActions("viewExperiment", [
      "setInitialFullExperimentData",
      "setLaunching",
    ]),
  },
}).$mount("#view-experiment");

// });
