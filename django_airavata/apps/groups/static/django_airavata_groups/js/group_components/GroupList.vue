<template>
  <div>
    <b-alert
      dismissible
      :variant="alertVariant"
      :show="showDismissibleAlert"
      @dismissed="showDismissibleAlert = false"
      >{{ alertMsg }}</b-alert
    >
    <table class="table table-hover">
      <thead>
        <tr>
          <th>Name</th>
          <th>Owner</th>
          <th>Description</th>
          <th id="group-list-actions-header">Actions</th>
        </tr>
      </thead>
      <tbody>
        <group-list-item
          @deleteSuccess="deleteSuccess"
          @deleteFailed="deleteFailed"
          v-bind:group="group"
          v-bind:type="owner"
          v-for="group in groupsForOwners"
          v-bind:key="group.groupID"
        >
        </group-list-item>
      </tbody>
    </table>
  </div>
</template>

<script>
import GroupListItem from "./GroupListItem.vue";

export default {
  name: "group-list",
  props: ["groupsForOwners"],
  data: function () {
    return {
      owner: "owner",
      alertMsg: null,
      alertVariant: "primary",
      showDismissibleAlert: false,
    };
  },
  components: {
    GroupListItem,
  },
  methods: {
    deleteSuccess() {
      window.location.reload(true);
    },
    deleteFailed(value) {
      this.alertMsg = value;
      this.alertVariant = "danger";
      this.showDismissibleAlert = true;
    },
  },
};
</script>

<style>
#group-list-actions-header {
  min-width: 150px;
}
</style>
