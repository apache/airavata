import Vue from 'vue'
import ProjectListContainer from './views/ProjectListContainer.vue'

new Vue({
  el: '#project-list',
  template: '<project-list-container v-bind:initialProjects="projects"></project-list-container>',
  data: {
      projects: null
  },
  components: {
      ProjectListContainer
  },
  beforeMount: function () {
      if (this.$el.dataset.projects) {
          this.projects = JSON.parse(this.$el.dataset.projects);
      }
  }
})
