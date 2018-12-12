<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">
          <slot name="title">Experiment Summary</slot>
        </h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card border-default">
          <div class="card-body">
            <table class="table">
              <tbody>
                <tr>
                  <th scope="row">Name</th>
                  <td>
                    <span :title="experiment.experimentId">{{ experiment.experimentName }}</span>
                  </td>
                </tr>
                <tr>
                  <th scope="row">Description</th>
                  <td>{{ experiment.description }}</td>
                </tr>
                <tr>
                  <th scope="row">Project</th>
                  <td v-if="localFullExperiment.project">{{ localFullExperiment.projectName }}</td>
                  <td v-else>
                    <em>You don't have access to this project.</em>
                  </td>
                </tr>
                <tr>
                  <th scope="row">Outputs</th>
                  <td>
                    <template v-for="output in localFullExperiment.outputDataProducts">
                      <span v-if="output.downloadURL" :key="output.productUri">
                        <a :href="output.downloadURL">
                          <i class="fa fa-download"></i>
                          {{ output.filename }}
                        </a>
                      </span>
                      <span v-else :key="output.productUri">{{ output.filename }}</span>
                    </template>
                  </td>
                </tr>
                <!-- Going to leave this out for now -->
                <!-- <tr>
                                    <th scope="row">Storage Directory</th>
                                    <td></td>
                                </tr> -->
                <tr>
                  <th scope="row">Owner</th>
                  <td>{{ experiment.userName }}</td>
                </tr>
                <tr>
                  <th scope="row">Application</th>
                  <td>{{ localFullExperiment.applicationName }}</td>
                </tr>
                <tr>
                  <th scope="row">Compute Resource</th>
                  <td>{{ localFullExperiment.computeHostName }}</td>
                </tr>
                <tr>
                  <th scope="row">Experiment Status</th>
                  <td>
                    <template v-if="localFullExperiment.experiment.isProgressing">
                      <i class="fa fa-refresh fa-spin"></i>
                      <span class="sr-only">Progressing...</span>
                    </template>
                    {{ localFullExperiment.experimentStatusName }}
                  </td>
                </tr>
                <tr v-if="localFullExperiment.jobDetails && localFullExperiment.jobDetails.length > 0">
                  <th scope="row">Job</th>
                  <td>
                    <table class="table">
                      <thead>
                        <th>Name</th>
                        <th>ID</th>
                        <th>Status</th>
                        <th>Creation Time</th>
                      </thead>
                      <tr v-for="(jobDetail, index) in localFullExperiment.jobDetails" :key="jobDetail.jobId">
                        <td>{{ jobDetail.jobName }}</td>
                        <td>{{ jobDetail.jobId }}</td>
                        <td>{{ jobDetail.jobStatusStateName }}</td>
                        <td>
                          <span :title="jobDetail.creationTime.toString()">{{ jobCreationTimes[index] }}</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <!--  TODO: leave this out for now -->
                <!-- <tr>
                                    <th scope="row">Notification List</th>
                                    <td>{{ experiment.emailAddresses
                                            ? experiment.emailAddresses.join(", ")
                                            : '' }}</td>
                                </tr> -->
                <tr>
                  <th scope="row">Creation Time</th>
                  <td>
                    <span :title="experiment.creationTime.toString()">{{ creationTime }}</span>
                  </td>
                </tr>
                <tr>
                  <th scope="row">Last Modified Time</th>
                  <td>
                    <span :title="localFullExperiment.experimentStatus.timeOfStateChange.toString()">{{ lastModifiedTime }}</span>
                  </td>
                </tr>
                <tr>
                  <th scope="row">Wall Time Limit</th>
                  <td>{{ experiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit }} minutes</td>
                </tr>
                <tr>
                  <th scope="row">CPU Count</th>
                  <td>{{ experiment.userConfigurationData.computationalResourceScheduling.totalCPUCount }}</td>
                </tr>
                <tr>
                  <th scope="row">Node Count</th>
                  <td>{{ experiment.userConfigurationData.computationalResourceScheduling.nodeCount }}</td>
                </tr>
                <tr>
                  <th scope="row">Queue</th>
                  <td>{{ experiment.userConfigurationData.computationalResourceScheduling.queueName }}</td>
                </tr>
                <tr>
                  <th scope="row">Inputs</th>
                  <td>
                    <template v-for="input in localFullExperiment.inputDataProducts">
                      <span v-if="input.downloadURL" :key="input.productUri">
                        <a :href="input.downloadURL">
                          <i class="fa fa-download"></i>
                          {{ input.filename }}
                        </a>
                      </span>
                      <span v-else :key="input.productUri">{{ input.filename }}</span>
                    </template>
                  </td>
                </tr>
                <tr>
                  <!-- TODO -->
                  <th scope="row">Errors</th>
                  <td></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";

import moment from "moment";

export default {
  name: "experiment-summary",
  props: {
    fullExperiment: {
      type: models.FullExperiment,
      required: true
    },
    launching: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      localFullExperiment: this.fullExperiment.clone()
    };
  },
  components: {},
  computed: {
    creationTime: function() {
      return moment(this.localFullExperiment.experiment.creationTime).fromNow();
    },
    lastModifiedTime: function() {
      return moment(
        this.localFullExperiment.experimentStatus.timeOfStateChange
      ).fromNow();
    },
    experiment: function() {
      return this.localFullExperiment.experiment;
    },
    jobCreationTimes: function() {
      return this.localFullExperiment.jobDetails.map(jobDetail =>
        moment(jobDetail.creationTime).fromNow()
      );
    }
  },
  methods: {
    loadExperiment: function() {
      return services.FullExperimentService.get(
        this.localFullExperiment.experiment.experimentId
      ).then(exp => (this.localFullExperiment = exp));
    },
    initPollingExperiment: function() {
      var pollExperiment = function() {
        if (
          (this.launching &&
            !this.localFullExperiment.experiment.hasLaunched) ||
          this.localFullExperiment.experiment.isProgressing
        ) {
          this.loadExperiment().then(exp => {
            setTimeout(pollExperiment.bind(this), 3000);
          });
        }
      }.bind(this);
      setTimeout(pollExperiment, 3000);
    }
  },
  watch: {},
  mounted: function() {
    this.initPollingExperiment();
  }
};
</script>

<style>
</style>
