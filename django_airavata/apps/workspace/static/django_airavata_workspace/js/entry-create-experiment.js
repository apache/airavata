import { components, entry } from "django-airavata-common-ui";
import CreateExperimentContainer from "./containers/CreateExperimentContainer.vue";

entry(Vue => {
  new Vue({
    render(h) {
      return h("div", [
        h(components.NotificationsDisplay),
        h(CreateExperimentContainer, {
          props: {
            appModuleId: this.appModuleId
          }
        })
      ]);
    },
    data() {
      return {
        appModuleId: null
      };
    },
    beforeMount() {
      if (this.$el.dataset.appModuleId) {
        this.appModuleId = this.$el.dataset.appModuleId;
      }
    }
  }).$mount("#create-experiment");
});
