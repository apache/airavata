import { components, entry } from "django-airavata-common-ui";
import DashboardContainer from "./containers/DashboardContainer.vue";

entry(Vue => {
  new Vue({
    render: h =>
      h("div", [h(components.NotificationsDisplay), h(DashboardContainer)])
  }).$mount("#dashboard");
});
