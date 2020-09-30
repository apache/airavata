<template>
  <div style="display: inline-block;">
    <a
      href="#"
      ref="copyLink"
      :data-clipboard-text="text"
      class="action-link"
      :class="linkClasses"
    >
      <slot>
        Copy Key
      </slot>
      <slot name="icon">
        <i class="far fa-clipboard"></i>
      </slot>
    </a>
    <b-tooltip :show="show" :disabled="!show" :target="() => $refs.copyLink">
      <slot name="tooltip">Copied!</slot>
    </b-tooltip>
  </div>
</template>

<script>
import ClipboardJS from "clipboard";

export default {
  name: "clipboard-copy-link",
  props: {
    text: {
      type: String,
      required: true,
    },
    linkClasses: {
      type: Array,
    },
  },
  data() {
    return {
      show: false,
    };
  },
  mounted() {
    let clipboard = new ClipboardJS(this.$refs.copyLink);
    clipboard.on("success", this.onCopySuccess);
  },
  beforeDestroy() {
    let clipboard = new ClipboardJS(this.$refs.copyLink);
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
