<template>
  <b-form-group label="Compute Resource" label-for="compute-resource">
    <b-form-select
      id="compute-resource"
      v-model="resourceHostId"
      :options="computeResourceOptions"
      required
      @input="computeResourceChanged"
      @input.native.stop
      :disabled="disabled || computeResourceOptions.length === 0"
    >
      <template slot="first">
        <option :value="null" disabled>Select a Compute Resource</option>
      </template>
    </b-form-select>
  </b-form-group>
</template>

<script>
import store from "./store";
import { mapGetters } from "vuex";

export default {
  name: "compute-resource-selector",
  props: {
    value: {
      // compute resource host id
      type: String,
      default: null,
    },
    includedComputeResources: {
      type: Array,
      default: null,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  store: store,
  data() {
    return {
      resourceHostId: this.value,
    };
  },
  created() {
    this.$store.dispatch("loadComputeResourceNames");
  },
  computed: {
    computeResourceOptions: function () {
      const computeResourceIds = Object.keys(this.computeResourceNames).filter(
        (crid) => {
          if (this.includedComputeResources) {
            return this.includedComputeResources.includes(crid);
          } else {
            return true;
          }
        }
      );
      const computeResourceOptions = computeResourceIds.map((computeHostId) => {
        return {
          value: computeHostId,
          text:
            computeHostId in this.computeResourceNames
              ? this.computeResourceNames[computeHostId]
              : "",
        };
      });
      computeResourceOptions.sort((a, b) => a.text.localeCompare(b.text));
      return computeResourceOptions;
    },
    ...mapGetters(["computeResourceNames"]),
  },
  methods: {
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

<style lang="scss">
@import "./styles";
:host {
  display: block;
}
</style>
