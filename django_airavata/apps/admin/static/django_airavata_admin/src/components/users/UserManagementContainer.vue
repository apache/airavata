<template>
  <div>
    <div class="row">
      <div class="col-auto mr-auto">
        <h1 class="h4 mb-4">Manage Users</h1>
      </div>
      <div class="col-auto">
        <b-button :to="{ name: 'extended-user-profile' }"
          >Extended User Profile</b-button
        >
      </div>
      <div class="col-auto">
        <b-dropdown :text="menuText">
          <b-dropdown-item
            :to="{ name: 'identity-service-users' }"
            :exact="true"
            >Identity Service</b-dropdown-item
          >
          <b-dropdown-item
            :to="{ name: 'unverified-email-users' }"
            :exact="true"
            >Unverified Emails</b-dropdown-item
          >
        </b-dropdown>
      </div>
    </div>
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: "user-management-container",
  computed: {
    menuText() {
      if (this.$route.name === "identity-service-users") {
        return "Identity Service";
      } else if (this.$route.name === "unverified-email-users") {
        return "Unverified Emails";
      } else {
        return "Menu";
      }
    },
  },
};
</script>
