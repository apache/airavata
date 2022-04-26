<template>
  <b-form-group
    :label="extendedUserProfileField.name"
    :description="extendedUserProfileField.help_text"
  >
    <b-form-checkbox v-model="value" :unchecked-value="false">
      {{ extendedUserProfileField.checkbox_label }}
    </b-form-checkbox>
  </b-form-group>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
export default {
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

<style></style>
