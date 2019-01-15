import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import ParserEditContainer from './containers/ParserEditContainer.vue'
// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: "#edit-parser",
  template: '<parser-edit-container :parserId="parserId"></parser-edit-container>',
  data: {
      parserId: null,
  },
  components: {
      ParserEditContainer,
  },
  beforeMount: function() {
      if (this.$el.dataset.parserId) {
          this.parserId = this.$el.dataset.parserId;
          console.log("parserId", this.parserId);
      }
  }
})
