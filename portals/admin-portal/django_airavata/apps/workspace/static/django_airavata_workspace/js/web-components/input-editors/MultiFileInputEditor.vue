<template>
  <!-- NOTE: experimentInput is late bound, don't create component until it is available -->
  <div>
    <multi-file-input-editor
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
import MultiFileInputEditor from "../../components/experiment/input-editors/MultiFileInputEditor.vue";
import WebComponentInputEditorMixin from "./WebComponentInputEditorMixin.js";

export default {
  mixins: [WebComponentInputEditorMixin],
  props: {
    // Explicit copy props from mixin, workaround for bug, see
    // https://github.com/vuejs/vue-web-component-wrapper/issues/30#issuecomment-427350734
    // for more details
    ...WebComponentInputEditorMixin.props,
  },
  components: {
    MultiFileInputEditor,
  },
};
</script>

<style lang="scss">
@import "../styles";
@import "~@uppy/core/dist/style.min.css";
@import "~@uppy/status-bar/dist/style.min.css";
@import "~@uppy/drag-drop/dist/style.min.css";
@import "~codemirror/lib/codemirror.css";
@import "~codemirror/theme/abcdef.css";
:host {
  display: block;
}
</style>
