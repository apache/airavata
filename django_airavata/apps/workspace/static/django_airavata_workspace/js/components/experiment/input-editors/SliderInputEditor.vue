<template>
  <vue-slider
    v-model="sliderValue"
    @change="onChange"
    :state="componentValidState"
    :disabled="readOnly"
    :min="sliderMin"
    :max="sliderMax"
    :interval="sliderStep"
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
    min: Number,
    max: Number,
    step: Number,
    valueFormat: {
      type: String,
      validator(value) {
        return ["percentage"].indexOf(value) !== -1;
      },
    },
    displayFormat: {
      type: String,
      validator(value) {
        return ["percentage"].indexOf(value) !== -1;
      },
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
    sliderMin: function () {
      return typeof this.min !== "undefined"
        ? this.min
        : "min" in this.editorConfig
        ? this.editorConfig.min
        : 0;
    },
    sliderMax: function () {
      return typeof this.max !== "undefined"
        ? this.max
        : "max" in this.editorConfig
        ? this.editorConfig.max
        : 100;
    },
    sliderStep: function () {
      return typeof this.step !== "undefined"
        ? this.step
        : "step" in this.editorConfig
        ? this.editorConfig.step
        : 1;
    },
  },
  methods: {
    initializeSliderValue() {
      this.sliderValue = this.parseValue(this.data);
      // If parsing the value resulted in it changing (failed to parse so
      // initialized to the 'sliderMin'), update the value
      if (this.data !== this.formatValue(this.sliderValue)) {
        this.onChange(this.sliderValue);
      }
    },
    parseValue(value) {
      // Just remove any percentage signs
      const result = value ? parseFloat(value.replaceAll("%", "")) : NaN;
      return !isNaN(result) ? result : this.sliderMin;
    },
    onChange(value) {
      this.data = this.formatValue(value);
      this.valueChanged();
    },
    tooltipFormatter(value) {
      if (this.displayFormat) {
        if (this.displayFormat === "percentage") {
          return `${value}%`;
        }
      } else if ("displayFormat" in this.editorConfig) {
        if (this.editorConfig.displayFormat.percentage) {
          return `${value}%`;
        }
      }
      return value;
    },
    formatValue(value) {
      if (this.valueFormat) {
        if (this.valueFormat === "percentage") {
          return `${value}%`;
        }
      } else if ("valueFormat" in this.editorConfig) {
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
