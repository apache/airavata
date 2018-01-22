import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import ViewExperimentContainer from './containers/ViewExperimentContainer.vue'

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: '#view-experiment',
  template: '<view-experiment-container :initial-full-experiment-data="fullExperimentData"></view-experiment-container>',
  data () {
      return {
          fullExperimentData: null,
      }
  },
  components: {
      ViewExperimentContainer,
  },
  beforeMount: function () {
      this.fullExperimentData = JSON.parse(this.$el.dataset.fullExperimentData);
  }
})
