import { entry } from "django-airavata-common-ui";
import GroupCreateContainer from "./containers/GroupCreateContainer.vue";

entry(Vue => {
  new Vue({
    render: h => h(GroupCreateContainer)
  }).$mount("#group-create");
});
