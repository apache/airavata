<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Experiment Statistics</h1>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-2 col-md-4">
        <experiment-statistics-card
          bg-variant="primary"
          header-text-variant="white"
          :count="experimentStatistics.allExperimentCount || 0"
          title="Total Experiments"
        >
          <span slot="link-text">All</span>
        </experiment-statistics-card>
      </div>
      <div class="col-lg-2 col-md-4">
        <experiment-statistics-card
          bg-variant="light"
          :count="experimentStatistics.createdExperimentCount || 0"
          :states="createdStates"
          title="Created Experiments"
        >
        </experiment-statistics-card>
      </div>
      <div class="col-lg-2 col-md-4">
        <experiment-statistics-card
          bg-variant="light"
          header-text-variant="success"
          :count="experimentStatistics.runningExperimentCount || 0"
          :states="runningStates"
          title="Running Experiments"
        >
        </experiment-statistics-card>

      </div>
      <div class="col-lg-2 col-md-4">
        <experiment-statistics-card
          bg-variant="success"
          header-text-variant="white"
          link-variant="success"
          :count="experimentStatistics.completedExperimentCount || 0"
          :states="completedStates"
          title="Completed Experiments"
        >
        </experiment-statistics-card>
      </div>
      <div class="col-lg-2 col-md-4">
        <experiment-statistics-card
          bg-variant="warning"
          header-text-variant="white"
          link-variant="warning"
          :count="experimentStatistics.cancelledExperimentCount || 0"
          :states="canceledStates"
          title="Cancelled Experiments"
        >
        </experiment-statistics-card>
      </div>
      <div class="col-lg-2 col-md-4">
        <experiment-statistics-card
          bg-variant="danger"
          header-text-variant="white"
          link-variant="danger"
          :count="experimentStatistics.failedExperimentCount || 0"
          :states="failedStates"
          title="Failed Experiments"
        >
        </experiment-statistics-card>

      </div>
    </div>
  </div>
</template>
<script>
import { models, services } from "django-airavata-api";
import ExperimentStatisticsCard from "./ExperimentStatisticsCard";

export default {
  name: "experiment-statistics-container",
  data() {
    return {
      experimentStatistics: {}
    };
  },
  created() {
    services.ExperimentStatisticsService.get().then(
      stats => (this.experimentStatistics = stats)
    );
  },
  components: {
    ExperimentStatisticsCard
  },
  computed: {
    createdStates() {
      // TODO: moved to ExperimentStatistics model
      return [models.ExperimentState.CREATED, models.ExperimentState.VALIDATED];
    },
    runningStates() {
      return [
        models.ExperimentState.SCHEDULED,
        models.ExperimentState.LAUNCHED,
        models.ExperimentState.EXECUTING
      ];
    },
    completedStates() {
      return [models.ExperimentState.COMPLETED];
    },
    canceledStates() {
      return [
        models.ExperimentState.CANCELING,
        models.ExperimentState.CANCELED
      ];
    },
    failedStates() {
      return [models.ExperimentState.FAILED];
    }
  }
};
</script>
