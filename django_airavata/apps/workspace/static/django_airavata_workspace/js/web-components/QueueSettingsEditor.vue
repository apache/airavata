<template>
  <div v-if="queue">
    <div class="card border-default">
      <b-link
        @click="showConfiguration = !showConfiguration"
        class="card-link text-dark"
      >
        <div class="card-body">
          <h5 class="card-title mb-4">
            Settings for queue {{ localQueueName }}
          </h5>
          <div class="row">
            <div class="col">
              <h3 class="h5 mb-0">
                {{ localNodeCount }}
              </h3>
              <span class="text-muted text-uppercase">NODE COUNT</span>
            </div>
            <div class="col">
              <h3 class="h5 mb-0">
                {{ localTotalCPUCount }}
              </h3>
              <span class="text-muted text-uppercase">CORE COUNT</span>
            </div>
            <div class="col">
              <h3 class="h5 mb-0">{{ localWallTimeLimit }} minutes</h3>
              <span class="text-muted text-uppercase">TIME LIMIT</span>
            </div>
            <div class="col" v-if="maxMemory > 0">
              <h3 class="h5 mb-0">{{ localTotalPhysicalMemory }} MB</h3>
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
          v-model="localQueueName"
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
          v-model="localNodeCount"
          required
          @input.native.stop="
            emitValueChanged('node-count-changed', localNodeCount)
          "
        >
        </b-form-input>
        <div slot="description">
          <font-awesome-icon :icon="infoIcon" />
          Max Allowed Nodes = {{ maxAllowedNodes }}
        </div>
      </b-form-group>
      <b-form-group label="Total Core Count" label-for="core-count">
        <b-form-input
          id="core-count"
          type="number"
          min="1"
          :max="maxAllowedCores"
          v-model="localTotalCPUCount"
          required
          @input.native.stop="
            emitValueChanged('total-cpu-count-changed', localTotalCPUCount)
          "
        >
        </b-form-input>
        <div slot="description">
          <font-awesome-icon :icon="infoIcon" />
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
            v-model="localWallTimeLimit"
            required
            @input.native.stop="
              emitValueChanged('walltime-limit-changed', localWallTimeLimit)
            "
          >
          </b-form-input>
        </b-input-group>
        <div slot="description">
          <font-awesome-icon :icon="infoIcon" />
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
            v-model="localTotalPhysicalMemory"
            @input.native.stop="
              emitValueChanged(
                'total-physical-memory-changed',
                localTotalPhysicalMemory
              )
            "
          >
          </b-form-input>
        </b-input-group>
        <div slot="description">
          <font-awesome-icon :icon="infoIcon" />
          Max Physical Memory = {{ maxMemory }} MB
        </div>
      </b-form-group>
      <div>
        <b-link class="text-secondary" @click="showConfiguration = false">
          <font-awesome-icon :icon="closeIcon" />
          Hide Settings</b-link
        >
      </div>
    </div>
  </div>
</template>

<script>
import { utils } from "django-airavata-api";
import Vue from "vue";
import vuestore from "./vuestore";
import { mapGetters } from "vuex";
import { BootstrapVue } from "bootstrap-vue";
Vue.use(BootstrapVue);

import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { config, dom } from "@fortawesome/fontawesome-svg-core";
import { faInfoCircle, faTimes } from "@fortawesome/free-solid-svg-icons";

// Make sure you tell Font Awesome to skip auto-inserting CSS into the <head>
config.autoAddCss = false;

export default {
  props: {
    queueName: {
      type: String,
      required: true,
    },
    totalCpuCount: {
      type: Number,
      required: true,
    },
    nodeCount: {
      type: Number,
      required: true,
    },
    wallTimeLimit: {
      type: Number,
      required: true,
    },
    totalPhysicalMemory: {
      type: Number,
      default: 0,
      // required: true,
    },
  },
  components: {
    FontAwesomeIcon,
  },
  store: vuestore,
  mounted() {
    // Add font awesome styles
    // https://github.com/FortAwesome/vue-fontawesome#web-components-with-vue-web-component-wrapper
    const { shadowRoot } = this.$parent.$options;
    const id = "fa-styles";

    if (!shadowRoot.getElementById(`${id}`)) {
      const faStyles = document.createElement("style");
      faStyles.setAttribute("id", id);
      faStyles.textContent = dom.css();
      shadowRoot.appendChild(faStyles);
    }
  },
  data() {
    return {
      localQueueName: this.queueName,
      localTotalCPUCount: this.totalCpuCount,
      localNodeCount: this.nodeCount,
      localWallTimeLimit: this.wallTimeLimit,
      localTotalPhysicalMemory: this.totalPhysicalMemory,
      showConfiguration: false,
    };
  },
  computed: {
    ...mapGetters([
      "queue",
      "queues",
      "maxAllowedCores",
      "maxAllowedNodes",
      "maxAllowedWalltime",
      "maxMemory",
    ]),
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
    closeIcon() {
      return faTimes;
    },
    infoIcon() {
      return faInfoCircle;
    },
  },
  methods: {
    emitValueChanged(eventName, value) {
      const inputEvent = new CustomEvent(eventName, {
        detail: [value],
        composed: true,
        bubbles: true,
      });
      this.$el.dispatchEvent(inputEvent);
    },
    queueChanged() {
      this.emitValueChanged("queue-name-changed", this.localQueueName);
    },
  },
  watch: {
    queueName(value) {
      this.localQueueName = value;
    },
    nodeCount(value) {
      this.localNodeCount = value;
    },
    totalCpuCount(value) {
      this.localTotalCPUCount = value;
    },
    wallTimeLimit(value) {
      this.localWallTimeLimit = value;
    },
    totalPhysicalMemory(value) {
      this.localTotalPhysicalMemory = value;
    },
  },
};
</script>

<style>
@import url("./styles.css");

:host {
  display: block;
  margin-bottom: 1rem;
}
</style>
