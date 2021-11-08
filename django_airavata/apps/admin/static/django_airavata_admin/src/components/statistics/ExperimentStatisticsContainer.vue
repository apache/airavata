<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Experiment Statistics</h1>
      </div>
    </div>
    <b-card header="Load experiment details by experiment id">
      <b-form-group>
        <b-input-group>
          <b-form-input
            v-model="experimentId"
            placeholder="Experiment ID"
            @keydown.native.enter="
              experimentId && showExperimentDetails(experimentId)
            "
          />
          <b-input-group-append>
            <b-button
              :disabled="!experimentId"
              @click="showExperimentDetails(experimentId)"
              variant="primary"
              >Load</b-button
            >
          </b-input-group-append>
        </b-input-group>
      </b-form-group>
    </b-card>
    <b-card no-body>
      <b-tabs card v-model="activeTabIndex" ref="tabs">
        <b-tab :title="selectedExperimentsTabTitle">
          <div class="row">
            <div class="col">
              <b-card header="Filter Options">
                <b-input-group class="w-100 mb-2">
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
                    <b-button
                      @click="getPast24Hours"
                      variant="outline-secondary"
                      >Past 24 Hours</b-button
                    >
                    <b-button @click="getPastWeek" variant="outline-secondary"
                      >Past Week</b-button
                    >
                  </b-input-group-append>
                </b-input-group>
                <b-dropdown text="Add Filters" class="mb-2">
                  <b-dropdown-item
                    v-if="!usernameFilterEnabled"
                    @click="usernameFilterEnabled = true"
                    >Username</b-dropdown-item
                  >
                  <b-dropdown-item
                    v-if="!applicationNameFilterEnabled"
                    @click="applicationNameFilterEnabled = true"
                    >Application Name</b-dropdown-item
                  >
                  <b-dropdown-item
                    v-if="!hostnameFilterEnabled"
                    @click="hostnameFilterEnabled = true"
                    >Hostname</b-dropdown-item
                  >
                </b-dropdown>
                <b-input-group v-if="usernameFilterEnabled" class="mb-2">
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
                <b-input-group v-if="applicationNameFilterEnabled" class="mb-2">
                  <b-form-select
                    v-model="applicationNameFilter"
                    :options="applicationNameOptions"
                    @input="loadStatistics"
                  >
                    <template slot="first">
                      <option :value="null" disabled>
                        Select an application to filter on
                      </option>
                    </template>
                  </b-form-select>
                  <b-input-group-append>
                    <b-button @click="removeApplicationNameFilter">
                      <i class="fa fa-times"></i>
                      <span class="sr-only"
                        >Remove application name filter</span
                      >
                    </b-button>
                  </b-input-group-append>
                </b-input-group>
                <b-input-group v-if="hostnameFilterEnabled" class="mb-2">
                  <b-form-select
                    v-model="hostnameFilter"
                    :options="hostnameOptions"
                    @input="loadStatistics"
                  >
                    <template slot="first">
                      <option :value="null" disabled>
                        Select compute resource to filter on
                      </option>
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
                      >Get Statistics</b-button
                    >
                  </div>
                </template>
              </b-card>
            </div>
          </div>
          <div class="row">
            <div class="col">
              <h2 class="h5 mb-4">
                Experiment Statistics from {{ fromTimeDisplay }} to
                {{ toTimeDisplay }}
              </h2>
            </div>
          </div>
          <div class="row">
            <div class="col-xl-2 col-md-4">
              <experiment-statistics-card
                bg-variant="primary"
                header-text-variant="white"
                :count="experimentStatistics.allExperimentCount || 0"
                title="Total Experiments"
                @click="selectExperiments('allExperiments')"
              >
                <span slot="link-text">All</span>
              </experiment-statistics-card>
            </div>
            <div class="col-xl-2 col-md-4">
              <experiment-statistics-card
                bg-variant="light"
                :count="experimentStatistics.createdExperimentCount || 0"
                :states="createdStates"
                title="Created Experiments"
                @click="selectExperiments('createdExperiments')"
              >
              </experiment-statistics-card>
            </div>
            <div class="col-xl-2 col-md-4">
              <experiment-statistics-card
                bg-variant="light"
                header-text-variant="success"
                :count="experimentStatistics.runningExperimentCount || 0"
                :states="runningStates"
                title="Running Experiments"
                @click="selectExperiments('runningExperiments')"
              >
              </experiment-statistics-card>
            </div>
            <div class="col-xl-2 col-md-4">
              <experiment-statistics-card
                bg-variant="success"
                header-text-variant="white"
                link-variant="success"
                :count="experimentStatistics.completedExperimentCount || 0"
                :states="completedStates"
                title="Completed Experiments"
                @click="selectExperiments('completedExperiments')"
              >
              </experiment-statistics-card>
            </div>
            <div class="col-xl-2 col-md-4">
              <experiment-statistics-card
                bg-variant="warning"
                header-text-variant="white"
                link-variant="warning"
                :count="experimentStatistics.cancelledExperimentCount || 0"
                :states="canceledStates"
                title="Cancelled Experiments"
                @click="selectExperiments('cancelledExperiments')"
              >
              </experiment-statistics-card>
            </div>
            <div class="col-xl-2 col-md-4">
              <experiment-statistics-card
                bg-variant="danger"
                header-text-variant="white"
                link-variant="danger"
                :count="experimentStatistics.failedExperimentCount || 0"
                :states="failedStates"
                title="Failed Experiments"
                @click="selectExperiments('failedExperiments')"
              >
              </experiment-statistics-card>
            </div>
          </div>
          <div class="row" v-if="items.length > 0">
            <div class="col">
              <b-card>
                <b-table :fields="fields" :items="items">
                  <template slot="cell(executionId)" slot-scope="data">
                    <application-name :application-interface-id="data.value" />
                  </template>
                  <template slot="cell(resourceHostId)" slot-scope="data">
                    <compute-resource-name :compute-resource-id="data.value" />
                  </template>
                  <template slot="cell(creationTime)" slot-scope="data">
                    <human-date :date="data.value" />
                  </template>
                  <template slot="cell(experimentStatus)" slot-scope="data">
                    <experiment-status-badge :status-name="data.value.name" />
                  </template>
                  <template slot="cell(actions)" slot-scope="data">
                    <b-link
                      @click="showExperimentDetails(data.item.experimentId)"
                    >
                      View Details
                      <i class="far fa-chart-bar" aria-hidden="true"></i>
                    </b-link>
                  </template>
                </b-table>
              </b-card>
              <pager
                v-if="experimentStatistics.allExperimentCount > 0"
                :paginator="experimentStatisticsPaginator"
                @next="experimentStatisticsPaginator.next()"
                @previous="experimentStatisticsPaginator.previous()"
              ></pager>
            </div>
          </div>
        </b-tab>
        <b-tab
          v-for="experimentDetail in experimentDetails"
          :key="experimentDetail.experimentId"
        >
          <template slot="title">
            {{ experimentDetail.experimentName }}
            <b-link
              @click="removeExperimentDetails(experimentDetail.experimentId)"
              class="text-secondary"
            >
              <i class="fas fa-times"></i>
              <span class="sr-only">Close experiment tab</span>
            </b-link>
          </template>
          <experiment-details-view :experiment="experimentDetail" />
        </b-tab>
      </b-tabs>
    </b-card>
  </div>
