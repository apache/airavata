import { entry } from "django-airavata-common-ui";
import RecentExperimentsContainer from "./containers/RecentExperimentsContainer.vue";

entry(Vue => {
  new Vue({
    render(h) {
      return h(RecentExperimentsContainer, {
        props: {
          viewAllExperiments: this.viewAllExperiments,
          username: this.username
        }
      });
    },
    data() {
      return {
        viewAllExperiments: null,
        username: null
      };
    },
    beforeMount() {
      this.viewAllExperiments = this.$el.dataset.viewAllExperiments;
      this.username = this.$el.dataset.username;
    }
  }).$mount("#recent-experiments");
});
