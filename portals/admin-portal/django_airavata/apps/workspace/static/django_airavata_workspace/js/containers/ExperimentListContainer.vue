<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Browse Experiments</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-input-group class="w-100 mb-2">
              <b-form-input
                v-if="defaultOptionSelected"
                v-model="search"
                placeholder="Search Experiments"
                @keydown.native.enter="searchExperiments"
              />
              <b-form-select
                v-if="applicationSelected"
                v-model="applicationSelect"
                :options="applicationNameOptions"
              >
                <template slot="first">
                  <option :value="null" disabled>
                    Select an application to search by
                  </option>
                </template>
              </b-form-select>
              <b-form-select
                v-if="projectSelected"
                v-model="projectSelect"
                :options="projectNameOptions"
              >
                <template slot="first">
                  <option :value="null" disabled>
                    Select a project to search by
                  </option>
                </template>
              </b-form-select>
              <b-form-select
                v-model="experimentAttributeSelect"
                @input="checkSearchOptions"
              >
                <template slot="first">
                  <option :value="null" disabled>
                    Select an attribute to search by
                  </option>
                </template>
                <option value="USER_NAME">User Name</option>
                <option value="EXPERIMENT_NAME">Experiment Name</option>
                <option value="EXPERIMENT_DESC">Experiment Description</option>
                <option value="APPLICATION_ID">Application</option>
                <option value="PROJECT_ID">Project</option>
                <option value="JOB_ID">Job Id</option>
              </b-form-select>
              <b-form-select v-model="experimentStatusSelect">
                <template slot="first">
                  <option :value="null" disabled>
                    Select an experiment status to filter by
                  </option>
                </template>
                <option value="ALL">ALL</option>
                <option value="CREATED">Created</option>
                <option value="VALIDATED">Validated</option>
                <option value="SCHEDULED">Scheduled</option>
                <option value="LAUNCHED">Launched</option>
                <option value="EXECUTING">Executing</option>
                <option value="CANCELED">Canceled</option>
                <option value="COMPLETED">Completed</option>
                <option value="FAILED">Failed</option>
              </b-form-select>
              <b-input-group-append>
                <b-button @click="resetSearch">Reset</b-button>
                <b-button variant="primary" @click="searchExperiments"
                  >Search</b-button
                >
              </b-input-group-append>
            </b-input-group>
            <b-input-group class="w-100 mb-2">
              <b-input-group-prepend is-text>
                <i class="fa fa-calendar-week" aria-hidden="true"></i>
              </b-input-group-prepend>
              <flat-pickr
                v-model="dateSelect"
                :config="dateConfig"
                placeholder="Select a date range to filter by"
                @on-change="dateRangeChanged"
                class="form-control"
              />
            </b-input-group>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <table class="table table-hover">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Application</th>
                  <th>User</th>
                  <th>Creation Time</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="experiment in experiments"
                  :key="experiment.experimentId"
                >
                  <td>
                    <b-link :href="viewLink(experiment)">{{
                      experiment.name
                    }}</b-link>
                  </td>
                  <td v-if="applicationName(experiment)">
                    {{ applicationName(experiment) }}
                  </td>
                  <td v-else class="font-italic text-muted">N/A</td>
                  <td>{{ experiment.userName }}</td>
                  <td>
                    <span :title="experiment.creationTime">{{
                      fromNow(experiment.creationTime)
                    }}</span>
                  </td>
                  <td>
                    <experiment-status-badge
                      :statusName="experiment.experimentStatus.name"
                    />
                  </td>
                  <td>
                    <!-- if we can't load the application for the experiment
                    (for example, if it was deleted), then user can't edit or
                    clone experiment -->
                    <span v-if="applicationName(experiment)">
                      <b-link
                        v-if="
                          experiment.isEditable && applicationName(experiment)
                        "
                        :href="editLink(experiment)"
                        class="action-link"
                        >Edit
                        <i class="fa fa-edit" aria-hidden="true"></i>
                      </b-link>
                      <b-link
                        v-else
                        @click="clone(experiment)"
                        class="action-link"
                        >Clone
                        <i class="fa fa-copy" aria-hidden="true"></i>
                      </b-link>
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
            <pager
              v-bind:paginator="experimentsPaginator"
              v-on:next="nextExperiments"
              v-on:previous="previousExperiments"
            ></pager>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { errors, models, services, utils } from "django-airavata-api";
import { components as comps } from "django-airavata-common-ui";

import moment from "moment";
import urls from "../utils/urls";

