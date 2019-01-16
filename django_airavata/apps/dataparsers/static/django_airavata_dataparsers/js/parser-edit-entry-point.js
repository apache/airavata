import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import ParserEditContainer from './containers/ParserEditContainer.vue'
// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  render(h) {
    return h(ParserEditContainer, {
      props: {
        parserId: this.parserId
      }
    });
  },
  data() {
    return {
      parserId: null
    };
  },
  beforeMount() {
      if (this.$el.dataset.parserId) {
          this.parserId = this.$el.dataset.parserId;
          console.log("parserId", this.parserId);
      } else {
        console.error("Missing data-parser-id attribute");
      }
  }
}).$mount("#edit-parser");
