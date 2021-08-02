import { components, entry } from "django-airavata-common-ui";
import UserProfileContainer from "./containers/UserProfileContainer.vue";

entry(Vue => {
    new Vue({
        render: h => h(components.MainLayout, [h(UserProfileContainer)])
    }).$mount("#user-profile");
});
