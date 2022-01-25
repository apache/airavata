<template>
  <compute-resource-selector
    :value="resourceHostId"
    :includedComputeResources="computeResources"
    @input.stop="computeResourceChanged"
  />
</template>

<script>
import store from "./store";
import { mapGetters } from "vuex";
import ComputeResourceSelector from "./ComputeResourceSelector.vue";

export default {
  name: "experiment-compute-resource-selector",
  props: {
    value: {
      type: String,
      default: null,
    },
    applicationModuleId: {
      type: String,
      required: true,
    },
  },
  created() {
    this.$store.dispatch("initializeComputeResources", {
      applicationModuleId: this.applicationModuleId,
      resourceHostId: this.value,
    });
  },
  store: store,
  components: {
    ComputeResourceSelector,
  },
  computed: {
    ...mapGetters([
      // compute resources for the current set of application deployments
      "computeResources",
      "resourceHostId",
      "groupResourceProfileId",
    ]),
  },
  methods: {
    computeResourceChanged(event) {
      const [computeResourceId] = event.detail;
      this.$store.dispatch("updateComputeResourceHostId", {
        computeResourceId,
      });
    },
  },
  watch: {
    value(value) {
      if (value && value !== this.resourceHostId) {
        this.$store.dispatch("updateComputeResourceHostId", {
          computeResourceId: value,
        });
      }
    },
  },
};
</script>

<style lang="scss">
@import "./styles";
:host {
  display: block;
}
</style>
