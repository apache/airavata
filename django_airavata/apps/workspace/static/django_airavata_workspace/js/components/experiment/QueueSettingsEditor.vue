<template>

  <div>
    <div class="row">
      <div class="col">
        <div
          class="card border-default"
          :class="{ 'border-danger': !valid }"
        >
          <b-link
            @click="showConfiguration = !showConfiguration"
            class="card-link text-dark"
          >
            <div class="card-body">
              <h5 class="card-title mb-4">Settings for queue {{ localComputationalResourceScheduling.queueName }}</h5>
              <div class="row">
                <div class="col">
                  <h3 class="h5 mb-0">{{ localComputationalResourceScheduling.nodeCount }}</h3>
                  <span class="text-muted text-uppercase">NODE COUNT</span>
                </div>
                <div class="col">
                  <h3 class="h5 mb-0">{{ localComputationalResourceScheduling.totalCPUCount }}</h3>
                  <span class="text-muted text-uppercase">CORE COUNT</span>
                </div>
                <div class="col">
                  <h3 class="h5 mb-0">{{ localComputationalResourceScheduling.wallTimeLimit }} minutes</h3>
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
            :feedback="getValidationFeedback('queueName')"
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
              {{ selectedQueueDefault.queueDescription }}
            </div>
          </b-form-group>
          <b-form-group
            label="Node Count"
            label-for="node-count"
            :feedback="getValidationFeedback('nodeCount')"
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
              <i
                class="fa fa-info-circle"
                aria-hidden="true"
              ></i>
              Max Allowed Nodes = {{ maxNodes }}
            </div>
          </b-form-group>
          <b-form-group
            label="Total Core Count"
            label-for="core-count"
            :feedback="getValidationFeedback('totalCPUCount')"
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
              <i
                class="fa fa-info-circle"
                aria-hidden="true"
              ></i>
              Max Allowed Cores = {{ maxCPUCount }}
            </div>
          </b-form-group>
          <b-form-group
            label="Wall Time Limit (in minutes)"
            label-for="walltime-limit"
            :feedback="getValidationFeedback('wallTimeLimit')"
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
              <i
                class="fa fa-info-circle"
                aria-hidden="true"
              ></i>
              Max Allowed Wall Time = {{ maxWalltime }} minutes
            </div>
          </b-form-group>
          <div>
            <a
              class="text-secondary action-link"
              href="#"
              @click.prevent="showConfiguration = false"
            >
              <i
                class="fa fa-times text-secondary"
                aria-hidden="true"
              ></i>
              Hide Settings</a>
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
      queueDefaults: [],
      showConfiguration: false
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
    localComputationalResourceScheduling() {
      return this.data;
    },
    selectedQueueDefault: function() {
      return this.queueDefaults.find(
        queue =>
          queue.queueName ===
          this.localComputationalResourceScheduling.queueName
      );
    },
    maxCPUCount: function() {
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
    validation() {
      // Don't run validation if we don't have selectedQueueDefault
      if (!this.selectedQueueDefault) {
        return {};
      }
      return this.localComputationalResourceScheduling.validate(
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
    loadQueueDefaults: function(updateQueueSettings) {
      return services.ApplicationDeploymentService.getQueues({
        lookup: this.appDeploymentId
      }).then(queueDefaults => {
        // Sort queue defaults
        this.queueDefaults = queueDefaults
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
          });

        if (updateQueueSettings) {
          // Find the default queue and apply it's settings
          const defaultQueue = this.queueDefaults[0];

          this.localComputationalResourceScheduling.queueName =
            defaultQueue.queueName;
          this.localComputationalResourceScheduling.totalCPUCount = this.getDefaultCPUCount(
            defaultQueue
          );
          this.localComputationalResourceScheduling.nodeCount = this.getDefaultNodeCount(
            defaultQueue
          );
          this.localComputationalResourceScheduling.wallTimeLimit = this.getDefaultWalltime(
            defaultQueue
          );
        }
      });
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
    appDeploymentId: function() {
      this.loadQueueDefaults(true);
    }
  },
  mounted: function() {
    // For brand new queue settings (no queueName specified) load the default
    // queue and its default values and apply them
    const updateQueueSettings = !this.value.queueName;
    this.loadQueueDefaults(updateQueueSettings).then(() => this.validate());
    this.$on("input", () => this.validate());
  }
};
</script>

<style>
</style>
