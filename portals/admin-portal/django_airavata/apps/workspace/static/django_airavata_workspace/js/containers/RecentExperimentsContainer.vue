<template>
  <sidebar>
    <sidebar-header
      title="My Recent Experiments"
      :view-all-url="viewAllExperiments"
    />
    <sidebar-feed :feed-items="feedItems">
      <template slot="description" slot-scope="slotProps">
        <experiment-status-badge :status-name="slotProps.feedItem.statusName" />
        <i
          v-if="slotProps.feedItem.isProgressing"
          class="fa fa-sync-alt fa-spin ml-1"
        ></i>
      </template>
    </sidebar-feed>
  </sidebar>
</template>

<script>
import urls from "../utils/urls";
import { errors, models, services, utils } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
export default {
  name: "recent-experiments-container",
  props: {
    viewAllExperiments: String,
    username: String,
  },
  components: {
    sidebar: components.Sidebar,
    "sidebar-header": components.SidebarHeader,
    "sidebar-feed": components.SidebarFeed,
    "experiment-status-badge": components.ExperimentStatusBadge,
  },
  created() {
    this.pollExperiments();
  },
  methods: {
    pollExperiments() {
      this.loadExperiments()
        .then(() => {
          setTimeout(
            function () {
              this.pollExperiments();
            }.bind(this),
            this.refreshDelay
          );
        })
        .catch(() => {
          // If loading experiments fails, just ignore. This can happen if the
          // user navigates away from the page while a request is executing.
        });
    },
    loadExperiments() {
      return services.ExperimentSearchService.list(
        {
          limit: 5,
          offset: 0,
          [models.ExperimentSearchFields.USER_NAME.name]: this.username,
        },
        {
          showSpinner: false,
          ignoreErrors: true,
        }
      ).then((experiments) => {
        this.feedItems = experiments.results.map((e) => {
          return {
            id: e.experimentId,
            statusName: e.experimentStatus.name,
            title: e.name,
            url: urls.viewExperiment(e),
            timestamp: e.statusUpdateTime,
            interfaceId: e.executionId,
            isProgressing: e.convertToExperiment().isProgressing,
            type: null,
          };
        });
        // Load any application interfaces that haven't been loaded yet, so that
        // we can display the applicationName of each experiment
        const unloadedInterfaceIds = {};
        this.feedItems
          .filter((i) => !(i.interfaceId in this.applicationInterfaces))
          .forEach((i) => (unloadedInterfaceIds[i.interfaceId] = true));
        Promise.all(
          Object.keys(unloadedInterfaceIds).map((interfaceId) => {
            return this.loadApplicationInterface(interfaceId);
          })
        ).then(() => {
          this.populateApplicationNames();
        });
      });
    },
    loadApplicationInterface(interfaceId) {
      return services.ApplicationInterfaceService.retrieve(
        {
          lookup: interfaceId,
        },
        {
          showSpinner: false,
          ignoreErrors: true,
        }
      )
        .then((applicationInterface) => {
          this.applicationInterfaces[interfaceId] = applicationInterface;
        })
        .catch((error) => {
          // ignore if missing
          if (errors.ErrorUtils.isNotFoundError(error)) {
            this.applicationInterfaces[interfaceId] = null;
          } else {
            throw error;
          }
        })
        .catch(utils.FetchUtils.reportError);
    },
    populateApplicationNames() {
      this.feedItems
        .filter((i) => i.type === null)
        .forEach((feedItem) => {
          if (
            feedItem.interfaceId in this.applicationInterfaces &&
            this.applicationInterfaces[feedItem.interfaceId]
          ) {
            feedItem.type = this.applicationInterfaces[
              feedItem.interfaceId
            ].applicationName;
          }
        });
    },
  },
  data() {
    return {
      feedItems: null,
      applicationInterfaces: {},
      refreshDelay: 10000,
    };
  },
};
</script>
