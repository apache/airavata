<template>
  <div>
    <pga-link />
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Dashboard</h1>
        <workspace-notices-management-container/>
        <h2 class="h6 mb-2 text-uppercase text-muted">Applications</h2>
      </div>
    </div>
    <div class="row" v-if="showNewUserMessage">
      <div class="col">
        <b-alert variant="info" show
          >Welcome {{ userProfile.firstName }} {{ userProfile.lastName }}! You
          currently don't have access to run any applications but the
          administrator of this gateway has been notified and will be in contact
          to grant you the appropriate privileges.</b-alert
        >
      </div>
    </div>
    <template v-if="favoriteApplicationsData.length > 0">
      <div class="row">
        <div class="col">
          <h1 class="h5 mb-2">Favorites</h1>
        </div>
      </div>
      <div class="row">
        <application-card
          v-for="item in favoriteApplicationsData"
          v-bind:appModule="item.appModule"
          v-bind:key="item.appModule.appModuleId"
          @app-selected="handleAppSelected"
          :disabled="item.disabled"
          @favorite="markFavorite(item.appModule)"
          @unfavorite="markNotFavorite(item.appModule)"
          ref="favoriteApplicationCards"
        >
          <favorite-toggle
            slot="card-actions"
            :favorite="true"
            class="card-link"
            @favorite="markFavorite(item.appModule)"
            @unfavorite="markNotFavorite(item.appModule)"
          />
        </application-card>
      </div>
      <hr />
    </template>
    <div class="row">
      <application-card
        v-for="item in nonFavoriteApplicationsData"
        v-bind:appModule="item.appModule"
        v-bind:key="item.appModule.appModuleId"
        @app-selected="handleAppSelected"
        :disabled="item.disabled"
        @favorite="markFavorite(item.appModule)"
        @unfavorite="markNotFavorite(item.appModule)"
      >
        <favorite-toggle
          slot="card-actions"
          :favorite="false"
          class="card-link"
          @favorite="markFavorite(item.appModule)"
          @unfavorite="markNotFavorite(item.appModule)"
        />
      </application-card>
    </div>
  </div>
</template>

<script>
import { services, session } from "django-airavata-api";
import { components as comps } from "django-airavata-common-ui";
import urls from "../utils/urls";
import PgaLink from "../components/PgaLink";
import WorkspaceNoticesManagementContainer from "../components/notices/WorkspaceNoticesManagementContainer";

export default {
  name: "dashboard-container",
  data() {
    return {
      accessibleAppModules: null,
      userProfile: null,
      allApplicationModules: null,
      workspacePreferences: null,
    };
  },
  components: {
    WorkspaceNoticesManagementContainer,
    "application-card": comps.ApplicationCard,
    "favorite-toggle": comps.FavoriteToggle,
    "pga-link": PgaLink,
  },
  methods: {
    handleAppSelected: function (appModule) {
      urls.navigateToCreateExperiment(appModule);
    },
    markFavorite(appModule) {
      services.ApplicationModuleService.favorite({
        lookup: appModule.appModuleId,
      })
        .then(() => {
          return services.WorkspacePreferencesService.get().then(
            (prefs) => (this.workspacePreferences = prefs)
          );
        })
        .then(() => {
          const index = this.favoriteApplicationsData.findIndex(
            (data) => data.appModule.appModuleId === appModule.appModuleId
          );
          this.$nextTick(() => {
            this.$refs.favoriteApplicationCards[index].$el.scrollIntoView({
              behavior: "smooth",
              block: "center",
            });
          });
        });
    },
    markNotFavorite(appModule) {
      services.ApplicationModuleService.unfavorite({
        lookup: appModule.appModuleId,
      }).then(() => {
        return services.WorkspacePreferencesService.get().then(
          (prefs) => (this.workspacePreferences = prefs)
        );
      });
    },
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
        this.accessibleAppModules &&
        this.accessibleAppModules.length === 0
      );
    },
    accessibleModuleIds() {
      return this.accessibleAppModules
        ? this.accessibleAppModules.map((a) => a.appModuleId)
        : [];
    },
    allApplicationData() {
      return this.allApplicationModules
        ? this.allApplicationModules.map((app) => {
            return {
              appModule: app,
              disabled: this.accessibleModuleIds.indexOf(app.appModuleId) < 0,
            };
          })
        : [];
    },
    favoriteApplicationsData() {
      return this.allApplicationData.filter(
        (app) =>
          this.favoriteApplicationIds.indexOf(app.appModule.appModuleId) >= 0
      );
    },
    nonFavoriteApplicationsData() {
      return this.allApplicationData.filter(
        (app) =>
          this.favoriteApplicationIds.indexOf(app.appModule.appModuleId) < 0
      );
    },
    favoriteApplicationIds() {
      if (
        this.workspacePreferences &&
        this.workspacePreferences.application_preferences
      ) {
        return this.workspacePreferences.application_preferences
          .filter((p) => p.favorite)
          .map((p) => p.application_id);
      } else {
        return [];
      }
    },
  },
  beforeMount: function () {
    services.ApplicationModuleService.list().then(
      (result) => (this.accessibleAppModules = result)
    );
    services.UserProfileService.retrieve({
      lookup: session.Session.username,
    }).then((userProfile) => (this.userProfile = userProfile));
    // Load all application, including ones that aren't accessible by this user
    services.ApplicationModuleService.listAll().then(
      (result) => (this.allApplicationModules = result)
    );
    services.WorkspacePreferencesService.get().then(
      (prefs) => (this.workspacePreferences = prefs)
    );
  },
};
</script>
