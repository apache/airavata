import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: '#groups-manage',
  // template: '<project-list-container v-bind:initialProjectsData="projectsData"></project-list-container>',
  data: {
      groupsData: null
  },
  // components: {
  //     ProjectListContainer
  // },
  beforeMount: function () {
      if (this.$el.dataset.groupsData) {
          this.groupsData = JSON.parse(this.$el.dataset.groupsData);
      }
  }
})
