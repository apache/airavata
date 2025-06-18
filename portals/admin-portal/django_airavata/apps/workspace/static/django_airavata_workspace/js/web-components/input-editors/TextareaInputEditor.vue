<template>
  <!-- NOTE: experimentInput is late bound, don't create component until it is available -->
  <!-- Important that input editor is wrapped in div. Input editor stops
  propagation on native input events, but we need for this component's input
  events to propagate. So the input editor should not be the root component. -->
  <div>
    <textarea-input-editor
      v-if="experimentInput"
      :id="id"
      :value="data"
      :experiment-input="experimentInput"
      :read-only="readOnly"
      :rows="rows"
      @input="valueChanged"
    />
  </div>
</template>

<script>
import TextareaInputEditor from "../../components/experiment/input-editors/TextareaInputEditor.vue";
import WebComponentInputEditorMixin from "./WebComponentInputEditorMixin.js";

export default {
  mixins: [WebComponentInputEditorMixin],
  props: {
    // Explicit copy props from mixin, workaround for bug, see
    // https://github.com/vuejs/vue-web-component-wrapper/issues/30#issuecomment-427350734
    // for more details
    ...WebComponentInputEditorMixin.props,
    rows: {
      type: Number,
    },
  },
  components: {
    TextareaInputEditor,
  },
};
</script>

<style lang="scss">
@import "../styles";
:host {
  display: block;
}
</style>
