import { components, entry } from "django-airavata-common-ui";
import ProjectListContainer from "./containers/ProjectListContainer.vue";

entry((Vue) => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(ProjectListContainer, {
          props: {
            initialProjectsData: this.projectsData,
          },
        }),
      ]);
    },
    data() {
      return {
        projectsData: null,
      };
    },
    beforeMount() {
      if (this.$el.dataset.projectsData) {
        this.projectsData = JSON.parse(this.$el.dataset.projectsData);
      }
    },
  }).$mount("#project-list");
});
