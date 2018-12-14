import Vue from "vue";
import BootstrapVue from "bootstrap-vue";
import GroupEditContainer from "./containers/GroupEditContainer.vue";
// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import "bootstrap-vue/dist/bootstrap-vue.css";

Vue.use(BootstrapVue);

new Vue({
  render(h) {
    return h(GroupEditContainer, {
      props: {
        groupId: this.groupId
      }
    });
  },
  data() {
    return {
      groupId: null
    };
  },
  beforeMount() {
    if (this.$el.dataset.groupId) {
      this.groupId = this.$el.dataset.groupId;
    }
  }
}).$mount("#group-edit");
