import { components, entry } from "django-airavata-common-ui";
import CreateExperimentContainer from "./containers/CreateExperimentContainer.vue";

entry(Vue => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(CreateExperimentContainer, {
          props: {
            appModuleId: this.appModuleId,
            userInputFiles: this.userInputFiles
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
    }
  }).$mount("#create-experiment");
});
