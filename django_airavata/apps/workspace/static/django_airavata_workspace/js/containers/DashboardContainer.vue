<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Dashboard</h1>
      </div>
    </div>
    <div
      class="row"
      v-if="showNewUserMessage"
    >
      <div class="col">
        <b-alert
          variant="info"
          show
        >Welcome {{ userProfile.firstName }} {{ userProfile.lastName }}!
          You currently don't have access to run any applications but the
          administrator of this gateway has been notified and will be in
          contact to grant you the appropriate privileges.</b-alert>
      </div>
    </div>
    <div class="row">
      <application-card
        v-for="item in applicationModules"
        v-bind:appModule="item"
        v-bind:key="item.appModuleId"
        @app-selected="handleAppSelected"
      >
      </application-card>
    </div>

  </div>
</template>

<script>
import { services, session } from "django-airavata-api";
import { components as comps } from "django-airavata-common-ui";
import urls from "../utils/urls";

export default {
  name: "dashboard-container",
  data() {
    return {
      applicationModules: null,
      userProfile: null
    };
  },
  components: {
    "application-card": comps.ApplicationCard
  },
  methods: {
    handleAppSelected: function(appModule) {
      urls.navigateToCreateExperiment(appModule);
    }
  },
  computed: {
    isNewUser() {
      return (
        this.userProfile &&
        Date.now() - this.userProfile.creationTime.getTime() <
          7 * 24 * 60 * 60 * 1000
      );
    },
    showNewUserMessage() {
      return (
        this.isNewUser &&
        this.userProfile &&
        this.applicationModules &&
        this.applicationModules.length === 0
      );
    }
  },
  beforeMount: function() {
    services.ApplicationModuleService.list().then(
      result => (this.applicationModules = result)
    );
    services.UserProfileService.retrieve({
      lookup: session.Session.username
    }).then(userProfile => (this.userProfile = userProfile));
  }
};
</script>
