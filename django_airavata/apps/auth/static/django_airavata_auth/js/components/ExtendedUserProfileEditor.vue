<template>
  <b-card>
    <template v-for="extendedUserProfileField in extendedUserProfileFields">
      <component
        :key="extendedUserProfileField.id"
        :is="getEditor(extendedUserProfileField)"
        :extended-user-profile-field="extendedUserProfileField"
      />
    </template>
  </b-card>
</template>

<script>
import { mapGetters } from "vuex";
import ExtendedUserProfileMultiChoiceFieldEditor from "./ExtendedUserProfileMultiChoiceFieldEditor.vue";
import ExtendedUserProfileSingleChoiceFieldEditor from "./ExtendedUserProfileSingleChoiceFieldEditor.vue";
import ExtendedUserProfileTextFieldEditor from "./ExtendedUserProfileTextFieldEditor.vue";
import ExtendedUserProfileUserAgreementFieldEditor from "./ExtendedUserProfileUserAgreementFieldEditor.vue";
export default {
  computed: {
    ...mapGetters("extendedUserProfile", ["extendedUserProfileFields"]),
  },
  methods: {
    getEditor(extendedUserProfileField) {
      const fieldTypeEditors = {
        text: ExtendedUserProfileTextFieldEditor,
        single_choice: ExtendedUserProfileSingleChoiceFieldEditor,
        multi_choice: ExtendedUserProfileMultiChoiceFieldEditor,
        user_agreement: ExtendedUserProfileUserAgreementFieldEditor,
      };

      if (extendedUserProfileField.field_type in fieldTypeEditors) {
        return fieldTypeEditors[extendedUserProfileField.field_type];
      } else {
        // eslint-disable-next-line no-console
        console.error(
          "Unexpected field_type",
          extendedUserProfileField.field_type
        );
      }
    },
  },
};
</script>

<style></style>
