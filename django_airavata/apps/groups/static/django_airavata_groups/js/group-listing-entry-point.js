import { entry } from "django-airavata-common-ui";
import GroupsManageContainer from "./containers/GroupsManageContainer.vue";

entry(Vue => {
  new Vue({
    render: h => h(GroupsManageContainer)
  }).$mount("#group-list");
});
