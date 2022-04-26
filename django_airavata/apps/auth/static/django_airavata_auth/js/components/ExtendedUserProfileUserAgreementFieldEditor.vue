<template>
  <extended-user-profile-field-editor v-bind="$props">
    <b-form-checkbox v-model="value" :unchecked-value="false">
      {{ extendedUserProfileField.checkbox_label }}
    </b-form-checkbox>
  </extended-user-profile-field-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import ExtendedUserProfileFieldEditor from "./ExtendedUserProfileFieldEditor.vue";
export default {
  components: { ExtendedUserProfileFieldEditor },
  props: ["extendedUserProfileField"],
  computed: {
    ...mapGetters("extendedUserProfile", ["getUserAgreementValue"]),
    value: {
      get() {
        return this.getUserAgreementValue(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setUserAgreementValue({
          value,
          id: this.extendedUserProfileField.id,
        });
      },
    },
  },
  methods: {
    ...mapMutations("extendedUserProfile", ["setUserAgreementValue"]),
  },
};
</script>
