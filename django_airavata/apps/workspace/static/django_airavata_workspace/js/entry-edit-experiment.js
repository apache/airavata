import { components, entry } from "django-airavata-common-ui";
import EditExperimentContainer from "./containers/EditExperimentContainer.vue";

// Expect a template with id "edit-experiment" and experiment-id data attribute
//
//   <div id="edit-experiment" data-experiment-id="..expid.."/>

entry(Vue => {
  new Vue({
    render(h) {
      return h("div", [
        h(components.NotificationsDisplay),
        h(EditExperimentContainer, {
          props: {
            experimentId: this.experimentId
          }
        })
      ]);
    },
    data() {
      return {
        experimentId: null
      };
    },
    beforeMount() {
      this.experimentId = this.$el.dataset.experimentId;
    }
  }).$mount("#edit-experiment");
});
