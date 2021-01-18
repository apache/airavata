<template>
  <b-button
    ref="copyButton"
    :variant="variant"
    :disabled="disabled"
    :data-clipboard-text="text"
  >
    <slot></slot>
    <slot name="icon">
      <i class="far fa-clipboard"></i>
    </slot>
    <b-tooltip :show="show" :disabled="!show" :target="() => $refs.copyButton">
      <slot name="tooltip">Copied!</slot>
    </b-tooltip>
  </b-button>
</template>

<script>
import ClipboardJS from "clipboard";

export default {
  name: "clipboard-copy-button",
  props: {
    text: {
      type: String,
    },
    variant: {
      type: String,
      default: "secondary",
    },
  },
  data() {
    return {
      show: false,
    };
  },
  computed: {
    disabled() {
      return !this.text;
    },
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
    onCopySuccess() {
      this.show = true;
      setTimeout(() => (this.show = false), 2000);
    },
  },
};
</script>
