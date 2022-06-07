<template>
  <b-card header="Extended User Profile">
    <b-table :items="items" :fields="fields" small borderless>
      <template #cell(value)="{ value, item }">
        <i v-if="item.valid" class="fas fa-check text-success"></i>
        <i v-if="!item.valid" class="fas fa-times text-danger"></i>
        <template v-if="item.type === 'text' || item.type === 'single_choice'">
          {{ value }}
        </template>
        <template v-else-if="item.type === 'user_agreement'">
          <b-checkbox class="ml-2 d-inline" :checked="value" disabled />
        </template>
        <template v-else-if="item.type === 'multi_choice'">
          <ul>
            <li v-for="result in item.value" :key="result">
              {{ result }}
            </li>
          </ul>
        </template>
      </template>
    </b-table>
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import { mapActions, mapGetters } from "vuex";
export default {
  props: {
    iamUserProfile: {
      type: models.IAMUserProfile,
      required: true,
    },
  },
  created() {
    this.loadExtendedUserProfileFields();
    this.loadExtendedUserProfileValues({
      username: this.iamUserProfile.userId,
    });
  },
  computed: {
    ...mapGetters("extendedUserProfile", [
      "extendedUserProfileFields",
      "extendedUserProfileValues",
    ]),
    fields() {
      return ["name", "value"];
    },
    items() {
      if (this.extendedUserProfileFields && this.extendedUserProfileValues) {
        const items = [];
        for (const field of this.extendedUserProfileFields) {
          const { value, valid } = this.getValue({ field });
          items.push({
            name: field.name,
            value,
            valid,
            type: field.field_type,
          });
        }
        return items;
      } else {
        return [];
      }
    },
  },
  methods: {
    ...mapActions("extendedUserProfile", [
      "loadExtendedUserProfileFields",
      "loadExtendedUserProfileValues",
    ]),
    getValue({ field }) {
      if (!this.extendedUserProfileValues) {
        return null;
      }
      const value = this.extendedUserProfileValues.find(
        (v) => v.ext_user_profile_field === field.id
      );
      if (value && value.value_type === "text") {
        return { value: value.text_value, valid: value.valid };
      }
      if (value && value.value_type === "user_agreement") {
        return { value: value.user_agreement, valid: value.valid };
      }
      if (value && value.value_type === "single_choice") {
        if (value.other_value) {
          return { value: `Other: ${value.other_value}`, valid: value.valid };
        } else if (value.choices && value.choices.length === 1) {
          const displayText = this.getChoiceDisplayText({
            field,
            choiceId: value.choices[0],
          });
          if (displayText) {
            return { value: displayText, valid: value.valid };
          }
        }
        return { value: null, valid: value.valid };
      }
      if (value && value.value_type === "multi_choice") {
        const results = [];
        if (value.choices) {
          for (const choiceId of value.choices) {
            const displayText = this.getChoiceDisplayText({ field, choiceId });
            if (displayText) {
              results.push(displayText);
            }
          }
        }
        if (value.other_value) {
          results.push(`Other: ${value.other_value}`);
        }
        return { value: results, valid: value.valid };
      }
      return { value: null, valid: !field.required };
    },
    getChoiceDisplayText({ field, choiceId }) {
      const choice = field.choices.find((c) => c.id === choiceId);
      return choice ? choice.display_text : null;
    },
  },
};
</script>

<style scoped>
ul {
  display: inline-block;
  padding-left: 20px;
}
</style>
