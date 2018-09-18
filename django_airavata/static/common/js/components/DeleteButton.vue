<template>
  <div class="delete-button">
    <b-button variant="danger" @click="$refs.modal.show()" :disabled="disabled">
      Delete
    </b-button>
    <confirmation-dialog ref="modal" :title="dialogTitle" @ok="$emit('delete')">
      <slot></slot>
    </confirmation-dialog>
  </div>
</template>
<script>
import ConfirmationDialog from "./ConfirmationDialog.vue";

export default {
  name: "delete-button",
  props: {
    dialogTitle: {
      type: String,
      default: "Please confirm delete"
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  components: {
    ConfirmationDialog
  }
};
</script>

<style scoped>
.delete-button {
  display: inline-block;
}
</style>



