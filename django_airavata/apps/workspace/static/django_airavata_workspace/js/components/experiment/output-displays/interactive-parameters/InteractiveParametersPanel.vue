<template>
  <b-card title="Parameters">
    <b-form-group
      v-for="param in parameters"
      :key="param.name"
      :label="param.name"
    >
      <!-- TODO: use dynamic components to pick the right widget for the type of parameter -->
      <interactive-parameter-checkbox-widget
        :value="param.value"
        @input="updated(param, $event)"
      />
    </b-form-group>
  </b-card>
</template>

<script>
import InteractiveParameterCheckboxWidget from "./InteractiveParameterCheckboxWidget";
export default {
  name: "interactive-parameters-panel",
  components: {
    InteractiveParameterCheckboxWidget
  },
  props: {
    parameters: {
      type: Array,
      required: true
    }
  },
  methods: {
    updated(param, value) {
      const params = this.parametersCopy();
      const i = params.findIndex(x => x.name === param.name);
      params[i].value = value;
      this.$emit("input", params);
    },
    parametersCopy() {
      return JSON.parse(JSON.stringify(this.parameters));
    }
  }
};
</script>
