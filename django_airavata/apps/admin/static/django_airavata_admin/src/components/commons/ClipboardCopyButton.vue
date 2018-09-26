<template>
  <b-button ref="copyButton" :variant="variant" :disabled="disabled" :data-clipboard-text="text">
    <slot></slot>
    <font-awesome-icon :icon="['far','clipboard']"></font-awesome-icon>
    <b-tooltip :show="show" :disabled="!show" :target="() => $refs.copyButton">Copied!</b-tooltip>
  </b-button>
</template>

<script>
import ClipboardJS from "clipboard";

export default {
  name: "clipboard-copy-button",
  props: {
    text: {
      type: String,
      required: true
    },
    variant: {
      type: String,
      default: "secondary"
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      show: false
    };
  },
  mounted() {
    let clipboard = new ClipboardJS(this.$refs.copyButton);
    clipboard.on("success", this.onCopySuccess);
  },
  beforeDestroy() {
    let clipboard = new ClipboardJS(this.$refs.copyButton);
    clipboard.destroy();
  },
  methods: {
    onCopySuccess(e) {
      this.show = true;
      setTimeout(() => (this.show = false), 2000);
    }
  }
};
</script>

