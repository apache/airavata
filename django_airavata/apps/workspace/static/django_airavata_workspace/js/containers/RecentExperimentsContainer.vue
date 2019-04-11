<template>
  <div>
    <sidebar-header
      title="Recent Experiments"
      :view-all-url="viewAllExperiments"
    />
    <sidebar-feed :feed-items="feedItems">
      <experiment-status-badge
        :status-name="slotProps.feedItem.statusName"
        slot="description"
        slot-scope="slotProps"
      />
    </sidebar-feed>
  </div>
</template>

<script>
import ExperimentStatusBadge from "../components/experiment/ExperimentStatusBadge.vue";
import urls from "../utils/urls";
import { services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
export default {
  name: "recent-experiments-container",
  props: {
    viewAllExperiments: String
  },
  components: {
    "sidebar-header": components.SidebarHeader,
    "sidebar-feed": components.SidebarFeed,
    ExperimentStatusBadge
  },
  created() {
    services.ExperimentSearchService.list({ limit: 5, offset: 0 }).then(
      experiments => {
        this.feedItems = experiments.results.map(e => {
          return {
            id: e.experimentId,
            statusName: e.experimentStatus.name,
            title: e.name,
            url: urls.viewExperiment(e),
            timestamp: e.statusUpdateTime
          };
        });
      }
    );
  },
  data() {
    return {
      feedItems: null
    };
  }
};
</script>

