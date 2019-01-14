import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import ParsersManageContainer from "./containers/ParsersManageContainer.vue";

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: "#parsers-manage",
  template: '<parsers-manage-container></parsers-manage-container>',
  data: {
      groupsOwnersData: null,
  },
  components: {
      ParsersManageContainer
  },
  beforeMount: function () {
  }
})
