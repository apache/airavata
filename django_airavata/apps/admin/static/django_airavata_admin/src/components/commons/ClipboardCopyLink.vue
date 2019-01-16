<template>
  <div style="display: inline-block">
    <b-link ref="copyLink" :data-clipboard-text="text">
      Copy Key
      <i class="far fa-clipboard"></i>
    </b-link>
    <b-tooltip :show="show" :disabled="!show" :target="() => $refs.copyLink">Copied!</b-tooltip>
  </div>
</template>

<script>
import ClipboardJS from "clipboard";

export default {
  name: "clipboard-copy-link",
  props: {
    text: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      show: false
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
    }
  }
};
</script>

