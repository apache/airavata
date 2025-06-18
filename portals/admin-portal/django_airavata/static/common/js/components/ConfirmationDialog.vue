<template>
  <b-modal
    :title="title"
    ref="modal"
    @ok="$emit('ok')"
    @cancel="$emit('cancel')"
    no-close-on-backdrop
    no-close-on-esc
    hide-header-close
    ok-title="Confirm"
    ok-variant="danger"
  >
    <slot></slot>
  </b-modal>
</template>

<script>
export default {
  name: "confirmation-dialog",
  props: {
    title: {
      type: String,
      default: "Please confirm",
    },
  },
  methods: {
    show() {
      this.$refs.modal.show();
    },
    hide() {
      this.$refs.modal.hide();
    },
  },
};
</script>