</template>
<script>
import { models, services, utils } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
import ExperimentStatisticsCard from "./ExperimentStatisticsCard";
import ExperimentDetailsView from "./ExperimentDetailsView";

import moment from "moment";

export default {
  name: "experiment-statistics-container",
  data() {
    //fp_incr sets the time of the date to midnight.
    //Calculating from today midnight to tomorrow midnight.
    const fromTime = new Date().fp_incr(0);
    const toTime = new Date().fp_incr(1);
    return {
      experimentStatisticsPaginator: null,
      selectedExperimentSummariesKey: null,
      fromTime: fromTime,
      toTime: toTime,
      dateRange: [fromTime, toTime],
      dateConfig: {
        mode: "range",
        wrap: true,
        dateFormat: "Y-m-d",
        maxDate: new Date().fp_incr(1),
      },
      usernameFilterEnabled: false,
      usernameFilter: null,
      applicationNameFilterEnabled: false,
      applicationNameFilter: null,
      hostnameFilterEnabled: false,
      hostnameFilter: null,
      appInterfaces: null,
      computeResourceNames: null,
      experimentDetails: [],
      experimentId: null,
      activeTabIndex: 0,
    };
  },
  created() {
    this.loadStatistics();
    this.loadApplicationInterfaces();
    this.loadComputeResources();
  },
  components: {
    ExperimentDetailsView,
    ExperimentStatisticsCard,
    "application-name": components.ApplicationName,
    "compute-resource-name": components.ComputeResourceName,
    "human-date": components.HumanDate,
    "experiment-status-badge": components.ExperimentStatusBadge,
    pager: components.Pager,
  },
  computed: {
    experimentStatistics() {
      return this.experimentStatisticsPaginator
        ? this.experimentStatisticsPaginator.results
        : {};
    },
    createdStates() {
      // TODO: moved to ExperimentStatistics model
      return [models.ExperimentState.CREATED, models.ExperimentState.VALIDATED];
    },
    runningStates() {
      return [
        models.ExperimentState.SCHEDULED,
        models.ExperimentState.LAUNCHED,
        models.ExperimentState.EXECUTING,
      ];
    },
    completedStates() {
      return [models.ExperimentState.COMPLETED];
    },
    canceledStates() {
      return [
        models.ExperimentState.CANCELING,
        models.ExperimentState.CANCELED,
      ];
    },
    failedStates() {
      return [models.ExperimentState.FAILED];
    },
    fields() {
      return [
        {
          key: "name",
          label: "Name",
        },
        {
          key: "userName",
          label: "Owner",
        },
        {
          key: "executionId",
          label: "Application",
        },
        {
          key: "resourceHostId",
          label: "Resource",
        },
        {
          key: "creationTime",
          label: "Creation Time",
        },
        {
          key: "experimentStatus",
          label: "Status",
        },
        {
          key: "actions",
          label: "Actions",
        },
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
        const options = this.appInterfaces.map((appInterface) => {
          return {
            value: appInterface.applicationInterfaceId,
            text: appInterface.applicationName,
          };
        });
        return utils.StringUtils.sortIgnoreCase(options, (o) => o.text);
      } else {
        return [];
      }
    },
    hostnameOptions() {
      if (this.computeResourceNames) {
        const options = this.computeResourceNames.map((name) => {
          return {
            value: name.host_id,
            text: name.host,
          };
        });
        return utils.StringUtils.sortIgnoreCase(options, (o) => o.text);
      } else {
        return [];
      }
    },
    selectedExperimentsTabTitle() {
      if (this.selectedExperimentSummariesKey === "allExperiments") {
        return "All Experiments";
      } else if (this.selectedExperimentSummariesKey === "createdExperiments") {
        return "Created Experiments";
      } else if (this.selectedExperimentSummariesKey === "runningExperiments") {
        return "Running Experiments";
      } else if (
        this.selectedExperimentSummariesKey === "completedExperiments"
      ) {
        return "Completed Experiments";
      } else if (
        this.selectedExperimentSummariesKey === "cancelledExperiments"
      ) {
        return "Cancelled Experiments";
      } else if (this.selectedExperimentSummariesKey === "failedExperiments") {
        return "Failed Experiments";
      } else {
        return "Experiments";
      }
    },
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
        (appInterfaces) => (this.appInterfaces = appInterfaces)
      );
    },
    loadComputeResources() {
      return services.ComputeResourceService.namesList().then(
        (names) => (this.computeResourceNames = names)
      );
    },
    loadStatistics() {
      const requestData = {
        fromTime: this.fromTime.toJSON(),
        toTime: this.toTime.toJSON(),
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
      return services.ExperimentStatisticsService.get(requestData).then(
        (stats) => {
          this.experimentStatisticsPaginator = stats;
        }
      );
    },
    getPast24Hours() {
      this.fromTime = new Date().fp_incr(0);
      //this.fromTime = new Date(this.fromTime.setHours(0,0,0));
      this.toTime = new Date().fp_incr(1);
      this.updateDateRange();
    },
    getPastWeek() {
      this.fromTime = new Date().fp_incr(-7);
      this.toTime = new Date().fp_incr(1);
      this.updateDateRange();
    },
    updateDateRange() {
      this.dateRange = [
        moment(this.fromTime).format("YYYY-MM-DD"),
        moment(this.toTime).format("YYYY-MM-DD"),
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
    },
    showExperimentDetails(experimentId) {
      const expDetailsIndex = this.getExperimentDetailsIndex(experimentId);
      if (expDetailsIndex >= 0) {
        this.selectExperimentDetailsTab(experimentId);
      } else {
        // TODO: maybe don't need to load the experiment first since ExperimentDetailsView will load FullExperiment?
        services.ExperimentService.retrieve({
          lookup: experimentId,
        }).then((exp) => {
          this.experimentDetails.push(exp);
          this.selectExperimentDetailsTab(experimentId);
          this.scrollTabsIntoView();
        });
      }
    },
    selectExperimentDetailsTab(experimentId) {
      const expDetailsIndex = this.getExperimentDetailsIndex(experimentId);
      // Note: running this in $nextTick doesn't work, but setTimeout does
      // (see also https://github.com/bootstrap-vue/bootstrap-vue/issues/1378#issuecomment-345689470)
      setTimeout(() => {
        // Add 1 to the index because the first tab has the overall statistics
        this.activeTabIndex = expDetailsIndex + 1;
      }, 1);
    },
    getExperimentDetailsIndex(experimentId) {
      return this.experimentDetails.findIndex(
        (e) => e.experimentId === experimentId
      );
    },
    removeExperimentDetails(experimentId) {
      const index = this.getExperimentDetailsIndex(experimentId);
      this.experimentDetails.splice(index, 1);
    },
    scrollTabsIntoView() {
      this.$refs.tabs.$el.scrollIntoView({ behavior: "smooth" });
    },
    selectExperiments(experimentSummariesKey) {
      if (
        this.experimentStatisticsPaginator &&
        this.experimentStatisticsPaginator.offset > 0
      ) {
        this.loadStatistics();
      }
      this.selectedExperimentSummariesKey = experimentSummariesKey;
    },
  },
};
</script>
