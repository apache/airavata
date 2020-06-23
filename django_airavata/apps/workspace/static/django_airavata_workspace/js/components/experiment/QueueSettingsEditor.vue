<template>
  <div>
    <div class="row">
      <div class="col">
        <div class="card border-default" :class="{ 'border-danger': !valid }">
          <b-link
            @click="showConfiguration = !showConfiguration"
            class="card-link text-dark"
          >
            <div class="card-body">
              <h5 class="card-title mb-4">
                Settings for queue {{ data.queueName }}
              </h5>
              <div class="row">
                <div class="col">
                  <h3 class="h5 mb-0">
                    {{ data.nodeCount }}
                  </h3>
                  <span class="text-muted text-uppercase">NODE COUNT</span>
                </div>
                <div class="col">
                  <h3 class="h5 mb-0">
                    {{ data.totalCPUCount }}
                  </h3>
                  <span class="text-muted text-uppercase">CORE COUNT</span>
                </div>
                <div class="col">
                  <h3 class="h5 mb-0">{{ data.wallTimeLimit }} minutes</h3>
                  <span class="text-muted text-uppercase">TIME LIMIT</span>
                </div>
              </div>
            </div>
          </b-link>
        </div>
      </div>
    </div>
    <div v-if="showConfiguration">
      <div class="row">
        <div class="col">
          <b-form-group
            label="Select a Queue"
            label-for="queue"
            :invalid-feedback="getValidationFeedback('queueName')"
            :state="getValidationState('queueName')"
          >
            <b-form-select
              id="queue"
              v-model="data.queueName"
              :options="queueOptions"
              required
              @change="queueChanged"
              :state="getValidationState('queueName')"
            >
            </b-form-select>
            <div slot="description">
              {{ queueDescription }}
            </div>
          </b-form-group>
          <b-form-group
            label="Node Count"
            label-for="node-count"
            :invalid-feedback="getValidationFeedback('nodeCount')"
            :state="getValidationState('nodeCount', true)"
          >
            <b-form-input
              id="node-count"
              type="number"
              min="1"
              :max="maxNodes"
              v-model="data.nodeCount"
              required
              :state="getValidationState('nodeCount', true)"
            >
            </b-form-input>
            <div slot="description">
              <i class="fa fa-info-circle" aria-hidden="true"></i>
              Max Allowed Nodes = {{ maxNodes }}
            </div>
          </b-form-group>
          <b-form-group
            label="Total Core Count"
            label-for="core-count"
            :invalid-feedback="getValidationFeedback('totalCPUCount')"
            :state="getValidationState('totalCPUCount', true)"
          >
            <b-form-input
              id="core-count"
              type="number"
              min="1"
              :max="maxCPUCount"
              v-model="data.totalCPUCount"
              required
              :state="getValidationState('totalCPUCount', true)"
            >
            </b-form-input>
            <div slot="description">
              <i class="fa fa-info-circle" aria-hidden="true"></i>
              Max Allowed Cores = {{ maxCPUCount }}
            </div>
          </b-form-group>
          <b-form-group
            label="Wall Time Limit (in minutes)"
            label-for="walltime-limit"
            :invalid-feedback="getValidationFeedback('wallTimeLimit')"
            :state="getValidationState('wallTimeLimit', true)"
          >
            <b-input-group right="minutes">
              <b-form-input
                id="walltime-limit"
                type="number"
                min="1"
                :max="maxWalltime"
                v-model="data.wallTimeLimit"
                required
                :state="getValidationState('wallTimeLimit', true)"
              >
              </b-form-input>
            </b-input-group>
            <div slot="description">
              <i class="fa fa-info-circle" aria-hidden="true"></i>
              Max Allowed Wall Time = {{ maxWalltime }} minutes
            </div>
          </b-form-group>
          <div>
            <a
              class="text-secondary action-link"
              href="#"
              @click.prevent="showConfiguration = false"
            >
              <i class="fa fa-times text-secondary" aria-hidden="true"></i>
              Hide Settings</a
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import { mixins, utils } from "django-airavata-common-ui";

