<template>
  <div>
    <h1 class="h4 mb-4">User Profile Editor</h1>
    <user-profile-editor
      v-if="user"
      v-model="user"
      @save="onSave"
      @resend-email-verification="resendEmailVerification"
    />
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
