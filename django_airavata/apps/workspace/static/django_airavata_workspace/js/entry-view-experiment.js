import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import ViewExperimentContainer from './containers/ViewExperimentContainer.vue'

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: '#view-experiment',
  template: '<view-experiment-container :initial-experiment-data="experiment"></view-experiment-container>',
  data () {
      return {
          experiment: null,
      }
  },
  components: {
      ViewExperimentContainer,
  },
  beforeMount: function () {
      console.log(this.$el.dataset.experimentData);
      this.experiment = JSON.parse(this.$el.dataset.experimentData);
  }
})
