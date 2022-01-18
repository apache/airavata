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
    <user-profile-editor
      v-if="user"
      v-model="user"
      @save="onSave"
      @resend-email-verification="resendEmailVerification"
    />
    <b-link
      v-if="user && user.complete"
      class="text-muted small"
      href="/workspace/dashboard"
      >Return to Dashboard</b-link
    >
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import UserProfileEditor from "../components/UserProfileEditor.vue";
import { notifications } from "django-airavata-common-ui";

export default {
  components: { UserProfileEditor },
  name: "user-profile-container",
  created() {
    services.UserService.current()
      .then((user) => {
        this.user = user;
      })
      .then(() => {
        const queryParams = new URLSearchParams(window.location.search);
        if (queryParams.has("code")) {
          this.verifyEmailChange(queryParams.get("code"));
        }
      });
  },
  data() {
    return {
      user: null,
    };
  },
  methods: {
    onSave(value) {
      services.UserService.update({
        lookup: value.id,
        data: value,
      }).then((user) => {
        notifications.NotificationList.add(
          new notifications.Notification({
            type: "SUCCESS",
            message: "User profile saved",
            duration: 5,
          })
        );
        this.user = user;
      });
    },
    resendEmailVerification() {
      services.UserService.resendEmailVerification({
        lookup: this.user.id,
      }).then(() => {
        notifications.NotificationList.add(
          new notifications.Notification({
            type: "SUCCESS",
            message: "Verification link sent",
            duration: 5,
          })
        );
      });
    },
    verifyEmailChange(code) {
      services.UserService.verifyEmailChange({
        lookup: this.user.id,
        data: { code: code },
      }).then((user) => {
        // User now updated with email change
        this.user = user;
        notifications.NotificationList.add(
          new notifications.Notification({
            type: "SUCCESS",
            message: "Email address verified and updated",
            duration: 5,
          })
        );
        // Update URL, removing the code from the query string
        window.history.replaceState({}, "", "/auth/user-profile/");
      });
    },
  },
};
</script>

<style></style>
