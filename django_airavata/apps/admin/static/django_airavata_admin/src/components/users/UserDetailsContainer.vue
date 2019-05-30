<template>
  <user-group-membership-editor
    v-model="localManagedUserProfile.groups"
    :editable-groups="editableGroups"
    :airavata-internal-user-id="managedUserProfile.airavataInternalUserId"
    @input="groupsUpdated"
  />
</template>
<script>
import { models } from "django-airavata-api";
import UserGroupMembershipEditor from "./UserGroupMembershipEditor";

export default {
  name: "user-details-container",
  props: {
    managedUserProfile: {
      type: models.ManagedUserProfile,
      required: true
    },
    editableGroups: {
      type: Array,
      required: true
    }
  },
  components: {
    UserGroupMembershipEditor
  },
  data() {
    return {
      localManagedUserProfile: this.managedUserProfile.clone()
    };
  },
  watch: {
    managedUserProfile(newValue) {
      this.localManagedUserProfile = newValue.clone();
    }
  },
  methods: {
    groupsUpdated() {
      this.$emit("groups-updated", this.localManagedUserProfile);
    }
  }
};
</script>

