<template>
  <b-form-select
    :value="value"
    :options="options"
    @input="$emit('input', $event)"
  />
</template>

<script>
export default {
  name: "interactive-parameter-select-widget",
  props: {
    value: {
      type: String,
      required: true,
    },
    parameter: {
      type: Object,
      required: true,
    },
  },
  computed: {
    options() {
      return this.parameter.options;
    },
  },
};
</script>
