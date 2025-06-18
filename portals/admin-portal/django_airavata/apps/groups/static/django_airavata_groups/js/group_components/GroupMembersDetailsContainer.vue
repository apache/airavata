<template>
<div>
    <b-card header="Details">
      <b>Name: </b> {{name}}<br>
      <b>Email: </b> {{userProfile.email}}<br>
      
      <span v-if="role"><b>Role: </b></span>
      <b-form-select
          v-if="isOwner && role !== 'OWNER'"
          :value="role"
          @input="changeRole($event)"
          :options="groupRoleOptions"
        >
      </b-form-select>
      <span v-if="(!isOwner && role) || (isOwner && role=='OWNER')">{{ role }}</span>

    </b-card>
    
</div>
</template>

<script>

import { models } from "django-airavata-api";
//GroupMembersDetailsContainer
export default {
 name: "group-members-details-container",
  props: {
    userProfile: {
      type: models.userProfile,
      required: true,
    },
    name: {
      type: String,
      required: true,
    },
    role: {
      type: String,
      required: false,
    },
    isOwner: {
      type: Boolean,
      required: false,
      default: false,
    },
    id: {
      type: String,
      required: true,
    },
  },
  
  methods: {
    changeRole(role) {
      this.$emit("change-role", [this.id, role]);
    },
  },
  computed: {
    groupRoleOptions() {
      return [
        {
          value: "MEMBER",
          text: "MEMBER",
        },
        {
          value: "ADMIN",
          text: "ADMIN",
        },
      ];
    },
  },
};
</script>