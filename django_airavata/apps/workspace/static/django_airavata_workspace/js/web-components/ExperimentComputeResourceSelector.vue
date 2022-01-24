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
    applicationModuleId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      initialized: false,
    };
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
    groupResourceProfileId(groupResourceProfileId) {
      if (!this.initialized && groupResourceProfileId) {
        this.$store.dispatch("initializeComputeResources", {
          applicationModuleId: this.applicationModuleId,
          groupResourceProfileId: groupResourceProfileId,
        });
        this.initialized = true;
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
