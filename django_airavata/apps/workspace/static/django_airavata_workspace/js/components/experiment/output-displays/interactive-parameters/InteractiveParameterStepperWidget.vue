<template>
  <b-input-group>
    <b-form-input
      ref="textInput"
      type="number"
      :value="value"
      :min="parameter.min"
      :max="parameter.max"
      :step="parameter.step || 'any'"
      @input="updateValue"
      @keydown.native.enter="enterKeyPressed"
    />
    <b-input-group-append>
      <b-button variant="primary" :disabled="disabled" @click="submit"
        >Submit</b-button
      >
    </b-input-group-append>
  </b-input-group>
</template>

<script>
export default {
  name: "interactive-parameter-stepper-widget",
  props: {
    value: {
      type: Number,
      required: true
    },
    parameter: {
      type: Object
    }
  },
  data() {
    return {
      currentValue: parseFloat(this.value)
    };
  },
  computed: {
    disabled() {
      return this.currentValue === parseFloat(this.value);
    }
  },
  methods: {
    updateValue(newValue) {
      this.currentValue = parseFloat(newValue);
    },
    submit() {
      this.$emit("input", this.currentValue);
    },
    enterKeyPressed() {
      if (!this.disabled) {
        this.$refs.textInput.blur();
        this.submit();
      }
    }
  }
};
</script>
