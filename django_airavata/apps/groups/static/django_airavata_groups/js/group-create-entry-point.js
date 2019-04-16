import { components, entry } from "django-airavata-common-ui";
import GroupCreateContainer from "./containers/GroupCreateContainer.vue";

entry(Vue => {
  new Vue({
    render: h => h(components.MainLayout, [h(GroupCreateContainer)])
  }).$mount("#group-create");
});
