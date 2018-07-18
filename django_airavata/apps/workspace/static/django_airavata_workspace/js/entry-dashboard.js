import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import DashboardContainer from './containers/DashboardContainer.vue'

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: '#dashboard',
  template: '<dashboard-container></dashboard-container>',
  data: {
  },
  components: {
      DashboardContainer
  }
})
