<template>
  <div>
    <div class="row">
      <div class="col-auto mr-auto">
        <h1 class="h4 mb-4">
          <slot name="title">Experiment Summary</slot>
        </h1>
      </div>
      <div class="col-auto">
        <share-button :entity-id="experiment.experimentId" />
        <b-link
          v-if="isEditable"
          class="btn btn-primary"
          :href="editLink"
        >
          Edit
          <i
            class="fa fa-edit"
            aria-hidden="true"
          ></i>
        </b-link>
        <b-link
          v-if="isLaunchable"
          class="btn btn-primary"
          @click="launch"
        >
          Launch
          <i
            class="fa fa-running"
            aria-hidden="true"
          ></i>
        </b-link>
        <b-btn
          v-if="isClonable"
          variant="primary"
          @click="clone"
        >
          Clone
          <i
            class="fa fa-copy"
            aria-hidden="true"
          ></i>
        </b-btn>
        <b-btn
          v-if="isCancelable"
          variant="primary"
          @click="cancel"
        >
          Cancel
          <i
            class="fa fa-window-close"
            aria-hidden="true"
          ></i>
        </b-btn>
      </div>
    </div>
    <template v-for="output in experiment.experimentOutputs">

      <div
        class="row"
        v-if="experiment.isFinished && outputDataProducts[output.name].length > 0"
        :key="output.name"
      >
        <div class="col">
          <output-display-container
            :experiment-output="output"
            :data-products="outputDataProducts[output.name]"
            :output-views="localFullExperiment.outputViews[output.name]"
            :experiment-id="experiment.experimentId"
          />
        </div>
      </div>
    </template>
    <div
      class="row"
      v-if="experiment.isFinished && storageDirLink"
    >
      <div class="col">
        <b-card header="Other Files">
          <b-link :href="storageDirLink">Storage Directory</b-link>
        </b-card>
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
                  <td v-if="localFullExperiment.project">{{ localFullExperiment.projectName }}</td>
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
                  <td v-if="localFullExperiment.applicationName">{{ localFullExperiment.applicationName }}</td>
                  <td
                    v-else
                    class="font-italic text-muted"
                  >Unable to load interface {{ localFullExperiment.experiment.executionId }}</td>
                </tr>
                <tr>
                  <th scope="row">Compute Resource</th>
                  <td v-if="localFullExperiment.computeHostName">{{ localFullExperiment.computeHostName }}</td>
                  <td
                    v-else
                    class="font-italic text-muted"
                  >Unable to load compute resource {{ localFullExperiment.resourceHostId }}</td>
                </tr>
                <tr>
                  <th scope="row">Experiment Status</th>
                  <td>
                    <template v-if="localFullExperiment.experiment.isProgressing">
                      <i class="fa fa-sync-alt fa-spin"></i>
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
                <tr
                  v-for="(jobDetail, index) in localFullExperiment.jobDetails"
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
              <th scope="row">Errors</th>
              <td>
                <b-card
                  v-for="error in experiment.errors"
                  :key="error.errorId"
                  header="Error"
                >
                  <p>{{error.userFriendlyMessage}}</p>
                </b-card>
              </td>
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
import { components, notifications } from "django-airavata-common-ui";
import OutputDisplayContainer from "./output-displays/OutputDisplayContainer";
import urls from "../../utils/urls";

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
  components: {
    "data-product-viewer": components.DataProductViewer,
    "clipboard-copy-link": components.ClipboardCopyLink,
    "share-button": components.ShareButton,
    OutputDisplayContainer
  },
  computed: {
    inputDataProducts() {
      const result = {};
      if (
        this.localFullExperiment &&
        this.localFullExperiment.inputDataProducts
      ) {
        this.localFullExperiment.experiment.experimentInputs.forEach(input => {
          result[input.name] = this.getDataProducts(
            input,
            this.localFullExperiment.inputDataProducts
          );
        });
      }
      return result;
    },
    outputDataProducts() {
      const result = {};
      if (
        this.localFullExperiment &&
        this.localFullExperiment.outputDataProducts
      ) {
        this.localFullExperiment.experiment.experimentOutputs.forEach(
          output => {
            result[output.name] = this.getDataProducts(
              output,
              this.localFullExperiment.outputDataProducts
            );
          }
        );
      }
      return result;
    },
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
    },
    editLink() {
      return urls.editExperiment(this.experiment);
    },
    isEditable() {
      return (
        this.experiment.isEditable && this.localFullExperiment.applicationName && !this.launching
      );
    },
    isLaunchable(){
      return this.isEditable;
    },
    isClonable() {
      return this.localFullExperiment.applicationName;
    },
    isCancelable() {
      return this.localFullExperiment.experiment.isCancelable;
    },
    storageDirLink() {
      if (this.experiment.relativeExperimentDataDir) {
        return urls.storageDirectory(this.experiment.relativeExperimentDataDir);
      } else {
        return null;
      }
    }
  },
  methods: {
    loadExperiment: function() {
      return services.FullExperimentService.retrieve(
        { lookup: this.localFullExperiment.experiment.experimentId },
        { ignoreErrors: true, showSpinner: false }
      ).then(exp => (this.localFullExperiment = exp));
    },
    initPollingExperiment: function() {
      var pollExperiment = function() {
        if (
          (this.launching &&
            !this.localFullExperiment.experiment.hasLaunched) ||
          this.localFullExperiment.experiment.isProgressing
        ) {
          this.loadExperiment()
            .then(() => {
              setTimeout(pollExperiment.bind(this), 3000);
            })
            .catch(() => {
              // Wait 30 seconds after an error and then try again
              setTimeout(pollExperiment.bind(this), 30000);
            });
        }
      }.bind(this);
      setTimeout(pollExperiment, 3000);
    },
    clone() {
      services.ExperimentService.clone({
        lookup: this.experiment.experimentId
      }).then(clonedExperiment => {
        urls.navigateToEditExperiment(clonedExperiment);
      });
    },
    launch() {
      services.ExperimentService.launch({
        lookup: this.experiment.experimentId
      }).then(() => {
        this.$emit("Launched")
      });
    },
    cancel() {
      services.ExperimentService.cancel({
        lookup: this.experiment.experimentId
      }).then(() => {
          notifications.NotificationList.add(
            new notifications.Notification({
              type: "SUCCESS",
              message: "Cancel-experiment requested",
              duration: 5
            })
          )
        });
    },
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
  },
  watch: {
    launching: function(val){
      if(val==true){
        this.initPollingExperiment();
      }
    }
  },
  mounted: function() {
    this.initPollingExperiment();
  }
};
</script>
