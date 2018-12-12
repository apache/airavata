import Vue from "vue";
import BootstrapVue from "bootstrap-vue";
import ExperimentListContainer from "./containers/ExperimentListContainer.vue";

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import "bootstrap-vue/dist/bootstrap-vue.css";

Vue.use(BootstrapVue);

new Vue({
  render(h) {
    return h(ExperimentListContainer, {
      props: {
        initialExperimentsData: this.experimentsData
      }
    });
  },
  data() {
    return {
      experimentsData: null
    };
  },
  beforeMount() {
    if (this.$el.dataset.experimentsData) {
      this.experimentsData = JSON.parse(this.$el.dataset.experimentsData);
    }
  }
}).$mount("#experiment-list");
