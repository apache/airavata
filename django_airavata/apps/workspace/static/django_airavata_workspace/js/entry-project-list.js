import Vue from 'vue'
import ProjectListContainer from './views/ProjectListContainer.vue'

new Vue({
  el: '#project-list',
  template: '<project-list-container v-bind:initialProjectsData="projectsData"></project-list-container>',
  data: {
      projects: null
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
