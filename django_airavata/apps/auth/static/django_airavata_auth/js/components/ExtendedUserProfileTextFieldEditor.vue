<template>
  <extended-user-profile-field-editor v-bind="$props">
    <b-form-input v-model="value" />
  </extended-user-profile-field-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import ExtendedUserProfileFieldEditor from "./ExtendedUserProfileFieldEditor.vue";
export default {
  components: { ExtendedUserProfileFieldEditor },
  props: ["extendedUserProfileField"],
  computed: {
    ...mapGetters("extendedUserProfile", ["getTextValue"]),
    value: {
      get() {
        return this.getTextValue(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setTextValue({ value, id: this.extendedUserProfileField.id });
      },
    },
  },
  methods: {
    ...mapMutations("extendedUserProfile", ["setTextValue"]),
  },
};
</script>

<style></style>
