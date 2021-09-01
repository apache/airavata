<template>
  <div v-if="queue">
    <div class="card border-default">
      <b-link
        @click="showConfiguration = !showConfiguration"
        class="card-link text-dark"
      >
        <div class="card-body">
          <h5 class="card-title mb-4">Settings for queue {{ queueName }}</h5>
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
          :value="queueName"
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
          :value="totalCPUCount"
          required
          @input.native.stop="updateTotalCPUCount"
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
            :value="wallTimeLimit"
            required
            @input.native.stop="updateWallTimeLimit"
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
            :value="totalPhysicalMemory"
            @input.native.stop="updateTotalPhysicalMemory"
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
import store from "./store";
import { mapGetters } from "vuex";
import { BootstrapVue } from "bootstrap-vue";
Vue.use(BootstrapVue);

import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { config, dom } from "@fortawesome/fontawesome-svg-core";
import { faInfoCircle, faTimes } from "@fortawesome/free-solid-svg-icons";

// Make sure you tell Font Awesome to skip auto-inserting CSS into the <head>
config.autoAddCss = false;

export default {
  components: {
    FontAwesomeIcon,
  },
  store: store,
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
      "queueName",
      "totalCPUCount",
      "nodeCount",
      "wallTimeLimit",
      "totalPhysicalMemory",
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
    queueDescription() {
      return this.queue ? this.queue.queueDescription : null;
    },
    closeIcon() {
      return faTimes;
    },
    infoIcon() {
      return faInfoCircle;
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
};
</script>

<style>
@import url("./styles.css");

:host {
  display: block;
  margin-bottom: 1rem;
}
</style>
