<template>
  <div>
    <b-card header="Edit Groups">
      <user-group-membership-editor
        v-if="iamUserProfile.airavataUserProfileExists"
        v-model="localIAMUserProfile.groups"
        :editable-groups="editableGroups"
        :airavata-internal-user-id="iamUserProfile.airavataInternalUserId"
      />
      <b-button @click="groupsUpdated" variant="primary" :disabled="!areGroupsUpdated">Save</b-button>
    </b-card>
    <activate-user-panel
      v-if="
        iamUserProfile.enabled &&
          iamUserProfile.emailVerified &&
          !iamUserProfile.airavataUserProfileExists
      "
      :username="iamUserProfile.userId"
      @activate-user="$emit('enable-user', $event)"
    />
    <enable-user-panel
      v-if="!iamUserProfile.enabled && !iamUserProfile.emailVerified"
      :username="iamUserProfile.userId"
      :email="iamUserProfile.email"
      @enable-user="$emit('enable-user', $event)"
    />
    <delete-user-panel
      v-if="!iamUserProfile.enabled && !iamUserProfile.emailVerified"
      :username="iamUserProfile.userId"
      @delete-user="$emit('delete-user', $event)"
    />
  </div>
</template>
<script>
import { models } from "django-airavata-api";
import UserGroupMembershipEditor from "./UserGroupMembershipEditor";
import ActivateUserPanel from "./ActivateUserPanel";
import EnableUserPanel from "./EnableUserPanel";
import DeleteUserPanel from "./DeleteUserPanel";

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
    EnableUserPanel,
    DeleteUserPanel,
    ActivateUserPanel
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
  },
  computed: {
    currentGroupIds() {
      const groupIds = this.iamUserProfile.groups.map(g => g.id);
      groupIds.sort();
      return groupIds;
    },
    updatedGroupIds() {
      const groupIds = this.localIAMUserProfile.groups.map(g => g.id);
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
    }
  }
};
</script>
