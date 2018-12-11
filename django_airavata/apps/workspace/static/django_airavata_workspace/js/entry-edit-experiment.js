import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import EditExperimentContainer from './containers/EditExperimentContainer.vue'

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

// Expect a template with id "edit-experiment" and experiment-id data attribute
//
//   <div id="edit-experiment" data-experiment-id="..expid.."/>

new Vue({
  render(h) {
    return h(EditExperimentContainer, {
      props: {
        experimentId: this.experimentId
      }
    })
  },
  data() {
    return {
      experimentId: null
    }
  },
  beforeMount() {
    this.experimentId = this.$el.dataset.experimentId;
  }
}).$mount("#edit-experiment");
