<template>
  <b-card header="Enable User">
    <p class="card-text">
      Enable user {{ username }} to log in. By clicking <b>Enable</b> you are
      verifying that the user's email address is {{ email }}
    </p>
    <b-button @click="enable">Enable</b-button>
  </b-card>
</template>

<script>
export default {
  name: "enable-user-panel",
  props: {
    username: {
      type: String,
      required: true,
    },
    email: {
      type: String,
      required: true,
    },
  },
  methods: {
    enable() {
      this.$emit("enable-user", this.username);
    },
  },
};
</script>
