<template>
  <b-modal
    title="New SSH Credential"
    ref="modal"
    @ok="okClicked"
    ok-title="Create"
    :ok-disabled="!valid"
  >
    <b-form-input
      type="text"
      placeholder="Description"
      required
      v-model="description"
    />
  </b-modal>
</template>

<script>
export default {
  name: "new-ssh-credential-modal",
  data() {
    return {
      description: null,
    };
  },
  computed: {
    valid() {
      return this.description != null && this.description.trim() !== "";
    },
  },
  methods: {
    okClicked() {
      this.$emit("new", { description: this.description });
    },
    show() {
      this.$refs.modal.show();
    },
  },
};
</script>
