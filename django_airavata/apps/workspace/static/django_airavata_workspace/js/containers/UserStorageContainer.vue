<template>
  <div class="row">
    <div class="col">
      <h1 class="h4">
        Storage
      </h1>
      <p>
        <small class="text-muted"><i class="fa fa-folder-open"></i> {{ username }}</small>
      </p>
      <b-card>
        <router-view :user-storage-path="userStoragePath"></router-view>
      </b-card>
    </div>
  </div>
</template>

<script>
import { services, session, utils } from "django-airavata-api";
import { notifications } from "django-airavata-common-ui";

export default {
  name: "user-storage-container",
  computed: {
    storagePath() {
      if (this.$route.path.startsWith("/")) {
        return this.$route.path.substring(1);
      } else {
        return this.$route.path;
      }
    },
    username() {
      return session.Session.username;
    }
  },
  data() {
    return {
      userStoragePath: null
    };
  },
  methods: {
    loadUserStoragePath(path) {
      return services.UserStoragePathService.get(
        { path },
        { ignoreErrors: true }
      )
        .then(result => {
          this.userStoragePath = result;
        })
        .catch(err => {
          if (err.details.status === 404) {
            this.handleMissingPath(path);
          } else {
            utils.FetchUtils.reportError(err);
          }
        });
    },
    handleMissingPath(path) {
      this.$router.replace("/~/");
      // Display a transient error about the path not existing
      notifications.NotificationList.add(
        new notifications.Notification({
          type: "WARNING",
          message: "Path does not exist: " + path,
          duration: 2
        })
      );
    }
  },
  created() {
    if (this.$route.path === "/") {
      this.$router.replace("/~/");
    } else {
      this.loadUserStoragePath(this.storagePath);
    }
  },
  watch: {
    $route(to, from) {
      this.loadUserStoragePath(this.storagePath);
    }
  }
};
</script>
