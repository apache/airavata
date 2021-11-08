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
  store: store,
  components: {
    ComputeResourceSelector,
  },
  computed: {
    ...mapGetters([
      // compute resources for the current set of application deployments
      "computeResources",
      "resourceHostId",
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
};
</script>

<style lang="scss">
@import "./styles";
:host {
  display: block;
}
</style>
