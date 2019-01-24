import { entry } from "django-airavata-common-ui";
import DashboardContainer from "./containers/DashboardContainer.vue";

entry(Vue => {
  new Vue({
    render: h => h(DashboardContainer)
  }).$mount("#dashboard");
});
