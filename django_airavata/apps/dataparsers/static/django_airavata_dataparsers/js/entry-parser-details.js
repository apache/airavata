import Vue from "vue";
import BootstrapVue from "bootstrap-vue";
import ParserDetailsContainer from "./containers/ParserDetailsContainer.vue";

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import "bootstrap-vue/dist/bootstrap-vue.css";

Vue.use(BootstrapVue);

new Vue({
  render(h) {
    return h(ParserDetailsContainer, {
      props: {
        parserId: this.parserId
      }
    });
  },
  data() {
    return {
      parserId: null,
      launching: false
    };
  },
  beforeMount() {
    if (this.$el.dataset.parserId) {
      this.parserId = this.$el.dataset.parserId;
    }
  }
}).$mount("#parser-details");
