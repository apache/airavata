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
    services.UserService.current().then((user) => {
      this.user = user;
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
  },
};
</script>

<style></style>
