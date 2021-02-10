<template>
  <vue-slider
    v-model="sliderValue"
    @change="onChange"
    :state="componentValidState"
    :disabled="readOnly"
    :min="min"
    :max="max"
    :interval="step"
    tooltip="always"
    :tooltip-formatter="tooltipFormatter"
  />
</template>

<script>
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";
import VueSlider from "vue-slider-component";

export default {
  name: "slider-input-editor",
  mixins: [InputEditorMixin],
  props: {
    value: {
      type: String,
    },
  },
  components: {
    VueSlider,
  },
  data() {
    return {
      sliderValue: null,
    };
  },
  created() {
    // computed properties are only available at *created* step of lifecycle
    this.sliderValue = this.parseValue(this.value);
  },
  computed: {
    min: function () {
      return "min" in this.editorConfig ? this.editorConfig.min : 0;
    },
    max: function () {
      return "max" in this.editorConfig ? this.editorConfig.max : 100;
    },
    step: function () {
      return "step" in this.editorConfig ? this.editorConfig.step : 1;
    },
  },
  methods: {
    parseValue(value) {
      // Just remove any percentage signs
      const result = parseInt(value.replaceAll("%", ""));
      return !isNaN(result) ? result : this.min;
    },
    onChange(value) {
      this.data = this.formatValue(value);
      this.valueChanged();
    },
    tooltipFormatter(value) {
      if ("displayFormat" in this.editorConfig) {
        if (this.editorConfig.displayFormat.percentage) {
          return `${value}%`;
        }
      }
      return value;
    },
    formatValue(value) {
      if ("valueFormat" in this.editorConfig) {
        if (this.editorConfig.valueFormat.percentage) {
          return `${value}%`;
        }
      }
      return String(value);
    },
  },
  watch: {
    data(newValue) {
      this.sliderValue = this.parseValue(newValue);
    },
  },
};
</script>
