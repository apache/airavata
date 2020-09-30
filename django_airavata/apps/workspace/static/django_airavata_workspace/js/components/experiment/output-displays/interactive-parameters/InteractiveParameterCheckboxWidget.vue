<template>
  <b-form-checkbox :checked="value" @input="$emit('input', $event)" />
</template>

<script>
export default {
  name: "interactive-parameter-checkbox-widget",
  props: {
    value: {
      type: Boolean,
      required: true,
    },
  },
};
</script>