export default {
  props: ["initialExperimentsData"],
  name: "experiment-list-container",
  data() {
    return {
      experimentsPaginator: null,
      applicationInterfaces: {},
      search: null,
      applicationSelect: null,
      projectSelect: null,
      dateSelect: null,
      experimentAttributeSelect: null,
      experimentStatusSelect: null,
      appInterfaces: null,
      projectInterfaces: null,
      fromDate: null,
      toDate: null,
      applicationSelected: false,
      projectSelected: false,
      defaultOptionSelected: true,
      dateConfig: {
        mode: "range",
        wrap: true,
        dateFormat: "Y-m-d",
        maxDate: new Date().fp_incr(1),
      },
    };
  },
  components: {
    pager: comps.Pager,
    "experiment-status-badge": comps.ExperimentStatusBadge,
  },
  methods: {
    searchExperiments: function () {
      this.experimentsPaginator = null;
      this.reloadExperiments();
    },
    resetSearch: function () {
      this.experimentsPaginator = null;
      this.search = null;
      this.experimentAttributeSelect = null;
      this.experimentStatusSelect = null;
      this.applicationSelect = null;
      this.projectSelect = null;
      this.dateSelect = null;
      this.toDate = null;
      this.fromDate = null;
      this.checkSearchOptions();
      this.reloadExperiments();
    },
    reloadExperiments: function () {
      const searchParams = {};
      if (this.experimentAttributeSelect) {
        if (
          this.experimentAttributeSelect == "APPLICATION_ID" &&
          this.applicationSelect
        ) {
          searchParams["APPLICATION_ID"] = this.applicationSelect;
        } else if (
          this.experimentAttributeSelect == "PROJECT_ID" &&
          this.projectSelect
        ) {
          searchParams["PROJECT_ID"] = this.projectSelect;
        } else if (this.search) {
          searchParams[this.experimentAttributeSelect] = this.search;
        }
      }
      if (this.experimentStatusSelect) {
        if (this.experimentStatusSelect != "ALL") {
          searchParams["STATUS"] = this.experimentStatusSelect;
        }
      }
      if (this.fromDate && this.toDate) {
        searchParams["FROM_DATE"] = this.fromDate.getTime();
        searchParams["TO_DATE"] = this.toDate.getTime();
      }

      services.ExperimentSearchService.list(searchParams).then(
        (result) => (this.experimentsPaginator = result)
      );
    },
    checkSearchOptions: function () {
      this.applicationSelected = false;
      this.projectSelected = false;
      this.defaultOptionSelected = false;
      if (this.experimentAttributeSelect == "APPLICATION_ID") {
        this.applicationSelected = true;
      } else if (this.experimentAttributeSelect == "PROJECT_ID") {
        this.projectSelected = true;
      } else {
        this.defaultOptionSelected = true;
      }
    },
    loadApplicationInterfaces: function () {
      return services.ApplicationInterfaceService.list().then(
        (appInterfaces) => (this.appInterfaces = appInterfaces)
      );
    },
    loadProjectInterfaces: function () {
      return services.ProjectService.listAll().then(
        (projectInterfaces) => (this.projectInterfaces = projectInterfaces)
      );
    },
    dateRangeChanged: function (selectedDates) {
      [this.fromDate, this.toDate] = selectedDates;
      if (this.fromDate && this.toDate) {
        this.reloadExperiments();
      }
    },
    nextExperiments: function () {
      this.experimentsPaginator.next();
    },
    previousExperiments: function () {
      this.experimentsPaginator.previous();
    },
    fromNow: function (date) {
      return moment(date).fromNow();
    },
    editLink: function (experiment) {
      return urls.editExperiment(experiment);
    },
    viewLink: function (experiment) {
      return urls.viewExperiment(experiment);
    },
    applicationName: function (experiment) {
      if (experiment.executionId in this.applicationInterfaces) {
        if (
          this.applicationInterfaces[experiment.executionId] instanceof
          models.ApplicationInterfaceDefinition
        ) {
          return this.applicationInterfaces[experiment.executionId]
            .applicationName;
        } else if (
          this.applicationInterfaces[experiment.executionId] === null
        ) {
          return null;
        }
      } else {
        const request = services.ApplicationInterfaceService.retrieve(
          {
            lookup: experiment.executionId,
          },
          {
            ignoreErrors: true,
          }
        )
          .then((result) => {
            this.$set(
              this.applicationInterfaces,
              experiment.executionId,
              result
            );
          })
          .catch((error) => {
            if (errors.ErrorUtils.isNotFoundError(error)) {
              this.$set(
                this.applicationInterfaces,
                experiment.executionId,
                null
              );
            } else {
              throw error;
            }
          })
          .catch(utils.FetchUtils.reportError);
        this.$set(this.applicationInterfaces, experiment.executionId, request);
      }
      return "...";
    },
    clone(experiment) {
      services.ExperimentService.clone({
        lookup: experiment.experimentId,
      }).then((clonedExperiment) => {
        urls.navigateToEditExperiment(clonedExperiment);
      });
    },
  },
  computed: {
    experiments: function () {
      return this.experimentsPaginator
        ? this.experimentsPaginator.results
        : null;
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
    projectNameOptions() {
      if (this.projectInterfaces) {
        const options = this.projectInterfaces.map((projectInterface) => {
          return {
            value: projectInterface.projectID,
            text: projectInterface.name,
          };
        });
        return utils.StringUtils.sortIgnoreCase(options, (o) => o.text);
      } else {
        return [];
      }
    },
  },
  beforeMount: function () {
    this.loadApplicationInterfaces();
    this.loadProjectInterfaces();
    services.ExperimentSearchService.list({
      initialData: this.initialExperimentsData,
    }).then((result) => (this.experimentsPaginator = result));
  },
};
</script>

<style></style>
