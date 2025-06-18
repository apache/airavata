import { components, entry } from "django-airavata-common-ui";
import DashboardContainer from "./containers/DashboardContainer.vue";
import RecentExperimentsContainer from "./containers/RecentExperimentsContainer.vue";

entry((Vue) => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(DashboardContainer),
        h(RecentExperimentsContainer, {
          props: {
            viewAllExperiments: this.viewAllExperiments,
            username: this.username,
          },
          slot: "sidebar",
        }),
      ]);
    },
    data() {
      return {
        viewAllExperiments: null,
        username: null,
      };
    },
    beforeMount() {
      this.viewAllExperiments = this.$el.dataset.viewAllExperiments;
      this.username = this.$el.dataset.username;
    },
  }).$mount("#dashboard");
});
