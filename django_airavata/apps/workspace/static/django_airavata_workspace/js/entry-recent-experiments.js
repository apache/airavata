import { entry } from "django-airavata-common-ui";
import RecentExperimentsContainer from "./containers/RecentExperimentsContainer.vue";

entry(Vue => {
  new Vue({
    render(h) {
      return h(RecentExperimentsContainer, {
        props: {
          viewAllExperiments: this.viewAllExperiments
        }
      });
    },
    data() {
      return {
        viewAllExperiments: null
      };
    },
    beforeMount() {
      this.viewAllExperiments = this.$el.dataset.viewAllExperiments;
    }
  }).$mount("#recent-experiments");
});
