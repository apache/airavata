import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import CreateExperimentContainer from './views/CreateExperimentContainer.vue'

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: '#create-experiment',
  template: '<create-experiment-container></create-experiment-container>',
  data: {
  },
  components: {
      CreateExperimentContainer,
  }
})
