import { components, entry } from "django-airavata-common-ui";
import { mapActions } from "vuex";
import ExperimentSummary from "./components/experiment/ExperimentSummary.vue";
import createStore from "./store";

entry((Vue) => {
  const store = createStore(Vue);
  new Vue({
    store,
    render(h) {
      return h(components.MainLayout, [h(ExperimentSummary)]);
    },
    async beforeMount() {
      const fullExperimentData = JSON.parse(
        this.$el.dataset.fullExperimentData
      );
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
});
