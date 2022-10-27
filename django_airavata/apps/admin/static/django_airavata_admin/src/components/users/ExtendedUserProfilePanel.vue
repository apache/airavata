<template>
  <b-card header="Extended User Profile">
    <template v-if="items.length === 0">
      <b-link href="/admin/extended-user-profile"
        >Add additional user profile fields for gateway users to
        complete</b-link
      >
    </template>
    <b-table v-else :items="items" :fields="fields" small borderless>
      <template #cell(value)="{ value, item }">
        <!-- only show a valid checkmark when there is a user provided value -->
        <i v-if="value && item.valid" class="fas fa-check text-success"></i>
        <i v-if="!item.valid" class="fas fa-times text-danger"></i>
        <template v-if="Array.isArray(value)">
          <ul>
            <li v-for="result in value" :key="result">
              {{ result }}
            </li>
          </ul>
        </template>
        <template v-else> {{ value }} </template>
      </template>
    </b-table>
    <b-link
      v-if="items.length > 0"
      href="/admin/extended-user-profile"
      class="text-muted small"
      >Add or edit these field definitions</b-link
    >
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
          const value = this.getValue(field);
          items.push({
            name: field.name,
            value: value ? value.value_display : null,
            // if no value, consider it invalid only if it is required
            valid: value ? value.valid : !field.required,
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
    getValue(field) {
      return this.extendedUserProfileValues.find(
        (v) => v.ext_user_profile_field === field.id
      );
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
