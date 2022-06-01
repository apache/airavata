<template>
  <div>
    <table class="table" v-if="fullExperiment">
      <tbody>
        <tr>
          <th scope="row">Name</th>
          <td>
            <div :title="experiment.experimentId">
              {{ experiment.experimentName }}
            </div>
            <small class="text-muted">
              ID: {{ experiment.experimentId }} (<clipboard-copy-link
                :text="experiment.experimentId"
                :link-classes="['text-reset']"
              >
                copy
                <span slot="icon"></span>
                <span slot="tooltip">Copied ID!</span> </clipboard-copy-link
              >)
            </small>
          </td>
        </tr>
        <tr>
          <th scope="row">Description</th>
          <td>{{ experiment.description }}</td>
        </tr>
        <tr>
          <th scope="row">Project</th>
          <td v-if="fullExperiment.project">
            {{ fullExperiment.projectName }}
          </td>
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
          <td v-if="fullExperiment.applicationName">
            {{ fullExperiment.applicationName }}
          </td>
          <td v-else class="font-italic text-muted">
            Unable to load interface {{ fullExperiment.experiment.executionId }}
          </td>
        </tr>
        <tr>
          <th scope="row">Compute Resource</th>
          <td v-if="fullExperiment.computeHostName">
            {{ fullExperiment.computeHostName }}
          </td>
          <td v-else class="font-italic text-muted">
            Unable to load compute resource {{ fullExperiment.resourceHostId }}
          </td>
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
        <tr
          v-if="
            fullExperiment.jobDetails && fullExperiment.jobDetails.length > 0
          "
        >
          <th scope="row">Job</th>
          <td>
            <table class="table">
              <thead>
                <th>Name</th>
                <th>ID</th>
                <th>Status</th>
                <th>Creation Time</th>
              </thead>
              <tbody>
                <tr
                  v-for="(jobDetail, index) in fullExperiment.jobDetails"
                  :key="jobDetail.jobId"
                >
                  <td>{{ jobDetail.jobName }}</td>
                  <td>{{ jobDetail.jobId }}</td>
                  <td>{{ jobDetail.jobStatusStateName }}</td>
                  <td>
                    <span :title="jobDetail.creationTime.toString()">{{
                      jobCreationTimes[index]
                    }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>
        <tr>
          <th scope="row">Notification List</th>
          <td>
            {{
              experiment.emailAddresses
                ? experiment.emailAddresses.join(", ")
                : ""
            }}
          </td>
        </tr>
        <tr
          v-if="
            fullExperiment.jobDetails && fullExperiment.jobDetails.length > 0
          "
        >
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
        <tr
          v-if="
            fullExperiment.jobDetails && fullExperiment.jobDetails.length > 0
          "
        >
          <th scope="row">Job Description</th>
          <td>
            <b-card
              v-for="jobDetail in fullExperiment.jobDetails"
              :key="jobDetail.jobId"
              :header="jobDetail.jobName"
            >
              <pre>{{ jobDetail.jobDescription }}</pre>
            </b-card>
          </td>
        </tr>
        <tr>
          <th scope="row">Creation Time</th>
          <td>
            <span :title="experiment.creationTime.toString()">{{
              creationTime
            }}</span>
          </td>
        </tr>
        <tr>
          <th scope="row">Last Modified Time</th>
          <td>
            <span
              :title="
                fullExperiment.experimentStatus.timeOfStateChange.toString()
              "
              >{{ lastModifiedTime }}</span
            >
          </td>
        </tr>
        <tr>
          <th scope="row">Wall Time Limit</th>
          <td>
            {{
              experiment.userConfigurationData.computationalResourceScheduling
                .wallTimeLimit
            }}
            minutes
          </td>
        </tr>
        <tr>
          <th scope="row">CPU Count</th>
          <td>
            {{
              experiment.userConfigurationData.computationalResourceScheduling
                .totalCPUCount
            }}
          </td>
        </tr>
        <tr>
          <th scope="row">Node Count</th>
          <td>
            {{
              experiment.userConfigurationData.computationalResourceScheduling
                .nodeCount
            }}
          </td>
        </tr>
        <tr
          v-if="
            experiment.userConfigurationData.computationalResourceScheduling
              .totalPhysicalMemory
          "
        >
          <th scope="row">Total Physical Memory</th>
          <td>
            {{
              experiment.userConfigurationData.computationalResourceScheduling.totalPhysicalMemory.toLocaleString()
            }}
            MB
          </td>
        </tr>
        <tr>
          <th scope="row">Queue</th>
          <td>
            {{
              experiment.userConfigurationData.computationalResourceScheduling
                .queueName
            }}
          </td>
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
                  :key="dp.productUri"
                />
              </li>
            </ul>
          </td>
        </tr>
        <tr>
          <th scope="row">Outputs</th>
          <td>
            <ul>
              <li
                v-for="output in experiment.experimentOutputs"
                :key="output.name"
              >
                {{ output.name }}:
                <template v-if="output.type.isSimpleValueType">
                  <span class="text-break">{{ output.value }}</span>
                </template>
                <data-product-viewer
                  v-for="dp in outputDataProducts[output.name]"
                  v-else-if="output.type.isFileValueType"
                  :data-product="dp"
                  :key="dp.productUri"
                />
              </li>
            </ul>
          </td>
        </tr>
        <tr>
          <th scope="row">Errors</th>
          <td>
            <b-card
              v-for="error in experiment.errors"
              :key="error.errorId"
              header="Error"
            >
              <p>{{ error.userFriendlyMessage }}</p>
              <pre class="pre-scrollable">{{ error.actualErrorMessage }}</pre>
            </b-card>
          </td>
        </tr>
        <template v-if="failedJobs.length > 0">
          <tr v-for="job in failedJobs" :key="job.jobId">
            <th scope="row">Job Submission Response</th>
            <td>
              <b-card v-if="job.stdOut" :header="job.jobName + ' STDOUT'">
                <pre class="pre-scrollable">{{ job.stdOut }}</pre>
              </b-card>
              <b-card v-if="job.stdErr" :header="job.jobName + ' STDERR'">
                <pre class="pre-scrollable">{{ job.stdErr }}</pre>
              </b-card>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
    <h2 class="h5 mb-3">Process Details</h2>
    <b-card
      v-for="process in experiment.processes"
      :key="process.processId"
      :header="process.processId"
    >
      <b-card
        v-for="task in process.sortedTasks"
        :key="task.taskId"
        :header="task.taskId"
      >
        <table class="table table-sm">
          <tbody>
            <tr>
              <th scope="row">Task Id</th>
              <td>{{ task.taskId }}</td>
            </tr>
            <tr>
              <th scope="row">Task Type</th>
              <td>{{ task.taskType.name }}</td>
            </tr>
            <tr>
              <th scope="row">Task Status</th>
              <td>{{ task.latestStatus.state.name }}</td>
            </tr>
            <tr>
              <th scope="row">Task Status Time</th>
              <td>
                <human-date :date="task.latestStatus.timeOfStateChange" />
              </td>
            </tr>
            <tr>
              <th scope="row">Task Status Reason</th>
              <td>{{ task.latestStatus.reason }}</td>
            </tr>
            <template v-if="task.taskErrors && task.taskErrors.length > 0">
              <tr>
                <th scope="row">Task Errors</th>
                <td>
                  <b-card
                    v-for="error in task.taskErrors"
                    :key="error.errorId"
                    :header="error.errorId"
                  >
                    <p>{{ error.userFriendlyMessage }}</p>
                    <pre class="pre-scrollable">{{
                      error.actualErrorMessage
                    }}</pre>
                  </b-card>
                </td>
              </tr>
            </template>
            <template v-if="task.jobs && task.jobs.length > 0">
              <tr>
                <th scope="row">Jobs</th>
                <td>
                  <b-card
                    v-for="job in task.jobs"
                    :key="job.jobId"
                    :header="job.jobName"
                  >
                    <pre>{{ job.jobDescription }}</pre>
                  </b-card>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </b-card>

      <b-card
        v-for="error in process.processErrors"
        :key="error.errorId"
        :header="'Process Error ' + error.errorId"
      >
        <p>{{ error.userFriendlyMessage }}</p>
        <pre class="pre-scrollable">{{ error.actualErrorMessage }}</pre>
      </b-card>
    </b-card>
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
      required: true,
    },
  },
  components: {
    "clipboard-copy-link": components.ClipboardCopyLink,
    "data-product-viewer": components.DataProductViewer,
    "human-date": components.HumanDate,
  },
  data() {
    return {
      fullExperiment: null,
    };
  },
  computed: {
    inputDataProducts() {
      const result = {};
      if (this.fullExperiment && this.fullExperiment.inputDataProducts) {
        this.fullExperiment.experiment.experimentInputs.forEach((input) => {
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
        this.fullExperiment.experiment.experimentOutputs.forEach((output) => {
          result[output.name] = this.getDataProducts(
            output,
            this.fullExperiment.outputDataProducts
          );
        });
      }
      return result;
    },
    creationTime: function () {
      return moment(this.fullExperiment.experiment.creationTime).fromNow();
    },
    lastModifiedTime: function () {
      return moment(
        this.fullExperiment.experimentStatus.timeOfStateChange
      ).fromNow();
    },
    jobCreationTimes: function () {
      return this.fullExperiment.jobDetails.map((jobDetail) =>
        moment(jobDetail.creationTime).fromNow()
      );
    },
    failedJobs() {
      if (this.fullExperiment && this.fullExperiment.jobDetails) {
        return this.fullExperiment.jobDetails.filter(
          (job) =>
            this.experiment.latestStatus.state ===
              models.ExperimentState.FAILED ||
            (job.latestJobStatus &&
              job.latestJobStatus.jobState === models.JobState.FAILED)
        );
      } else {
        return [];
      }
    },
  },
  created() {
    services.FullExperimentService.retrieve({
      lookup: this.experiment.experimentId,
    }).then((fullExperiment) => (this.fullExperiment = fullExperiment));
  },
  methods: {
    getDataProducts(io, collection) {
      if (!io.value || !collection) {
        return [];
      }
      let dataProducts = null;
      if (io.type === models.DataType.URI_COLLECTION) {
        const dataProductURIs = io.value.split(",");
        dataProducts = dataProductURIs.map((uri) =>
          collection.find((dp) => dp.productUri === uri)
        );
      } else {
        const dataProductURI = io.value;
        dataProducts = collection.filter(
          (dp) => dp.productUri === dataProductURI
        );
      }
      return dataProducts
        ? dataProducts.filter((dp) => (dp ? true : false))
        : [];
    },
  },
};
</script>

<style scoped>
.table {
  table-layout: fixed;
}
.table th[scope="row"] {
  width: 20%;
}
</style>
