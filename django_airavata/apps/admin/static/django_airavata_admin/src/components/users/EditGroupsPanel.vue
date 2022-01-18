<template>
  <b-card header="Edit Groups">
    <user-group-membership-editor
      v-model="data"
      :editable-groups="editableGroups"
      :airavata-internal-user-id="airavataInternalUserId"
    />
    <b-button
      @click="$emit('save', data)"
      variant="primary"
      :disabled="!areGroupsUpdated"
      >Save</b-button
    >
  </b-card>
</template>

<script>
import VModelMixin from "django-airavata-common-ui/js/mixins/VModelMixin";
import UserGroupMembershipEditor from "./UserGroupMembershipEditor.vue";

export default {
  components: { UserGroupMembershipEditor },
  props: {
    value: {
      type: Array,
      required: true,
    },
    editableGroups: {
      type: Array,
      required: true,
    },
    airavataInternalUserId: {
      type: String,
      required: true,
    },
  },
  mixins: [VModelMixin],
  computed: {
    currentGroupIds() {
      const groupIds = this.value.map((g) => g.id);
      groupIds.sort();
      return groupIds;
    },
    updatedGroupIds() {
      const groupIds = this.data.map((g) => g.id);
      groupIds.sort();
      return groupIds;
    },
    areGroupsUpdated() {
      for (const groupId of this.currentGroupIds) {
        // Check if a group was removed
        if (this.updatedGroupIds.indexOf(groupId) < 0) {
          return true;
        }
      }
      for (const groupId of this.updatedGroupIds) {
        // Check if a group was added
        if (this.currentGroupIds.indexOf(groupId) < 0) {
          return true;
        }
      }
      return false;
    },
  },
};
</script>

<style></style>
