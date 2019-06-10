<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Experiment Statistics from {{fromTimeDisplay}} to {{toTimeDisplay}}</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-card header="Filter">
          <b-input-group class="w-100">
            <b-input-group-prepend is-text>
              <i class="fa fa-calendar-week" aria-hidden="true"></i>
            </b-input-group-prepend>
            <flat-pickr
              :value="dateRange"
              :config="dateConfig"
              @on-change="dateRangeChanged"
              class="form-control"
            />
            <b-input-group-append>
              <b-button @click="getPast24Hours" variant="outline-secondary">Past 24 Hours</b-button>
              <b-button @click="getPastWeek" variant="outline-secondary">Past Week</b-button>
            </b-input-group-append>
          </b-input-group>
        </b-card>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-2 col-md-4">
        <experiment-statistics-card
          bg-variant="primary"
          header-text-variant="white"
          :count="experimentStatistics.allExperimentCount || 0"
          title="Total Experiments"
          @click="selectExperiments(experimentStatistics.allExperiments)"
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
          @click="selectExperiments(experimentStatistics.createdExperiments)"
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
          @click="selectExperiments(experimentStatistics.runningExperiments)"
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
          @click="selectExperiments(experimentStatistics.completedExperiments)"
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
          @click="selectExperiments(experimentStatistics.cancelledExperiments)"
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
          @click="selectExperiments(experimentStatistics.failedExperiments)"
        >
        </experiment-statistics-card>

      </div>
    </div>
    <div
      class="row"
      v-if="items.length"
    >
      <div class="col">
        <b-card>
          <b-table
            :fields="fields"
            :items="items"
          >
            <template
              slot="executionId"
              slot-scope="data"
            >
              <application-name :application-interface-id="data.value" />
            </template>
            <template
              slot="resourceHostId"
              slot-scope="data"
            >
              <compute-resource-name :compute-resource-id="data.value" />
            </template>
            <template
              slot="creationTime"
              slot-scope="data"
            >
              <human-date :date="data.value" />
            </template>
            <template
              slot="experimentStatus"
              slot-scope="data"
            >
              <experiment-status-badge :status-name="data.value.name" />
            </template>
          </b-table>
        </b-card>
      </div>
    </div>
  </div>
</template>
<script>
import { models, services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
import ExperimentStatisticsCard from "./ExperimentStatisticsCard";

import moment from "moment";

export default {
  name: "experiment-statistics-container",
  data() {
    const fromTime = new Date(Date.now() - 24 * 60 * 60 * 1000); // 24 hours ago
    const toTime = new Date();
    return {
      experimentStatistics: {},
      selectedExperimentSummaries: null,
      fromTime: fromTime,
      toTime: toTime,
      dateRange: [fromTime, toTime],
      dateConfig: {
        mode: "range",
        wrap: true,
        maxDate: new Date()
      }
    };
  },
  created() {
    this.loadStatistics();
  },
  components: {
    ExperimentStatisticsCard,
    "application-name": components.ApplicationName,
    "compute-resource-name": components.ComputeResourceName,
    "human-date": components.HumanDate,
    "experiment-status-badge": components.ExperimentStatusBadge
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
    },
    fields() {
      return [
        {
          key: "name",
          label: "Name"
        },
        {
          key: "userName",
          label: "Owner"
        },
        {
          key: "executionId",
          label: "Application"
        },
        {
          key: "resourceHostId",
          label: "Resource"
        },
        {
          key: "creationTime",
          label: "Creation Time"
        },
        {
          key: "experimentStatus",
          label: "Status"
        },
        {
          key: "actions",
          label: "Actions"
        }
      ];
    },
    items() {
      if (this.selectedExperimentSummaries) {
        return this.selectedExperimentSummaries;
      } else {
        return [];
      }
    },
    fromTimeDisplay() {
      return moment(this.fromTime).format("MMM Do YYYY");
    },
    toTimeDisplay() {
      return moment(this.toTime).format("MMM Do YYYY");
    }
  },
  methods: {
    selectExperiments(experiments) {
      this.selectedExperimentSummaries = experiments;
    },
    dateRangeChanged(selectedDates) {
      [this.fromTime, this.toTime] = selectedDates;
      if (this.fromTime && this.toTime) {
        this.loadStatistics();
      }
    },
    loadStatistics() {
      services.ExperimentStatisticsService.get({
        fromTime: this.fromTime.toJSON(),
        toTime: this.toTime.toJSON()
      }).then(stats => (this.experimentStatistics = stats));
    },
    getPast24Hours() {
      this.fromTime = this.daysAgo(1);
      this.toTime = new Date();
      this.updateDateRange();
      this.loadStatistics();
    },
    getPastWeek() {
      this.fromTime = this.daysAgo(7);
      this.toTime = new Date();
      this.updateDateRange();
      this.loadStatistics();
    },
    updateDateRange() {
      this.dateRange = [
        moment(this.fromTime).format("YYYY-MM-DD"),
        moment(this.toTime).format("YYYY-MM-DD")
      ];
    },
    daysAgo(days) {
      return new Date(Date.now() - days * 24 * 60 * 60 * 1000);
    }
  }
};
</script>
