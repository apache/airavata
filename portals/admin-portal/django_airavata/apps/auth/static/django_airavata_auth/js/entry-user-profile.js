import { components, entry } from "django-airavata-common-ui";
import UserProfileContainer from "./containers/UserProfileContainer.vue";
import createStore from "./store";

entry((Vue) => {
  const store = createStore(Vue);
  new Vue({
    store,
    render: (h) => h(components.MainLayout, [h(UserProfileContainer)]),
  }).$mount("#user-profile");
});
