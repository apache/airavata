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
      required: true,
    },
    parameter: {
      type: Object,
    },
  },
  data() {
    return {
      currentValue: parseFloat(this.value),
      valid: false,
    };
  },
  computed: {
    disabled() {
      return !this.valid || this.currentValue === parseFloat(this.value);
    },
  },
  methods: {
    updateValue(newValue) {
      if ("max" in this.parameter) {
        newValue = Math.min(this.parameter.max, newValue);
      }
      if ("min" in this.parameter) {
        newValue = Math.max(this.parameter.min, newValue);
      }
      this.currentValue = parseFloat(newValue);
      if (this.$refs.textInput.validity.valid) {
        this.valid = true;
        this.$emit("valid");
      } else {
        this.valid = false;
        this.$emit("invalid", this.$refs.textInput.validationMessage);
      }
    },
    submit() {
      if (!this.disabled) {
        this.$emit("input", this.currentValue);
      }
    },
    enterKeyPressed() {
      if (!this.disabled) {
        this.$refs.textInput.blur();
        this.submit();
      }
    },
  },
};
</script>
