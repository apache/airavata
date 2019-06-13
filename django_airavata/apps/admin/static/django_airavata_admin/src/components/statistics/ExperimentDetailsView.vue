<template>
  <div>
    <table
      class="table"
      v-if="fullExperiment"
    >
      <tbody>
        <tr>
          <th scope="row">Name</th>
          <td>
            <div :title="experiment.experimentId">{{ experiment.experimentName }}</div>
            <small class="text-muted">
              ID: {{ experiment.experimentId }}
              (<clipboard-copy-link
                :text="experiment.experimentId"
                :link-classes="['text-reset']"
              >
                copy
                <span slot="icon"></span>
                <span slot="tooltip">Copied ID!</span>
              </clipboard-copy-link>)
            </small>
          </td>
        </tr>
        <tr>
          <th scope="row">Description</th>
          <td>{{ experiment.description }}</td>
        </tr>
        <tr>
          <th scope="row">Project</th>
          <td v-if="fullExperiment.project">{{ fullExperiment.projectName }}</td>
          <td v-else>
            <em>You don't have access to this project.</em>
          </td>
        </tr>
        <tr>
          <th scope="row">Owner</th>
          <td>{{ experiment.userName }}</td>
        </tr>
        <tr>
          <th scope="row">Application</th>
          <td v-if="fullExperiment.applicationName">{{ fullExperiment.applicationName }}</td>
          <td
            v-else
            class="font-italic text-muted"
          >Unable to load interface {{ fullExperiment.experiment.executionId }}</td>
        </tr>
        <tr>
          <th scope="row">Compute Resource</th>
          <td v-if="fullExperiment.computeHostName">{{ fullExperiment.computeHostName }}</td>
          <td
            v-else
            class="font-italic text-muted"
          >Unable to load compute resource {{ fullExperiment.resourceHostId }}</td>
        </tr>
        <tr>
          <th scope="row">Experiment Status</th>
          <td>
            <template v-if="fullExperiment.experiment.isProgressing">
              <i class="fa fa-sync-alt fa-spin"></i>
              <span class="sr-only">Progressing...</span>
            </template>
            {{ fullExperiment.experimentStatusName }}
          </td>
        </tr>
        <tr v-if="fullExperiment.jobDetails && fullExperiment.jobDetails.length > 0">
          <th scope="row">Job</th>
          <td>
            <table class="table">
              <thead>
          <th>Name</th>
          <th>ID</th>
          <th>Status</th>
          <th>Creation Time</th>
          </thead>
        <tr
          v-for="(jobDetail, index) in fullExperiment.jobDetails"
          :key="jobDetail.jobId"
        >
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
    <tr>
      <th scope="row">Notification List</th>
      <td>{{ experiment.emailAddresses
        ? experiment.emailAddresses.join(", ")
        : '' }}</td>
    </tr>
    <tr v-if="fullExperiment.jobDetails && fullExperiment.jobDetails.length > 0">
      <th scope="row">Working Dir</th>
      <td>
        <div
          v-for="jobDetail in fullExperiment.jobDetails"
          :key="jobDetail.jobId"
        >
          {{ jobDetail.jobName }}: {{ jobDetail.workingDir }}
        </div>
      </td>
    </tr>
    <tr>
      <th scope="row">Creation Time</th>
      <td>
        <span :title="experiment.creationTime.toString()">{{ creationTime }}</span>
      </td>
    </tr>
    <tr>
      <th scope="row">Last Modified Time</th>
      <td>
        <span :title="fullExperiment.experimentStatus.timeOfStateChange.toString()">{{ lastModifiedTime }}</span>
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
        <ul>
          <li
            v-for="input in experiment.experimentInputs"
            :key="input.name"
          >
            {{ input.name }}:
            <template v-if="input.type.isSimpleValueType">
              <span class="text-break">{{ input.value }}</span>
            </template>
            <data-product-viewer
              v-for="dp in inputDataProducts[input.name]"
              v-else-if="input.type.isFileValueType"
              :data-product="dp"
              :input-file="true"
              class="data-product"
              :key="dp.productUri"
            />
          </li>
        </ul>
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
</template>

<script>
import { models, services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";

import moment from "moment";

export default {
  name: "experiment-details-view",
  props: {
    experiment: {
      type: models.Experiment,
      required: true
    }
  },
  components: {
    "clipboard-copy-link": components.ClipboardCopyLink,
    "data-product-viewer": components.DataProductViewer
  },
  data() {
    return {
      fullExperiment: null
    };
  },
  computed: {
    inputDataProducts() {
      const result = {};
      if (this.fullExperiment && this.fullExperiment.inputDataProducts) {
        this.fullExperiment.experiment.experimentInputs.forEach(input => {
          result[input.name] = this.getDataProducts(
            input,
            this.fullExperiment.inputDataProducts
          );
        });
      }
      return result;
    },
    outputDataProducts() {
      const result = {};
      if (this.fullExperiment && this.fullExperiment.outputDataProducts) {
        this.fullExperiment.experiment.experimentOutputs.forEach(output => {
          result[output.name] = this.getDataProducts(
            output,
            this.fullExperiment.outputDataProducts
          );
        });
      }
      return result;
    },
    creationTime: function() {
      return moment(this.fullExperiment.experiment.creationTime).fromNow();
    },
    lastModifiedTime: function() {
      return moment(
        this.fullExperiment.experimentStatus.timeOfStateChange
      ).fromNow();
    },
    jobCreationTimes: function() {
      return this.fullExperiment.jobDetails.map(jobDetail =>
        moment(jobDetail.creationTime).fromNow()
      );
    }
  },
  created() {
    services.FullExperimentService.retrieve({
      lookup: this.experiment.experimentId
    }).then(fullExperiment => (this.fullExperiment = fullExperiment));
  },
  methods: {
    getDataProducts(io, collection) {
      if (!io.value || !collection) {
        return [];
      }
      let dataProducts = null;
      if (io.type === models.DataType.URI_COLLECTION) {
        const dataProductURIs = io.value.split(",");
        dataProducts = dataProductURIs.map(uri =>
          collection.find(dp => dp.productUri === uri)
        );
      } else {
        const dataProductURI = io.value;
        dataProducts = collection.filter(
          dp => dp.productUri === dataProductURI
        );
      }
      return dataProducts ? dataProducts.filter(dp => (dp ? true : false)) : [];
    }
  }
};
</script>

