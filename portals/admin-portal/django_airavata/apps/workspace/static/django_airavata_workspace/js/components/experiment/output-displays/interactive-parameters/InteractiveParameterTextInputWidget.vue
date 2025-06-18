<template>
  <b-input-group>
    <b-form-input
      ref="textInput"
      :value="value"
      @input="currentValue = $event"
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
  name: "interactive-parameter-text-input-widget",
  props: {
    value: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      currentValue: this.value,
    };
  },
  computed: {
    disabled() {
      return this.currentValue === this.value;
    },
  },
  methods: {
    submit() {
      this.$emit("input", this.currentValue);
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
