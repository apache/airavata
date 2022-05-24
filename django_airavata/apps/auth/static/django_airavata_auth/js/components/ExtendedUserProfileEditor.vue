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
import ExtendedUserProfileMultiChoiceValueEditor from "./ExtendedUserProfileMultiChoiceValueEditor.vue";
import ExtendedUserProfileSingleChoiceValueEditor from "./ExtendedUserProfileSingleChoiceValueEditor.vue";
import ExtendedUserProfileTextValueEditor from "./ExtendedUserProfileTextValueEditor.vue";
import ExtendedUserProfileUserAgreementValueEditor from "./ExtendedUserProfileUserAgreementValueEditor.vue";
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
        text: ExtendedUserProfileTextValueEditor,
        single_choice: ExtendedUserProfileSingleChoiceValueEditor,
        multi_choice: ExtendedUserProfileMultiChoiceValueEditor,
        user_agreement: ExtendedUserProfileUserAgreementValueEditor,
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
