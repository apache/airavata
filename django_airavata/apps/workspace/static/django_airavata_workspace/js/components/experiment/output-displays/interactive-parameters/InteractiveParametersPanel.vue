<template>
  <b-card title="Parameters">
    <b-form-group
      v-for="param in parameters"
      :key="param.name"
      :label="param.name"
    >
      <interactive-parameter-widget-container
        :parameter="param"
        @input="updated(param, $event)"/>
    </b-form-group>
  </b-card>
</template>

<script>
import InteractiveParameterWidgetContainer from "./InteractiveParameterWidgetContainer";

export default {
  name: "interactive-parameters-panel",
  components: {
    InteractiveParameterWidgetContainer
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
