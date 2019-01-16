import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import ParsersManageContainer from "./containers/ParsersManageContainer.vue";

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  render: h => h(ParsersManageContainer)
}).$mount("#parsers-manage");
