import { components, entry } from "django-airavata-common-ui";
import ExperimentListContainer from "./containers/ExperimentListContainer.vue";
import VueFlatPickr from "vue-flatpickr-component";
import "flatpickr/dist/flatpickr.css";

entry((Vue) => {
  Vue.use(VueFlatPickr);
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(ExperimentListContainer, {
          props: {
            initialExperimentsData: this.experimentsData,
          },
        }),
      ]);
    },
    data() {
      return {
        experimentsData: null,
      };
    },
    beforeMount() {
      if (this.$el.dataset.experimentsData) {
        this.experimentsData = JSON.parse(this.$el.dataset.experimentsData);
      }
    },
  }).$mount("#experiment-list");
});
