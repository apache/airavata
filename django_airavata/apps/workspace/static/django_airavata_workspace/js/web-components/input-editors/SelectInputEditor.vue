<template>
  <div>
    <select-input-editor
      v-if="experimentInput"
      :id="id"
      :value="data"
      :experiment-input="experimentInput"
      :read-only="readOnly"
      :options="allOptions"
      @input="valueChanged"
    >
    </select-input-editor>
    <div ref="optionsSlot" class="options-slot"></div>
  </div>
</template>

<script>
import SelectInputEditor from "../../components/experiment/input-editors/SelectInputEditor.vue";
import WebComponentInputEditorMixin from "./WebComponentInputEditorMixin.js";
import InlineOptionsMixin from "./InlineOptionsMixin.js";

export default {
  mixins: [WebComponentInputEditorMixin, InlineOptionsMixin],
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
:host .options-slot {
  display: none;
}
</style>
