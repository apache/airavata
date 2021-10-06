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
    this.initializeSliderValue();
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
    initializeSliderValue() {
      this.sliderValue = this.parseValue(this.data);
      // If parsing the value resulted in it changing (failed to parse so
      // initialized to the 'min'), update the value
      if (this.data !== this.formatValue(this.sliderValue)) {
        this.onChange(this.sliderValue);
      }
    },
    parseValue(value) {
      // Just remove any percentage signs
      const result = parseInt(value ? value.replaceAll("%", "") : null);
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
    data() {
      this.initializeSliderValue();
    },
  },
};
</script>
