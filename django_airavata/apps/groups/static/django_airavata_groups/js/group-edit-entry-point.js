import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import GroupEditContainer from './groups_components/GroupEditContainer.vue'
// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  el: "#edit-group",
  template: '<group-edit-container :groupId="groupId"></group-edit-container>',
  data: {
      groupId: null,
  },
  components: {
      GroupEditContainer,
  },
  beforeMount: function() {
      if (this.$el.dataset.groupId) {
          this.groupId = this.$el.dataset.groupId;
          console.log("groupId", this.groupId);
      }
  }
})
