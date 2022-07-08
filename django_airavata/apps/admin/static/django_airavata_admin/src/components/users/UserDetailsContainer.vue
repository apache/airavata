<template>
  <b-tabs content-class="mt-3 px-2">
    <b-tab
      title="User Profile"
      :active="iamUserProfile.airavataUserProfileExists"
    >
      <b-alert
        variant="warning"
        show
        v-if="!iamUserProfile.userProfileComplete"
      >
        This user has not completed their user profile. An incomplete user
        profile is shown below.
      </b-alert>
      <b-alert variant="danger" show v-if="isUsernameInvalid">
        The user has an invalid username. Please use
        <strong>Change Username</strong> under the
        <strong>Troubleshooting</strong> tab to fix the user's username.
      </b-alert>
      <edit-groups-panel
        v-if="iamUserProfile.airavataUserProfileExists"
        :value="localIAMUserProfile.groups"
        :editable-groups="editableGroups"
        :airavata-internal-user-id="iamUserProfile.airavataInternalUserId"
        @save="groupsUpdated"
      />
      <user-profile-panel :iamUserProfile="iamUserProfile" />
      <extended-user-profile-panel :iamUserProfile="iamUserProfile" />
      <external-idp-user-info-panel
        v-if="hasExternalIDPUserInfo"
        :externalIDPUserInfo="localIAMUserProfile.externalIDPUserInfo"
      />
    </b-tab>
    <b-tab
      title="Troubleshooting"
      :active="!iamUserProfile.airavataUserProfileExists"
    >
      <activate-user-panel
        v-if="
          iamUserProfile.enabled &&
          iamUserProfile.emailVerified &&
          iamUserProfile.userProfileComplete &&
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
      <b-alert variant="danger" show v-if="isUsernameInvalid">
        The user has an invalid username. Please fix the user's username so that
        they can complete their user profile.
      </b-alert>
      <change-username-panel
        :username="iamUserProfile.userId"
        :email="iamUserProfile.email"
        :airavata-user-profile-exists="iamUserProfile.airavataUserProfileExists"
        @update-username="$emit('update-username', $event)"
      />
    </b-tab>
  </b-tabs>
</template>
<script>
import { models } from "django-airavata-api";
import UserGroupMembershipEditor from "./UserGroupMembershipEditor";
import ActivateUserPanel from "./ActivateUserPanel";
import EnableUserPanel from "./EnableUserPanel";
import DeleteUserPanel from "./DeleteUserPanel";
import ChangeUsernamePanel from "./ChangeUsernamePanel.vue";
import EditGroupsPanel from "./EditGroupsPanel.vue";
import ExternalIDPUserInfoPanel from "./ExternalIDPUserInfoPanel.vue";
import UserProfilePanel from "./UserProfilePanel.vue";
import ExtendedUserProfilePanel from "./ExtendedUserProfilePanel.vue";

export default {
  name: "user-details-container",
  props: {
    iamUserProfile: {
      type: models.IAMUserProfile,
      required: true,
    },
    editableGroups: {
      type: Array,
      required: true,
    },
  },
  components: {
    UserGroupMembershipEditor,
    EnableUserPanel,
    DeleteUserPanel,
    ActivateUserPanel,
    ChangeUsernamePanel,
    EditGroupsPanel,
    "external-idp-user-info-panel": ExternalIDPUserInfoPanel,
    UserProfilePanel,
    ExtendedUserProfilePanel,
  },
  data() {
    return {
      localIAMUserProfile: this.iamUserProfile.clone(),
    };
  },
  watch: {
    iamUserProfile(newValue) {
      this.localIAMUserProfile = newValue.clone();
    },
  },
  methods: {
    groupsUpdated(groups) {
      this.localIAMUserProfile.groups = groups;
      this.$emit("groups-updated", this.localIAMUserProfile);
    },
  },
  computed: {
    hasExternalIDPUserInfo() {
      return (
        Object.keys(this.localIAMUserProfile.externalIDPUserInfo).length !== 0
      );
    },
    isUsernameInvalid() {
      return (
        this.iamUserProfile.userProfileInvalidFields.indexOf("username") >= 0
      );
    },
  },
};
</script>
