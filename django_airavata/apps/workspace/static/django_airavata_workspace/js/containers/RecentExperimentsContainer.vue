<template>
  <div>
    <sidebar-header
      title="My Recent Experiments"
      :view-all-url="viewAllExperiments"
    />
    <sidebar-feed :feed-items="feedItems">
      <template
        slot="description"
        slot-scope="slotProps"
      >
        <experiment-status-badge :status-name="slotProps.feedItem.statusName" />
        <i
          v-if="slotProps.feedItem.isProgressing"
          class="fa fa-sync-alt fa-spin ml-1"
        ></i>
      </template>
    </sidebar-feed>
  </div>
</template>

<script>
import ExperimentStatusBadge from "../components/experiment/ExperimentStatusBadge.vue";
import urls from "../utils/urls";
import { models, services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
export default {
  name: "recent-experiments-container",
  props: {
    viewAllExperiments: String,
    username: String
  },
  components: {
    "sidebar-header": components.SidebarHeader,
    "sidebar-feed": components.SidebarFeed,
    ExperimentStatusBadge
  },
  created() {
    this.pollExperiments();
  },
  methods: {
    pollExperiments() {
      this.loadExperiments().then(() => {
        setTimeout(
          function() {
            this.pollExperiments();
          }.bind(this),
          this.refreshDelay
        );
      });
    },
    loadExperiments() {
      return services.ExperimentSearchService.list(
        {
          limit: 5,
          offset: 0,
          [models.ExperimentSearchFields.USER_NAME.name]: this.username
        },
        {
          showSpinner: false
        }
      ).then(experiments => {
        this.feedItems = experiments.results.map(e => {
          return {
            id: e.experimentId,
            statusName: e.experimentStatus.name,
            title: e.name,
            url: urls.viewExperiment(e),
            timestamp: e.statusUpdateTime,
            interfaceId: e.executionId,
            isProgressing: e.convertToExperiment().isProgressing,
            type:
              e.executionId in this.applicationInterfaces
                ? this.applicationInterfaces[e.executionId].applicationName
                : null
          };
        });
        // Load any application interfaces that haven't been loaded yet, so that
        // we can display the applicationName of each experiment
        const unloadedInterfaceIds = {};
        this.feedItems
          .filter(i => i.type === null)
          .forEach(i => (unloadedInterfaceIds[i.interfaceId] = true));
        Promise.all(
          Object.keys(unloadedInterfaceIds).map(interfaceId => {
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
          lookup: interfaceId
        },
        {
          showSpinner: false
        }
      ).then(applicationInterface => {
        this.applicationInterfaces[interfaceId] = applicationInterface;
      });
    },
    populateApplicationNames() {
      this.feedItems
        .filter(i => i.type === null)
        .forEach(feedItem => {
          feedItem.type = this.applicationInterfaces[
            feedItem.interfaceId
          ].applicationName;
        });
    }
  },
  data() {
    return {
      feedItems: null,
      applicationInterfaces: {},
      refreshDelay: 10000
    };
  }
};
</script>

