<template>
  <div>
    <template v-for="extendedUserProfileField in extendedUserProfileFields">
      <component
        ref="extendedUserProfileFieldComponents"
        :key="extendedUserProfileField.id"
        :is="getEditor(extendedUserProfileField)"
        :extended-user-profile-field="extendedUserProfileField"
        @valid="recordValidChildComponent(extendedUserProfileField.id)"
        @invalid="recordInvalidChildComponent(extendedUserProfileField.id)"
      />
    </template>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import ExtendedUserProfileMultiChoiceFieldEditor from "./ExtendedUserProfileMultiChoiceFieldEditor.vue";
import ExtendedUserProfileSingleChoiceFieldEditor from "./ExtendedUserProfileSingleChoiceFieldEditor.vue";
import ExtendedUserProfileTextFieldEditor from "./ExtendedUserProfileTextFieldEditor.vue";
import ExtendedUserProfileUserAgreementFieldEditor from "./ExtendedUserProfileUserAgreementFieldEditor.vue";
import { mixins } from "django-airavata-common-ui";
export default {
  mixins: [mixins.ValidationParent],
  computed: {
    ...mapGetters("extendedUserProfile", ["extendedUserProfileFields"]),
    valid() {
      return this.childComponentsAreValid;
    },
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
    touch() {
      this.$refs.extendedUserProfileFieldComponents.forEach((c) => c.touch());
    },
  },
};
</script>

<style></style>
