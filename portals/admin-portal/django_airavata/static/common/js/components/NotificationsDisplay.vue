<template>
  <div id="notifications-display">
    <transition-group name="fade" tag="div">
      <template v-for="unhandledError in unhandledErrors">
        <b-alert
          v-if="isUnauthenticatedError(unhandledError.error)"
          variant="warning"
          :key="unhandledError.id"
          show
          dismissible
          @dismissed="dismissUnhandledError(unhandledError)"
        >
          Your login session has expired. Please
          <b-link class="alert-link" :href="loginLinkWithNext"
            >log in again</b-link
          >. You can also
          <b-link class="alert-link" :href="loginLink" target="_blank"
            >login in a separate tab
            <i class="fa fa-external-link-alt" aria-hidden="true"></i
          ></b-link>
          and then return to this tab and try again.
        </b-alert>
        <b-alert
          v-else
          variant="danger"
          :key="unhandledError.id"
          show
          dismissible
          @dismissed="dismissUnhandledError(unhandledError)"
        >
          {{ unhandledError.message }}
        </b-alert>
      </template>
      <b-alert
        v-for="notification in notifications"
        :variant="variant(notification)"
        :key="notification.id"
        :show="notification.duration > 0 ? notification.duration : true"
        dismissible
        @dismissed="dismissNotification(notification)"
      >
        {{ notification.message }}
      </b-alert>
    </transition-group>
    <b-alert variant="danger" :show="apiServerBackUp === false">
      <p>API Server is down.</p>
      <i class="fa fa-sync-alt fa-spin"></i> Checking status ...
    </b-alert>
    <b-alert variant="success" :show="apiServerBackUp" dismissible>
      API Server is back up. Please try again.
    </b-alert>
  </div>
</template>

<script>
import { errors, services } from "django-airavata-api";
import NotificationList from "../notifications/NotificationList";

export default {
  name: "notifications-display",
  data() {
    return {
      notifications: NotificationList.list,
      unhandledErrors: errors.UnhandledErrorDisplayList.list,
      apiServerBackUp: null,
      apiServerBackUpTimestamp: null,
      pollingDelay: 10000,
    };
  },
  methods: {
    dismissNotification: function (notification) {
      NotificationList.remove(notification);
    },
    dismissUnhandledError: function (unhandledError) {
      errors.UnhandledErrorDisplayList.remove(unhandledError);
    },
    variant: function (notification) {
      if (notification.type === "SUCCESS") {
        return "success";
      } else if (notification.type === "ERROR") {
        return "danger";
      } else if (notification.type === "WARNING") {
        return "warning";
      } else {
        return "secondary";
      }
    },
    loadAPIServerStatus() {
      return services.APIServerStatusCheckService.get(
        {},
        { ignoreErrors: true, showSpinner: false }
      ).then((status) => {
        if (status.apiServerUp === true) {
          this.apiServerBackUp = true;
          this.apiServerBackUpTimestamp = Date.now();
        }
      });
    },
    initPollingAPIServerStatus: function () {
      const pollAPIServerStatus = function () {
        if (!this.apiServerBackUp) {
          const repoll = () =>
            setTimeout(pollAPIServerStatus.bind(this), this.pollingDelay);
          this.loadAPIServerStatus().then(repoll, repoll);
        }
      }.bind(this);
      setTimeout(pollAPIServerStatus.bind(this), this.pollingDelay);
    },
    isUnauthenticatedError(error) {
      return errors.ErrorUtils.isUnauthenticatedError(error);
    },
  },
  computed: {
    apiServerDown() {
      // Return true if any notifications indicate that the API Server is down,
      // but excludes notifications that came before the timestamp of the last
      // API server status check
      const notificationsApiServerDown = this.notifications
        ? this.notifications
            .filter((n) => {
              if (this.apiServerBackUpTimestamp) {
                return (
                  n.createdDate.getTime() - this.apiServerBackUpTimestamp > 0
                );
              } else {
                return true;
              }
            })
            .some(
              (n) =>
                n.details &&
                n.details.response &&
                n.details.response.apiServerDown
            )
        : false;
      const unhandledErrorsApiServerDown = this.unhandledErrors
        ? this.unhandledErrors
            .filter((n) => {
              if (this.apiServerBackUpTimestamp) {
                return (
                  n.createdDate.getTime() - this.apiServerBackUpTimestamp > 0
                );
              } else {
                return true;
              }
            })
            .some(
              (e) =>
                e.details &&
                e.details.response &&
                e.details.response.apiServerDown
            )
        : false;
      return notificationsApiServerDown || unhandledErrorsApiServerDown;
    },
    loginLinkWithNext() {
      return errors.ErrorUtils.buildLoginUrl();
    },
    loginLink() {
      return errors.ErrorUtils.buildLoginUrl(false);
    },
  },
  watch: {
    /*
     * Whenever notifications indicate that the API server is down, start
     * polling the API server status so we can let the user know when it is
     * back up.
     */
    apiServerDown(newValue) {
      if (newValue) {
        this.apiServerBackUp = false;
        this.initPollingAPIServerStatus();
      }
    },
  },
};
</script>

<style>
#notifications-display {
  position: fixed;
  top: 75px;
  left: 20vw;
  width: 60vw;
  z-index: 10000;
}
</style>
