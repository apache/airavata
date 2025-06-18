<template>
  <b-form-textarea
    :id="id"
    v-model="jsonString"
    @input="valueChanged"
    :rows="rows"
    :disabled="disabled"
    :state="state"
  />
</template>

<script>
export default {
  name: "json-editor",
  props: {
    value: {
      type: Object,
    },
    id: String,
    rows: Number,
    disabled: Boolean,
  },
  data() {
    return {
      jsonString: this.value ? this.formatJSON(this.value) : null,
      state: null,
    };
  },
  methods: {
    formatJSON(value) {
      return JSON.stringify(value, null, 4);
    },
    valueChanged(newValue) {
      try {
        if (newValue) {
          const parsedValue = JSON.parse(newValue);
          this.$emit("input", parsedValue);
        } else {
          this.$emit("input", null);
        }
        this.state = true;
      } catch (e) {
        this.state = false;
      }
    },
  },
  watch: {
    value(newValue) {
      this.jsonString = newValue ? this.formatJSON(newValue) : null;
    },
  },
};
</script>