export default {
  name: "queue-settings-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: models.ComputationalResourceSchedulingModel
    },
    appDeploymentId: {
      type: String,
      required: true
    },
    computeResourcePolicy: {
      type: models.ComputeResourcePolicy,
      required: false
    },
    batchQueueResourcePolicies: {
      type: Array,
      required: false
    }
  },
  data() {
    return {
      showConfiguration: false,
      appDeploymentQueues: null
    };
  },
  computed: {
    queueOptions: function() {
      const queueOptions = this.queueDefaults.map(queueDefault => {
        return {
          value: queueDefault.queueName,
          text: queueDefault.queueName
        };
      });
      return queueOptions;
    },
    selectedQueueDefault: function() {
      return this.queueDefaults.find(
        queue => queue.queueName === this.data.queueName
      );
    },
    maxCPUCount: function() {
      if (!this.selectedQueueDefault) {
        return 0;
      }
      const batchQueueResourcePolicy = this.getBatchQueueResourcePolicy(
        this.selectedQueueDefault.queueName
      );
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedCores,
          this.selectedQueueDefault.maxProcessors
        );
      }
      return this.selectedQueueDefault.maxProcessors;
    },
    maxNodes: function() {
      if (!this.selectedQueueDefault) {
        return 0;
      }
      const batchQueueResourcePolicy = this.getBatchQueueResourcePolicy(
        this.selectedQueueDefault.queueName
      );
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedNodes,
          this.selectedQueueDefault.maxNodes
        );
      }
      return this.selectedQueueDefault.maxNodes;
    },
    maxWalltime: function() {
      if (!this.selectedQueueDefault) {
        return 0;
      }
      const batchQueueResourcePolicy = this.getBatchQueueResourcePolicy(
        this.selectedQueueDefault.queueName
      );
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedWalltime,
          this.selectedQueueDefault.maxRunTime
        );
      }
      return this.selectedQueueDefault.maxRunTime;
    },
    queueDefaults() {
      return this.appDeploymentQueues
        ? this.appDeploymentQueues
            .filter(q => this.isQueueInComputeResourcePolicy(q.queueName))
            .sort((a, b) => {
              // Sort default first, then by alphabetically by name
              if (a.isDefaultQueue) {
                return -1;
              } else if (b.isDefaultQueue) {
                return 1;
              } else {
                return a.queueName.localeCompare(b.queueName);
              }
            })
        : [];
    },
    defaultQueue() {
      if (this.queueDefaults.length === 0) {
        return null;
      }
      return this.queueDefaults[0];
    },
    defaultQueueBatchQueueResourcePolicy() {
      if (this.defaultQueue) {
        return this.getBatchQueueResourcePolicy(this.defaultQueue.queueName);
      } else {
        return null;
      }
    },
    queueDescription() {
      return this.selectedQueueDefault
        ? this.selectedQueueDefault.queueDescription
        : null;
    },
    validation() {
      // Don't run validation if we don't have selectedQueueDefault
      if (!this.selectedQueueDefault) {
        return this.data.validate();
      }
      return this.data.validate(
        this.selectedQueueDefault,
        this.getBatchQueueResourcePolicy(this.selectedQueueDefault.queueName)
      );
    },
    valid() {
      return Object.keys(this.validation).length === 0;
    }
  },
  methods: {
    queueChanged: function(queueName) {
      const queueDefault = this.queueDefaults.find(
        queue => queue.queueName === queueName
      );
      this.data.totalCPUCount = this.getDefaultCPUCount(queueDefault);
      this.data.nodeCount = this.getDefaultNodeCount(queueDefault);
      this.data.wallTimeLimit = this.getDefaultWalltime(queueDefault);
    },
    validate() {
      if (!this.valid) {
        this.$emit("invalid");
      } else {
        this.$emit("valid");
      }
    },
    loadAppDeploymentQueues() {
      return services.ApplicationDeploymentService.getQueues({
        lookup: this.appDeploymentId
      }).then(queueDefaults => (this.appDeploymentQueues = queueDefaults));
    },
    setDefaultQueue() {
      if (this.queueDefaults.length === 0) {
        this.data.queueName = null;
        return;
      }
      const defaultQueue = this.queueDefaults[0];

      this.data.queueName = defaultQueue.queueName;
      this.data.totalCPUCount = this.getDefaultCPUCount(defaultQueue);
      this.data.nodeCount = this.getDefaultNodeCount(defaultQueue);
      this.data.wallTimeLimit = this.getDefaultWalltime(defaultQueue);
    },
    isQueueInComputeResourcePolicy: function(queueName) {
      if (!this.computeResourcePolicy) {
        return true;
      }
      return this.computeResourcePolicy.allowedBatchQueues.includes(queueName);
    },
    getBatchQueueResourcePolicy: function(queueName) {
      if (
        !this.batchQueueResourcePolicies ||
        this.batchQueueResourcePolicies.length === 0
      ) {
        return null;
      }
      return this.batchQueueResourcePolicies.find(
        bqrp => bqrp.queuename === queueName
      );
    },
    getDefaultCPUCount: function(queueDefault) {
      const batchQueueResourcePolicy = this.getBatchQueueResourcePolicy(
        queueDefault.queueName
      );
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedCores,
          queueDefault.defaultCPUCount
        );
      }
      return queueDefault.defaultCPUCount;
    },
    getDefaultNodeCount: function(queueDefault) {
      const batchQueueResourcePolicy = this.getBatchQueueResourcePolicy(
        queueDefault.queueName
      );
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedNodes,
          queueDefault.defaultNodeCount
        );
      }
      return queueDefault.defaultNodeCount;
    },
    getDefaultWalltime: function(queueDefault) {
      const batchQueueResourcePolicy = this.getBatchQueueResourcePolicy(
        queueDefault.queueName
      );
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedWalltime,
          queueDefault.defaultWalltime
        );
      }
      return queueDefault.defaultWalltime;
    },
    getValidationFeedback: function(properties) {
      return utils.getProperty(this.validation, properties);
    },
    getValidationState: function(properties, showValidState) {
      return this.getValidationFeedback(properties)
        ? "invalid"
        : showValidState
        ? "valid"
        : null;
    }
  },
  watch: {
    appDeploymentId() {
      this.loadAppDeploymentQueues().then(() => this.setDefaultQueue());
    },
    // If the default queue changes, re-set queue defaults
    defaultQueue(value, oldValue) {
      // Only set defaultQueue if it changes from a non-null value to some
      // different non-null value (initially it is null when data is loading)
      if (oldValue && value !== oldValue) {
        this.setDefaultQueue();
      }
    },
    // If batch queue resource policy for the default queue changes, re-set queue defaults
    defaultQueueBatchQueueResourcePolicy(newValue, oldValue) {
      // Only set defaultQueue if it changes from a non-null value to some
      // different non-null value (initially it is null when data is loading)
      if (oldValue && newValue !== oldValue) {
        this.setDefaultQueue();
      }
    }
  },
  mounted: function() {
    this.loadAppDeploymentQueues().then(() => {
      // For brand new queue settings (no queueName specified) load the default
      // queue and its default values and apply them
      if (!this.value.queueName) {
        this.setDefaultQueue();
      }
    });
    this.$on("input", () => this.validate());
  }
};
</script>

<style></style>
