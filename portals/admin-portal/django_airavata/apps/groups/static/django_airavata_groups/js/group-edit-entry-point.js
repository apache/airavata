import { components, entry } from "django-airavata-common-ui";
import GroupEditContainer from "./containers/GroupEditContainer.vue";

entry((Vue) => {
  new Vue({
    render(h) {
      return h(components.MainLayout, [
        h(GroupEditContainer, {
          props: {
            groupId: this.groupId,
            next: this.next,
          },
        }),
      ]);
    },
    data() {
      return {
        groupId: null,
        next: "/groups/",
      };
    },
    beforeMount() {
      if (this.$el.dataset.groupId) {
        this.groupId = this.$el.dataset.groupId;
      }
      if (this.$el.dataset.next) {
        this.next = this.$el.dataset.next;
      }
    },
  }).$mount("#group-edit");
});
