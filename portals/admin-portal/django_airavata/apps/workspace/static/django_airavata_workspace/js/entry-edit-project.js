import { components, entry } from "django-airavata-common-ui";
import EditProjectContainer from "./containers/EditProjectContainer.vue";

// Expect a template with id "edit-project" and project-id data attribute
//
//   <div id="edit-project" data-project-id="..projectID.."/>

entry((Vue) => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(EditProjectContainer, {
          props: {
            projectId: this.projectId,
          },
        }),
      ]);
    },
    data() {
      return {
        projectId: null,
      };
    },
    beforeMount() {
      this.projectId = this.$el.dataset.projectId;
    },
  }).$mount("#edit-project");
});
