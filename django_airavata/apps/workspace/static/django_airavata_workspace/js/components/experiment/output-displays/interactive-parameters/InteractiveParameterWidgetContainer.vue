<template>
  <compoment :is="widgetComponent"
    :value="parameter.value"
    :parameter="parameter"
    @input="$emit('input', $event)"
  />
</template>

<script>
import InteractiveParameterCheckboxWidget from "./InteractiveParameterCheckboxWidget";
import InteractiveParameterSelectWidget from "./InteractiveParameterSelectWidget";

export default {
  name: "interactive-parameter-widget-container",
  props: {
    parameter: {
      type: Object,
      required: true
    }
  },
  components: {
    InteractiveParameterCheckboxWidget,
    InteractiveParameterSelectWidget
  },
  computed: {
    widgetComponent() {
      if (this.parameter.options) {
        return InteractiveParameterSelectWidget;
      } else if (typeof this.parameter.value === "boolean") {
        return InteractiveParameterCheckboxWidget;
      } else {
        return null;
      }
    }
  }
};
</script>

