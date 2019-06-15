<template>
  <user-group-membership-editor
    v-model="localIAMUserProfile.groups"
    :editable-groups="editableGroups"
    :airavata-internal-user-id="iamUserProfile.airavataInternalUserId"
    @input="groupsUpdated"
  />
</template>
<script>
import { models } from "django-airavata-api";
import UserGroupMembershipEditor from "./UserGroupMembershipEditor";

export default {
  name: "user-details-container",
  props: {
    iamUserProfile: {
      type: models.IAMUserProfile,
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
      localIAMUserProfile: this.iamUserProfile.clone()
    };
  },
  watch: {
    iamUserProfile(newValue) {
      this.localIAMUserProfile = newValue.clone();
    }
  },
  methods: {
    groupsUpdated() {
      this.$emit("groups-updated", this.localIAMUserProfile);
    }
  }
};
</script>

