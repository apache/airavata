<template>
  <b-card header="Activate User">
    <p class="card-text">
      User {{ username }} has verified their email address, but doesn't yet have
      an Airavata User Profile. Click <b>Activate</b>
      to create an Airavata User Profile for this user. This will allow the user
      to be assigned to groups.
    </p>
    <b-button @click="activate">Activate</b-button>
  </b-card>
</template>

<script>
export default {
  name: "activate-user-panel",
  props: {
    username: {
      type: String,
      required: true,
    },
  },
  methods: {
    activate() {
      this.$emit("activate-user", this.username);
    },
  },
};
</script>
