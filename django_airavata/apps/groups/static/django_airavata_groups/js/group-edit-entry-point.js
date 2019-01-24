import { entry } from "django-airavata-common-ui";
import GroupEditContainer from "./containers/GroupEditContainer.vue";

entry(Vue => {
new Vue({
  render(h) {
    return h(GroupEditContainer, {
      props: {
        groupId: this.groupId
      }
    });
  },
  data() {
    return {
      groupId: null
    };
  },
  beforeMount() {
    if (this.$el.dataset.groupId) {
      this.groupId = this.$el.dataset.groupId;
    }
  }
}).$mount("#group-edit");
});
