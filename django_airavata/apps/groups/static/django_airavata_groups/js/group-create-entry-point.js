import { components, entry } from "django-airavata-common-ui";
import GroupCreateContainer from "./containers/GroupCreateContainer.vue";

entry((Vue) => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(GroupCreateContainer, {
          props: {
            next: this.next,
          },
        }),
      ]);
    },
    data() {
      return {
        next: "/groups/",
      };
    },
    beforeMount() {
      if (this.$el.dataset.next) {
        this.next = this.$el.dataset.next;
      }
    },
  }).$mount("#group-create");
});
