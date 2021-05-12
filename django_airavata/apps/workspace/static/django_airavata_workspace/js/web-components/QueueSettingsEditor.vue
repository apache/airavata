<template>
  <div>
    <div class="card border-default">
      <b-link
        @click="showConfiguration = !showConfiguration"
        class="card-link text-dark"
      >
        <div class="card-body">
          <h5 class="card-title mb-4">
            Settings for queue {{ computationalResourceScheduling.queueName }}
          </h5>
          <div class="row">
            <div class="col">
              <h3 class="h5 mb-0">
                {{ computationalResourceScheduling.nodeCount }}
              </h3>
              <span class="text-muted text-uppercase">NODE COUNT</span>
            </div>
            <div class="col">
              <h3 class="h5 mb-0">
                {{ computationalResourceScheduling.totalCPUCount }}
              </h3>
              <span class="text-muted text-uppercase">CORE COUNT</span>
            </div>
            <div class="col">
              <h3 class="h5 mb-0">
                {{ computationalResourceScheduling.wallTimeLimit }} minutes
              </h3>
              <span class="text-muted text-uppercase">TIME LIMIT</span>
            </div>
            <div class="col" v-if="maxMemory > 0">
              <h3 class="h5 mb-0">
                {{ computationalResourceScheduling.totalPhysicalMemory }} MB
              </h3>
              <span class="text-muted text-uppercase">PHYSICAL MEMORY</span>
            </div>
          </div>
        </div>
      </b-link>
    </div>
    <div v-if="showConfiguration">
      <b-form-group label="Select a Queue" label-for="queue">
        <b-form-select
          id="queue"
          v-model="computationalResourceScheduling.queueName"
          :options="queueOptions"
          required
          @change="queueChanged"
        >
        </b-form-select>
        <div slot="description">{{ queueDescription }}</div>
      </b-form-group>
      <b-form-group label="Node Count" label-for="node-count">
        <b-form-input
          id="node-count"
          type="number"
          min="1"
          :max="maxAllowedNodes"
          v-model="computationalResourceScheduling.nodeCount"
          required
          @input.native.stop="emitValueChanged"
        >
        </b-form-input>
        <div slot="description">
          <i class="fa fa-info-circle" aria-hidden="true"></i>
          Max Allowed Nodes = {{ maxAllowedNodes }}
        </div>
      </b-form-group>
      <b-form-group label="Total Core Count" label-for="core-count">
        <b-form-input
          id="core-count"
          type="number"
          min="1"
          :max="maxAllowedCores"
          v-model="computationalResourceScheduling.totalCPUCount"
          required
          @input.native.stop="emitValueChanged"
        >
        </b-form-input>
        <div slot="description">
          <i class="fa fa-info-circle" aria-hidden="true"></i>
          Max Allowed Cores = {{ maxAllowedCores }}
        </div>
      </b-form-group>
      <b-form-group label="Wall Time Limit" label-for="walltime-limit">
        <b-input-group append="minutes">
          <b-form-input
            id="walltime-limit"
            type="number"
            min="1"
            :max="maxAllowedWalltime"
            v-model="computationalResourceScheduling.wallTimeLimit"
            required
            @input.native.stop="emitValueChanged"
          >
          </b-form-input>
        </b-input-group>
        <div slot="description">
          <i class="fa fa-info-circle" aria-hidden="true"></i>
          Max Allowed Wall Time = {{ maxAllowedWalltime }} minutes
        </div>
      </b-form-group>
      <b-form-group
        v-if="maxMemory > 0"
        label="Total Physical Memory"
        label-for="total-physical-memory"
      >
        <b-input-group append="MB">
          <b-form-input
            id="total-physical-memory"
            type="number"
            min="0"
            :max="maxMemory"
            v-model="computationalResourceScheduling.totalPhysicalMemory"
            @input.native.stop="emitValueChanged"
          >
          </b-form-input>
        </b-input-group>
        <div slot="description">
          <i class="fa fa-info-circle" aria-hidden="true"></i>
          Max Physical Memory = {{ maxMemory }} MB
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
</template>

<script>
import { models, utils } from "django-airavata-api";
import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
Vue.use(BootstrapVue);

export default {
  props: {
    value: {
      type: models.ComputationalResourceSchedulingModel,
      // required: true,
    },
    queues: {
      type: Array, // of BatchQueue
      // required: true,
    },
    maxAllowedNodes: {
      type: Number,
      required: true,
    },
    maxAllowedCores: {
      type: Number,
      required: true,
    },
    maxAllowedWalltime: {
      type: Number,
      required: true,
    },
    maxMemory: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      computationalResourceScheduling: this.cloneValue(),
      showConfiguration: false,
    };
  },
  computed: {
    queueOptions() {
      if (!this.queues) {
        return [];
      }
      const queueOptions = this.queues.map((q) => {
        return {
          value: q.queueName,
          text: q.queueName,
        };
      });
      utils.StringUtils.sortIgnoreCase(queueOptions, (q) => q.text);
      return queueOptions;
    },
    queue() {
      return this.queues
        ? this.queues.find((q) => q.queueName === this.queueName)
        : null;
    },
    queueName() {
      return this.computationalResourceScheduling
        ? this.computationalResourceScheduling.queueName
        : null;
    },
    queueDescription() {
      return this.queue ? this.queue.queueDescription : null;
    },
  },
  methods: {
    cloneValue() {
      return this.value
        ? this.value.clone()
        : new models.ComputationalResourceSchedulingModel();
    },
    emitValueChanged() {
      const inputEvent = new CustomEvent("input", {
        detail: [this.computationalResourceScheduling.clone()],
        composed: true,
        bubbles: true,
      });
      this.$el.dispatchEvent(inputEvent);
    },
    queueChanged() {
      this.emitValueChanged();
    },
  },
  watch: {
    value: {
      handler() {
        this.computationalResourceScheduling = this.cloneValue();
      },
      deep: true,
    },
  },
};
</script>

<style>
@import url("./styles.css");
</style>
