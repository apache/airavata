import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import ProjectListContainer from './containers/ProjectListContainer.vue'

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: '#project-list',
  template: '<project-list-container v-bind:initialProjectsData="projectsData"></project-list-container>',
  data: {
      projectsData: null
  },
  components: {
      ProjectListContainer
  },
  beforeMount: function () {
      if (this.$el.dataset.projectsData) {
          this.projectsData = JSON.parse(this.$el.dataset.projectsData);
      }
  }
})
