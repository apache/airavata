<template>
  <b-modal
    title="Select Compute Resource"
    ref="modal"
    @ok="onSelectComputeResource"
    :ok-disabled="modalSelectComputeResourceOkDisabled"
  >
    <b-form-select
      v-model="selectedComputeResource"
      :options="computeResourceOptions"
    >
      <template slot="first">
        <option :value="null">Please select compute resource</option>
      </template>
    </b-form-select>
  </b-modal>
</template>

<script>
import { services } from "django-airavata-api";
export default {
  name: "compute-resources-modal",
  props: {
    computeResourceNames: Array,
    excludedResourceIds: Array,
  },
  data() {
    return {
      selectedComputeResource: null,
      localComputeResourceNames: null,
    };
  },
  created() {
    if (!this.computeResourceNames) {
      services.ComputeResourceService.namesList().then(
        (resourceNames) => (this.localComputeResourceNames = resourceNames)
      );
    }
  },
  computed: {
    modalSelectComputeResourceOkDisabled: function () {
      return this.selectedComputeResource == null;
    },
    computeResourceOptions: function () {
      const names = this.computeResourceNames
        ? this.computeResourceNames
        : this.localComputeResourceNames;
      const options = names
        ? names
            .filter((comp) =>
              this.excludedResourceIds
                ? !this.excludedResourceIds.includes(comp.host_id)
                : true
            )
            .map((comp) => {
              return {
                value: comp.host_id,
                text: comp.host,
              };
            })
        : [];
      options.sort((a, b) =>
        a.text.toLowerCase().localeCompare(b.text.toLowerCase())
      );
      return options;
    },
  },
  methods: {
    onSelectComputeResource() {
      this.$emit("selected", this.selectedComputeResource);
    },
    show() {
      this.$refs.modal.show();
    },
  },
};
</script>
