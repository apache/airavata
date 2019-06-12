<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Experiment Statistics from {{fromTimeDisplay}} to {{toTimeDisplay}}</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-card header="Filter Options">
          <b-input-group class="w-100 mb-2">
            <b-input-group-prepend is-text>
              <i
                class="fa fa-calendar-week"
                aria-hidden="true"
              ></i>
            </b-input-group-prepend>
            <flat-pickr
              :value="dateRange"
              :config="dateConfig"
              @on-change="dateRangeChanged"
              class="form-control"
            />
            <b-input-group-append>
              <b-button
                @click="getPast24Hours"
                variant="outline-secondary"
              >Past 24 Hours</b-button>
              <b-button
                @click="getPastWeek"
                variant="outline-secondary"
              >Past Week</b-button>
            </b-input-group-append>
          </b-input-group>
          <b-dropdown
            text="Add Filters"
            class="mb-2"
          >
            <b-dropdown-item
              v-if="!usernameFilterEnabled"
              @click="usernameFilterEnabled=true"
            >Username</b-dropdown-item>
            <b-dropdown-item
              v-if="!applicationNameFilterEnabled"
              @click="applicationNameFilterEnabled=true"
            >Application Name</b-dropdown-item>
            <b-dropdown-item
              v-if="!hostnameFilterEnabled"
              @click="hostnameFilterEnabled=true"
            >Hostname</b-dropdown-item>
          </b-dropdown>
          <b-input-group
            v-if="usernameFilterEnabled"
            class="mb-2"
          >
            <b-form-input
              v-model="usernameFilter"
              placeholder="Username"
              @keydown.native.enter="loadStatistics"
            />
            <b-input-group-append>
              <b-button @click="removeUsernameFilter">
                <i class="fa fa-times"></i>
                <span class="sr-only">Remove username filter</span>
              </b-button>
            </b-input-group-append>
          </b-input-group>
          <b-input-group
            v-if="applicationNameFilterEnabled"
            class="mb-2"
          >
            <b-form-select
              v-model="applicationNameFilter"
              :options="applicationNameOptions"
              @input="loadStatistics"
            >
              <template slot="first">
                <option
                  :value="null"
                  disabled
                >Select an application to filter on</option>
              </template>
            </b-form-select>
            <b-input-group-append>
              <b-button @click="removeApplicationNameFilter">
                <i class="fa fa-times"></i>
                <span class="sr-only">Remove application name filter</span>
              </b-button>
            </b-input-group-append>
          </b-input-group>
          <b-input-group
            v-if="hostnameFilterEnabled"
            class="mb-2"
          >
            <b-form-select
              v-model="hostnameFilter"
              :options="hostnameOptions"
              @input="loadStatistics"
            >
              <template slot="first">
                <option
                  :value="null"
                  disabled
                >Select compute resource to filter on</option>
              </template>
            </b-form-select>
            <b-input-group-append>
              <b-button @click="removeHostnameFilter">
                <i class="fa fa-times"></i>
                <span class="sr-only">Remove hostname filter</span>
              </b-button>
            </b-input-group-append>
          </b-input-group>
          <template slot="footer">
            <div class="d-flex justify-content-end">
              <b-button
                @click="loadStatistics"
                class="ml-auto"
                variant="primary"
              >Get Statistics</b-button>
            </div>
          </template>
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
          @click="selectedExperimentSummariesKey = 'allExperiments'"
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
          @click="selectedExperimentSummariesKey = 'createdExperiments'"
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
          @click="selectedExperimentSummariesKey = 'runningExperiments'"
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
          @click="selectedExperimentSummariesKey = 'completedExperiments'"
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
          @click="selectedExperimentSummariesKey = 'cancelledExperiments'"
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
          @click="selectedExperimentSummariesKey = 'failedExperiments'"
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
import { models, services, utils } from "django-airavata-api";
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
      selectedExperimentSummariesKey: null,
      fromTime: fromTime,
      toTime: toTime,
      dateRange: [fromTime, toTime],
      dateConfig: {
        mode: "range",
        wrap: true,
        dateFormat: "Y-m-d H:i",
        maxDate: new Date()
      },
      usernameFilterEnabled: false,
      usernameFilter: null,
      applicationNameFilterEnabled: false,
      applicationNameFilter: null,
      hostnameFilterEnabled: false,
      hostnameFilter: null,
      appInterfaces: null,
      computeResourceNames: null
    };
  },
  created() {
    this.loadStatistics();
    this.loadApplicationInterfaces();
    this.loadComputeResources();
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
    },
    selectedExperimentSummaries() {
      if (
        this.selectedExperimentSummariesKey &&
        this.experimentStatistics &&
        this.selectedExperimentSummariesKey in this.experimentStatistics
      ) {
        return this.experimentStatistics[this.selectedExperimentSummariesKey];
      } else {
        return [];
      }
    },
    applicationNameOptions() {
      if (this.appInterfaces) {
        const options = this.appInterfaces.map(appInterface => {
          return {
            value: appInterface.applicationInterfaceId,
            text: appInterface.applicationName
          };
        });
        return utils.StringUtils.sortIgnoreCase(options, o => o.text);
      } else {
        return [];
      }
    },
    hostnameOptions() {
      if (this.computeResourceNames) {
        const options = this.computeResourceNames.map(name => {
          return {
            value: name.host_id,
            text: name.host
          };
        });
        return utils.StringUtils.sortIgnoreCase(options, o => o.text);
      } else {
        return [];
      }
    }
  },
  methods: {
    dateRangeChanged(selectedDates) {
      [this.fromTime, this.toTime] = selectedDates;
      if (this.fromTime && this.toTime) {
        this.loadStatistics();
      }
    },
    loadApplicationInterfaces() {
      return services.ApplicationInterfaceService.list().then(
        appInterfaces => (this.appInterfaces = appInterfaces)
      );
    },
    loadComputeResources() {
      return services.ComputeResourceService.namesList().then(
        names => (this.computeResourceNames = names)
      );
    },
    loadStatistics() {
      const requestData = {
        fromTime: this.fromTime.toJSON(),
        toTime: this.toTime.toJSON()
      };
      if (this.usernameFilterEnabled && this.usernameFilter) {
        requestData["userName"] = this.usernameFilter;
      }
      if (this.applicationNameFilterEnabled && this.applicationNameFilter) {
        requestData["applicationName"] = this.applicationNameFilter;
      }
      if (this.hostnameFilterEnabled && this.hostnameFilter) {
        requestData["resourceHostName"] = this.hostnameFilter;
      }
      services.ExperimentStatisticsService.get(requestData).then(
        stats => (this.experimentStatistics = stats)
      );
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
        moment(this.fromTime).format("YYYY-MM-DD HH:mm"),
        moment(this.toTime).format("YYYY-MM-DD HH:mm")
      ];
    },
    daysAgo(days) {
      return new Date(Date.now() - days * 24 * 60 * 60 * 1000);
    },
    removeUsernameFilter() {
      this.usernameFilter = null;
      this.usernameFilterEnabled = false;
      this.loadStatistics();
    },
    removeApplicationNameFilter() {
      this.applicationNameFilter = null;
      this.applicationNameFilterEnabled = false;
      this.loadStatistics();
    },
    removeHostnameFilter() {
      this.hostnameFilter = null;
      this.hostnameFilterEnabled = false;
      this.loadStatistics();
    }
  }
};
</script>
