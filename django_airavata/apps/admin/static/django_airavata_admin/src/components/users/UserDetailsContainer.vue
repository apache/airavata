<template>
  <div>
    <user-group-membership-editor
      v-if="iamUserProfile.airavataUserProfileExists"
      v-model="localIAMUserProfile.groups"
      :editable-groups="editableGroups"
      :airavata-internal-user-id="iamUserProfile.airavataInternalUserId"
      @input="groupsUpdated"
    />
    <enable-user-panel
      v-if="!iamUserProfile.enabled && !iamUserProfile.emailVerified"
      :username="iamUserProfile.userId"
      :email="iamUserProfile.email"
      @enable-user="$emit('enable-user', $event)"
    />
  </div>
</template>
<script>
import { models } from "django-airavata-api";
import UserGroupMembershipEditor from "./UserGroupMembershipEditor";
import EnableUserPanel from "./EnableUserPanel";

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
    UserGroupMembershipEditor,
    EnableUserPanel
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

