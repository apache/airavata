import { components, entry } from "django-airavata-common-ui";
import ParserEditContainer from "./containers/ParserEditContainer.vue";

entry((Vue) => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(ParserEditContainer, {
          props: {
            parserId: this.parserId,
          },
        }),
      ]);
    },
    data() {
      return {
        parserId: null,
      };
    },
    beforeMount() {
      if (this.$el.dataset.parserId) {
        this.parserId = this.$el.dataset.parserId;
      }
    },
  }).$mount("#edit-parser");
});
