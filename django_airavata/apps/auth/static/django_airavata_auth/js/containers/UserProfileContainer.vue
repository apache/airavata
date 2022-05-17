<template>
  <div>
    <h1 class="h4 mb-4">User Profile Editor</h1>
    <b-alert v-if="user && !user.username_valid" show variant="danger">
      <p>
        Unfortunately the username on your profile is invalid, which prevents
        creating or updating your user profile. The administrators have been
        notified and will be able to update your user account with a valid
        username. An administrator will notify you once your username has been
        updated to a valid value.
      </p>
      <p>
        In the meantime, please complete as much of your profile as possible.
      </p>
    </b-alert>
    <b-alert v-else-if="user && !user.complete" show>
      >Please complete your user profile before continuing.</b-alert
    >
    <b-card>
      <user-profile-editor
        ref="userProfileEditor"
        @save="onSave"
        @resend-email-verification="handleResendEmailVerification"
      />
      <!-- include extended-user-profile-editor if there are extendedUserProfileFields -->
      <template
        v-if="extendedUserProfileFields && extendedUserProfileFields.length > 0"
      >
        <hr />
        <extended-user-profile-editor ref="extendedUserProfileEditor" />
      </template>

      <b-button variant="primary" @click="onSave">Save</b-button>
    </b-card>
    <b-link
      v-if="user && user.complete"
      class="text-muted small"
      href="/workspace/dashboard"
      >Return to Dashboard</b-link
    >
  </div>
</template>

<script>
import UserProfileEditor from "../components/UserProfileEditor.vue";
import { notifications } from "django-airavata-common-ui";
import { mapActions, mapGetters } from "vuex";
import ExtendedUserProfileEditor from "../components/ExtendedUserProfileEditor.vue";

export default {
  components: { UserProfileEditor, ExtendedUserProfileEditor },
  name: "user-profile-container",
  async created() {
    await this.loadCurrentUser();
    await this.loadExtendedUserProfileFields();
    await this.loadExtendedUserProfileValues();

    const queryParams = new URLSearchParams(window.location.search);
    if (queryParams.has("code")) {
      await this.verifyEmailChange({ code: queryParams.get("code") });
      notifications.NotificationList.add(
        new notifications.Notification({
          type: "SUCCESS",
          message: "Email address verified and updated",
          duration: 5,
        })
      );
      // Update URL, removing the code from the query string
      window.history.replaceState({}, "", "/auth/user-profile/");
    }
  },
  data() {
    return {
      invalidForm: false,
    };
  },
  computed: {
    ...mapGetters("userProfile", ["user"]),
    ...mapGetters("extendedUserProfile", ["extendedUserProfileFields"]),
  },
  methods: {
    ...mapActions("userProfile", [
      "loadCurrentUser",
      "verifyEmailChange",
      "updateUser",
      "resendEmailVerification",
    ]),
    ...mapActions("extendedUserProfile", [
      "loadExtendedUserProfileFields",
      "loadExtendedUserProfileValues",
      "saveExtendedUserProfileValues",
    ]),
    async onSave() {
      if (
        this.$refs.userProfileEditor.valid &&
        this.$refs.extendedUserProfileEditor.valid
      ) {
        await this.updateUser();
        await this.saveExtendedUserProfileValues();
        notifications.NotificationList.add(
          new notifications.Notification({
            type: "SUCCESS",
            message: "User profile saved",
            duration: 5,
          })
        );
      } else {
        this.$refs.extendedUserProfileEditor.touch();
      }
    },
    async handleResendEmailVerification() {
      await this.resendEmailVerification();
      notifications.NotificationList.add(
        new notifications.Notification({
          type: "SUCCESS",
          message: "Verification link sent",
          duration: 5,
        })
      );
    },
  },
};
</script>

<style></style>
