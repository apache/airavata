<template>

  <div>
    <div class="row">
      <div class="col">
        <div class="card border-default">
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
                <h3 class="h5 mb-0">{{ localComputationalResourceScheduling.wallTimeLimit }}</h3>
                <span class="text-muted text-uppercase">TIME LIMIT</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div v-if="!showConfiguration">
          <i
            class="fa fa-cog text-secondary"
            aria-hidden="true"
          ></i>
          <a
            class="text-secondary"
            href="#"
            @click.prevent="showConfiguration = true"
          >Configure Resource</a>
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
              v-model="localComputationalResourceScheduling.queueName"
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
            :state="getValidationState('nodeCount')"
          >
            <b-form-input
              id="node-count"
              type="number"
              min="1"
              :max="maxNodes"
              v-model="localComputationalResourceScheduling.nodeCount"
              required
              @input="emitValueChanged"
              :state="getValidationState('nodeCount')"
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
            :state="getValidationState('totalCPUCount')"
          >
            <b-form-input
              id="core-count"
              type="number"
              min="1"
              :max="maxCPUCount"
              v-model="localComputationalResourceScheduling.totalCPUCount"
              required
              @input="emitValueChanged"
              :state="getValidationState('totalCPUCount')"
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
            label="Wall Time Limit"
            label-for="walltime-limit"
            :feedback="getValidationFeedback('wallTimeLimit')"
            :state="getValidationState('wallTimeLimit')"
          >
            <b-input-group right="minutes">
              <b-form-input
                id="walltime-limit"
                type="number"
                min="1"
                :max="maxWalltime"
                v-model="localComputationalResourceScheduling.wallTimeLimit"
                required
                @input="emitValueChanged"
                :state="getValidationState('wallTimeLimit')"
              >
              </b-form-input>
            </b-input-group>
            <div slot="description">
              <i
                class="fa fa-info-circle"
                aria-hidden="true"
              ></i>
              Max Allowed Wall Time = {{ maxWalltime }}
            </div>
          </b-form-group>
          <div>
            <i
              class="fa fa-times text-secondary"
              aria-hidden="true"
            ></i>
            <a
              class="text-secondary"
              href="#"
              @click.prevent="showConfiguration = false"
            >Hide Settings</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import { utils } from "django-airavata-common-ui";

export default {
  name: "queue-settings-editor",
  props: {
    value: {
      type: models.ComputationalResourceSchedulingModel,
      required: true
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
      localComputationalResourceScheduling: this.value.clone(),
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
    }
  },
  methods: {
    queueChanged: function(queueName) {
      const queueDefault = this.queueDefaults.find(
        queue => queue.queueName === queueName
      );
      this.localComputationalResourceScheduling.totalCPUCount = this.getDefaultCPUCount(
        queueDefault
      );
      this.localComputationalResourceScheduling.nodeCount = this.getDefaultNodeCount(
        queueDefault
      );
      this.localComputationalResourceScheduling.wallTimeLimit = this.getDefaultWalltime(
        queueDefault
      );
      this.emitValueChanged();
    },
    emitValueChanged: function() {
      this.$emit("input", this.localComputationalResourceScheduling);
    },
    loadQueueDefaults: function(updateQueueSettings) {
      services.ApplicationDeploymentService.getQueues({
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
          this.emitValueChanged();
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
      return utils.getProperty(
        this.localComputationalResourceScheduling.validate(
          this.selectedQueueDefault,
          this.getBatchQueueResourcePolicy(this.selectedQueueDefault.queueName)
        ),
        properties
      );
    },
    getValidationState: function(properties) {
      return this.getValidationFeedback(properties) ? "invalid" : null;
    }
  },
  watch: {
    value: function(newValue) {
      this.localComputationalResourceScheduling = newValue.clone();
    },
    appDeploymentId: function() {
      this.loadQueueDefaults();
    }
  },
  mounted: function() {
    // For brand new queue settings (no queueName specified) load the default
    // queue and its default values and apply them
    const updateQueueSettings = !this.value.queueName;
    this.loadQueueDefaults(updateQueueSettings);
  }
};
</script>

<style>
</style>
