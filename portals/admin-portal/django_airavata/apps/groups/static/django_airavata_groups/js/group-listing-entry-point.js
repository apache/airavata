import { components, entry } from "django-airavata-common-ui";
import GroupsManageContainer from "./containers/GroupsManageContainer.vue";

entry((Vue) => {
  new Vue({
    render: (h) => h(components.MainLayout, [h(GroupsManageContainer)]),
  }).$mount("#group-list");
});
