<template>
  <select-input-editor
    v-if="experimentInput"
    :id="id"
    :value="data"
    :experiment-input="experimentInput"
    :read-only="readOnly"
    :options="options"
    @input="valueChanged"
  />
</template>

<script>
import SelectInputEditor from "../../components/experiment/input-editors/SelectInputEditor.vue";
import WebComponentInputEditorMixin from "./WebComponentInputEditorMixin.js";

export default {
  mixins: [WebComponentInputEditorMixin],
  props: {
    // Explicit copy props from mixin, workaround for bug, see
    // https://github.com/vuejs/vue-web-component-wrapper/issues/30#issuecomment-427350734
    // for more details
    ...WebComponentInputEditorMixin.props,
    options: {
      type: Array,
      default: null,
    },
  },
  components: {
    SelectInputEditor,
  },
};
</script>

<style lang="scss">
@import "../styles";
:host {
  display: block;
}
</style>
