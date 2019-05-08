import { components, entry } from "django-airavata-common-ui";
import CreateExperimentContainer from "./containers/CreateExperimentContainer.vue";

entry(Vue => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(CreateExperimentContainer, {
          props: {
            appModuleId: this.appModuleId,
            userInputFiles: this.userInputFiles,
            experimentDataDir: this.experimentDataDir
          }
        })
      ]);
    },
    data() {
      return {
        appModuleId: null,
        userInputFiles: null
      };
    },
    beforeMount() {
      if (this.$el.dataset.appModuleId) {
        this.appModuleId = this.$el.dataset.appModuleId;
      }
      if (this.$el.dataset.userInputFiles) {
        this.userInputFiles = JSON.parse(this.$el.dataset.userInputFiles);
      }
      if (this.$el.dataset.experimentDataDir) {
        this.experimentDataDir = this.$el.dataset.experimentDataDir;
      }
    }
  }).$mount("#create-experiment");
});
