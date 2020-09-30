import { components, entry } from "django-airavata-common-ui";
import ParserDetailsContainer from "./containers/ParserDetailsContainer.vue";

entry((Vue) => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(ParserDetailsContainer, {
          props: {
            parserId: this.parserId,
          },
        }),
      ]);
    },
    data() {
      return {
        parserId: null,
        launching: false,
      };
    },
    beforeMount() {
      if (this.$el.dataset.parserId) {
        this.parserId = this.$el.dataset.parserId;
      }
    },
  }).$mount("#parser-details");
});
