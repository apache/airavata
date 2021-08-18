import { components, entry } from "django-airavata-common-ui";
import GroupCreateContainer from "./containers/GroupCreateContainer.vue";
import VueDraggable from 'vue-draggable';

entry((Vue) => {
  Vue.use(VueDraggable);
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
