import Vue from "vue";
import BootstrapVue from "bootstrap-vue";
import ProjectListContainer from "./containers/ProjectListContainer.vue";

// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import "bootstrap-vue/dist/bootstrap-vue.css";

Vue.use(BootstrapVue);

new Vue({
  render(h) {
    return h(ProjectListContainer, {
      props: {
        initialProjectsData: this.projectsData
      }
    });
  },
  data() {
    return {
      projectsData: null
    };
  },
  beforeMount() {
    if (this.$el.dataset.projectsData) {
      this.projectsData = JSON.parse(this.$el.dataset.projectsData);
    }
  }
}).$mount("#project-list");
