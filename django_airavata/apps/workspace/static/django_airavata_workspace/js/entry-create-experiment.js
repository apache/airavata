import Vue from "vue";
import BootstrapVue from "bootstrap-vue";
import CreateExperimentContainer from "./containers/CreateExperimentContainer.vue";
import { errors } from "django-airavata-common-ui";

errors.GlobalErrorHandler.init();

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import "bootstrap-vue/dist/bootstrap-vue.css";

Vue.use(BootstrapVue);

new Vue({
  render(h) {
    return h(CreateExperimentContainer, {
      props: {
        appModuleId: this.appModuleId
      }
    });
  },
  data() {
    return {
      appModuleId: null
    };
  },
  beforeMount() {
    if (this.$el.dataset.appModuleId) {
      this.appModuleId = this.$el.dataset.appModuleId;
    }
  }
}).$mount("#create-experiment");
