<template>
  <b-card>
    <b-form-group label="Username">
      <b-form-input disabled :value="user.username" />
    </b-form-group>
    <b-form-group label="First Name">
      <b-form-input v-model="user.first_name" />
    </b-form-group>
    <b-form-group label="Last Name">
      <b-form-input v-model="user.last_name" />
    </b-form-group>
    <b-form-group label="Email">
      <b-form-input v-model="user.email" />
      <b-alert class="mt-1" show v-if="user.pending_email_change"
        >Once you verify your email address at
        <strong>{{ user.pending_email_change.email_address }}</strong
        >, your email address will be updated. If you didn't receive the
        verification email,
        <b-link @click="$emit('resend-email-verification')"
          >click here to resend verification link.</b-link
        ></b-alert
      >
    </b-form-group>
    <b-button variant="primary" @click="$emit('save', user)">Save</b-button>
    <b-button>Cancel</b-button>
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
export default {
  name: "user-profile-editor",
  props: {
    value: {
      type: models.User,
      required: true,
    },
  },
  data() {
    return {
      user: this.cloneValue(),
    };
  },
  methods: {
    cloneValue() {
      return JSON.parse(JSON.stringify(this.value));
    },
  },
};
</script>

<style></style>
