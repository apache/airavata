<template>
  <b-card header="User Profile">
    <b-table :items="items" :fields="fields" small borderless>
      <template #cell(value)="{ value, item }">
        <i v-if="item.valid" class="fas fa-check text-success"></i>
        <i v-if="!item.valid" class="fas fa-times text-danger"></i>
        {{ value }}
      </template>
    </b-table>
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
export default {
  props: {
    iamUserProfile: {
      type: models.IAMUserProfile,
      required: true,
    },
  },
  computed: {
    fields() {
      return ["name", "value"];
    },
    items() {
      if (!this.iamUserProfile) {
        return [];
      } else {
        return [
          {
            name: "Username",
            value: this.iamUserProfile.userId,
            valid: this.isValid("username"),
          },
          {
            name: "Email",
            value: this.iamUserProfile.email,
            valid: this.isValid("email"),
          },
          {
            name: "First Name",
            value: this.iamUserProfile.firstName,
            valid: this.isValid("first_name"),
          },
          {
            name: "Last Name",
            value: this.iamUserProfile.lastName,
            valid: this.isValid("last_name"),
          },
        ];
      }
    },
  },
  methods: {
    isValid(fieldName) {
      return (
        this.iamUserProfile.userProfileInvalidFields.indexOf(fieldName) < 0
      );
    },
  },
};
</script>

<style></style>
