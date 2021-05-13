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
          v-model="computationalResourceScheduling.totalCPUCount"
          required
          @input.native.stop="emitValueChanged"
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
            v-model="computationalResourceScheduling.wallTimeLimit"
            required
            @input.native.stop="emitValueChanged"
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
            v-model="computationalResourceScheduling.totalPhysicalMemory"
            @input.native.stop="emitValueChanged"
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
import { models, utils } from "django-airavata-api";
import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
Vue.use(BootstrapVue);

import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { config, dom } from "@fortawesome/fontawesome-svg-core";
import { faInfoCircle, faTimes } from "@fortawesome/free-solid-svg-icons";

// Make sure you tell Font Awesome to skip auto-inserting CSS into the <head>
config.autoAddCss = false;

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
  components: {
    FontAwesomeIcon,
  },
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
    closeIcon() {
      return faTimes;
    },
    infoIcon() {
      return faInfoCircle;
    }
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
