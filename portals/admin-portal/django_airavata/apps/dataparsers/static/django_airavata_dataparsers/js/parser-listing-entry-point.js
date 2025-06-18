import { components, entry } from "django-airavata-common-ui";
import ParsersManageContainer from "./containers/ParsersManageContainer.vue";

entry((Vue) => {
  new Vue({
    render: (h) => h(components.MainLayout, [h(ParsersManageContainer)]),
  }).$mount("#parsers-manage");
});
