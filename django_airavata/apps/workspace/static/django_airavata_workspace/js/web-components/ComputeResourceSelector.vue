<template>
  <b-form-group label="Compute Resource" label-for="compute-resource">
    <b-form-select
      id="compute-resource"
      v-model="resourceHostId"
      :options="computeResourceOptions"
      required
      @input="computeResourceChanged"
    >
      <template slot="first">
        <option :value="null" disabled>Select a Compute Resource</option>
      </template>
    </b-form-select>
  </b-form-group>
</template>

<script>
import { getComputeResourceNames } from "./store";
export default {
  name: "compute-resource-selector",
  props: {
    value: {
      // compute resource host id
      type: String,
      default: null,
    },
    computeResources: {
      type: Array, // of compute resource host ids
      default: () => [],
    },
  },
  data() {
    return {
      resourceHostId: this.value,
      computeResourceNames: {},
    };
  },
  created() {
    this.loadComputeResourceNames();
  },
  computed: {
    computeResourceOptions: function () {
      const computeResourceOptions = this.computeResources.map(
        (computeHostId) => {
          return {
            value: computeHostId,
            text:
              computeHostId in this.computeResourceNames
                ? this.computeResourceNames[computeHostId]
                : "",
          };
        }
      );
      computeResourceOptions.sort((a, b) => a.text.localeCompare(b.text));
      return computeResourceOptions;
    },
  },
  methods: {
    async loadComputeResourceNames() {
      this.computeResourceNames = await getComputeResourceNames();
    },
    computeResourceChanged() {
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
