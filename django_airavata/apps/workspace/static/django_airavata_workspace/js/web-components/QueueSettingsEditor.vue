<template>
  <div>
    <div class="card border-default">
      <b-link
        @click="showConfiguration = !showConfiguration"
        class="card-link text-dark"
      >
        <div class="card-body">
          <h5 class="card-title mb-4">
            Settings for queue {{ selectedQueueName }}
          </h5>
          <div class="row">
            <div class="col">
              <h3 class="h5 mb-0">
                {{ nodeCount }}
              </h3>
              <span class="text-muted text-uppercase">NODE COUNT</span>
            </div>
            <div class="col">
              <h3 class="h5 mb-0">
                {{ totalCPUCount }}
              </h3>
              <span class="text-muted text-uppercase">CORE COUNT</span>
            </div>
            <div class="col">
              <h3 class="h5 mb-0">{{ wallTimeLimit }} minutes</h3>
              <span class="text-muted text-uppercase">TIME LIMIT</span>
            </div>
            <div class="col" v-if="maxMemory > 0">
              <h3 class="h5 mb-0">{{ totalPhysicalMemory }} MB</h3>
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
          :value="selectedQueueName"
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
          :value="nodeCount"
          required
          @input.native.stop="updateNodeCount"
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
          :value="totalCPUCount"
          required
          @input.native.stop="updateTotalCPUCount"
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
            :value="wallTimeLimit"
            required
            @input.native.stop="updateWallTimeLimit"
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
            :value="totalPhysicalMemory"
            @input.native.stop="updateTotalPhysicalMemory"
          >
          </b-form-input>
        </b-input-group>
        <div slot="description">
          <i class="fa fa-info-circle" aria-hidden="true"></i>
          Max Physical Memory = {{ maxMemory }} MB
        </div>
      </b-form-group>
      <div>
        <b-link class="text-secondary" @click="showConfiguration = false">
          <i class="fa fa-times" aria-hidden="true"></i>
          Hide Settings</b-link
        >
      </div>
    </div>
  </div>
</template>

<script>
import { utils } from "django-airavata-api";
import Vue from "vue";
import store from "./store";
import { mapGetters } from "vuex";
import { BootstrapVue } from "bootstrap-vue";
Vue.use(BootstrapVue);

export default {
  store: store,
  props: {
    queueName: {
      type: String,
    },
  },
  created() {
    if (this.queueName && this.selectedQueueName !== this.queueName) {
      this.queueChanged(this.queueName);
    }
  },
  data() {
    return {
      showConfiguration: false,
    };
  },
  computed: {
    ...mapGetters({
      queue: "queue",
      queues: "queues",
      maxAllowedCores: "maxAllowedCores",
      maxAllowedNodes: "maxAllowedNodes",
      maxAllowedWalltime: "maxAllowedWalltime",
      maxMemory: "maxMemory",
      selectedQueueName: "queueName",
      totalCPUCount: "totalCPUCount",
      nodeCount: "nodeCount",
      wallTimeLimit: "wallTimeLimit",
      totalPhysicalMemory: "totalPhysicalMemory",
    }),
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
    queueDescription() {
      return this.queue ? this.queue.queueDescription : null;
    },
  },
  methods: {
    queueChanged(queueName) {
      this.$store.dispatch("updateQueueName", { queueName });
    },
    updateNodeCount(event) {
      this.$store.dispatch("updateNodeCount", {
        nodeCount: event.target.value,
      });
    },
    updateTotalCPUCount(event) {
      this.$store.dispatch("updateTotalCPUCount", {
        totalCPUCount: event.target.value,
      });
    },
    updateWallTimeLimit(event) {
      this.$store.dispatch("updateWallTimeLimit", {
        wallTimeLimit: event.target.value,
      });
    },
    updateTotalPhysicalMemory(event) {
      this.$store.dispatch("updateTotalPhysicalMemory", {
        totalPhysicalMemory: event.target.value,
      });
    },
  },
  watch: {
    queueName(value) {
      if (value && this.selectedQueueName !== value) {
        this.queueChanged(value);
      }
    },
  },
};
</script>

<style lang="scss">
@import "./styles";

:host {
  display: block;
  margin-bottom: 1rem;
}
</style>
