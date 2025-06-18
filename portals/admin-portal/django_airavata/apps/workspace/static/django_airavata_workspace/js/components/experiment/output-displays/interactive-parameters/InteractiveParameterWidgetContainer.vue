<template>
  <compoment
    :is="widgetComponent"
    :value="parameter.value"
    :parameter="parameter"
    @input="$emit('input', $event)"
    @valid="$emit('valid')"
    @invalid="$emit('invalid', $event)"
  />
</template>

<script>
import InteractiveParameterCheckboxWidget from "./InteractiveParameterCheckboxWidget.vue";
import InteractiveParameterRangeWidget from "./InteractiveParameterRangeWidget.vue";
import InteractiveParameterSelectWidget from "./InteractiveParameterSelectWidget.vue";
import InteractiveParameterStepperWidget from "./InteractiveParameterStepperWidget.vue";
import InteractiveParameterTextInputWidget from "./InteractiveParameterTextInputWidget.vue";

export default {
  name: "interactive-parameter-widget-container",
  props: {
    parameter: {
      type: Object,
      required: true,
    },
  },
  components: {
    InteractiveParameterCheckboxWidget,
    InteractiveParameterRangeWidget,
    InteractiveParameterSelectWidget,
    InteractiveParameterStepperWidget,
    InteractiveParameterTextInputWidget,
  },
  computed: {
    widgetComponent() {
      if (this.parameter.options) {
        return InteractiveParameterSelectWidget;
      } else if (
        this.parameter.type === "boolean" ||
        (this.parameter.widget && this.parameter.widget === "checkbox")
      ) {
        return InteractiveParameterCheckboxWidget;
      } else if (
        this.parameter.type === "string" ||
        (this.parameter.widget && this.parameter.widget === "textinput")
      ) {
        return InteractiveParameterTextInputWidget;
      } else if (
        (this.parameter.type === "float" ||
          this.parameter.type === "integer") &&
        "min" in this.parameter &&
        "max" in this.parameter
      ) {
        return InteractiveParameterRangeWidget;
      } else if (
        this.parameter.type === "float" ||
        this.parameter.type === "integer" ||
        (this.parameter.widget && this.parameter.widget === "stepper")
      ) {
        return InteractiveParameterStepperWidget;
      } else {
        return null;
      }
    },
  },
};
</script>
