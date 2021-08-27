<template>
  <compute-resource-selector
    :value="value"
    :includedComputeResources="computeResources"
    @input.stop="computeResourceChanged"
  />
</template>

<script>
import vuestore from "./vuestore";
import { mapGetters } from "vuex";
import ComputeResourceSelector from "./ComputeResourceSelector.vue";

export default {
  name: "experiment-compute-resource-selector",
  props: {
    value: {
      // compute resource host id
      type: String,
      required: true,
    },
  },
  store: vuestore,
  components: {
    ComputeResourceSelector,
  },
  data() {
    return {
      resourceHostId: this.value,
    };
  },
  computed: {
    // compute resources for the current set of application deployments
    ...mapGetters(["computeResources"]),
  },
  methods: {
    computeResourceChanged(event) {
      const [computeResourceId] = event.detail;
      this.resourceHostId = computeResourceId;
      this.emitValueChanged();
    },
    emitValueChanged: function () {
      const inputEvent = new CustomEvent("input", {
        detail: [this.resourceHostId],
        composed: true,
        bubbles: true,
      });
      this.$el.dispatchEvent(inputEvent);
    },
  },
  watch: {
    value() {
      this.resourceHostId = this.value;
    },
  },
};
</script>

<style>
@import "./styles.css";
</style>
