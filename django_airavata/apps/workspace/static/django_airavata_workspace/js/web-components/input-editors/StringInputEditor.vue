<template>
  <!-- NOTE: experimentInput is late bound, don't create component until it is available -->
  <div>
    <string-input-editor
      v-if="experimentInput"
      :id="id"
      :value="data"
      :experiment-input="experimentInput"
      :read-only="readOnly"
      @input="valueChanged"
    />
  </div>
</template>

<script>
import StringInputEditor from "../../components/experiment/input-editors/StringInputEditor.vue";
import WebComponentInputEditorMixin from "./WebComponentInputEditorMixin.js";

export default {
  mixins: [WebComponentInputEditorMixin],
  props: {
    ...WebComponentInputEditorMixin.props,
  },
  components: {
    StringInputEditor,
  },
};
</script>

<style>
@import "../styles.css";
</style>
