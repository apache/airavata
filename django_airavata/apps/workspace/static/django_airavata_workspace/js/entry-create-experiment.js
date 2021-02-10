import { components, entry } from "django-airavata-common-ui";
import CreateExperimentContainer from "./containers/CreateExperimentContainer.vue";
import "../../scss/styles.scss";

entry((Vue) => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(CreateExperimentContainer, {
          props: {
            appModuleId: this.appModuleId,
            userInputValues: this.userInputValues,
            experimentDataDir: this.experimentDataDir,
          },
        }),
      ]);
    },
    data() {
      return {
        appModuleId: null,
        userInputValues: null,
      };
    },
    beforeMount() {
      if (this.$el.dataset.appModuleId) {
        this.appModuleId = this.$el.dataset.appModuleId;
      }
      if (this.$el.dataset.userInputValues) {
        this.userInputValues = JSON.parse(this.$el.dataset.userInputValues);
      }
      if (this.$el.dataset.experimentDataDir) {
        this.experimentDataDir = this.$el.dataset.experimentDataDir;
      }
    },
  }).$mount("#create-experiment");
});
