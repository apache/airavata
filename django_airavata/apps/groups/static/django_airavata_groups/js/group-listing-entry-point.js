import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import ProjectListContainer from './groups_components/GroupsManageContainer.vue'
// This is imported globally on the website so no need to include it again in this view
// import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue);

new Vue({
  template: '<groups-manage></groups-manage>',
  data: {
      groupsOwnersData: null
      groupsMembersData: null
  },
  // components: {
  //     ProjectListContainer
  // },
  beforeMount: function () {
      if (this.$refs['groups-owners-manage'].dataset.groupsOwnersData) {
          this.groupsOwnersData = JSON.parse(this.$el.dataset.groupsOwnersData);
      }
      if (this.$refs['groups-members-manage'].dataset.groupsMembersData) {
          this.groupsMembersData = JSON.parse(this.$el.dataset.groupsMembersData);
      }
  }
})
