<template>
  <div>
    <h1 class="h4 mb-4">User Profile Editor</h1>
    <user-profile-editor v-if="user" v-model="user" @save="onSave" />
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import UserProfileEditor from "../components/UserProfileEditor.vue";

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
  },
};
</script>

<style></style>
