import Vue from "vue";
import BootstrapVue from "bootstrap-vue";
import ViewExperimentContainer from "./containers/ViewExperimentContainer.vue";

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import "bootstrap-vue/dist/bootstrap-vue.css";

Vue.use(BootstrapVue);

new Vue({
  render(h) {
    return h(ViewExperimentContainer, {
      props: {
        initialFullExperimentData: this.fullExperimentData,
        launching: this.launching
      }
    });
  },
  data() {
    return {
      fullExperimentData: null,
      launching: false
    };
  },
  beforeMount() {
    this.fullExperimentData = JSON.parse(this.$el.dataset.fullExperimentData);
    if ("launching" in this.$el.dataset) {
      this.launching = JSON.parse(this.$el.dataset.launching);
    }
  }
}).$mount("#view-experiment");
