<template>
  <div>
    <b-alert
      :variant="showDismissibleAlert.variant"
      dismissible
      :show="showDismissibleAlert.dismissable"
      @dismissed="showDismissibleAlert.dismissable = false"
    >
      {{ showDismissibleAlert.message }}
    </b-alert>

    <b-form >
      <b-form-group
        id="group1"
        label="Group Name:"
        label-for="group_name"
        description="Name should only contain Alpha Characters"
      >
        <b-form-input
          id="group_name"
          type="text"
          v-model="localGroup.name"
          required
          placeholder="Enter group name"
        >
        </b-form-input>
      </b-form-group>

      <b-form-group id="group2" label="Description:" label-for="description">
        <b-form-textarea
          id="description"
          type="text"
          :rows="6"
          v-model="localGroup.description"
          required
          placeholder="Enter description of the group"
        >
        </b-form-textarea>
      </b-form-group>

      <b-card title="Manage Group Members" title-tag="h5">
        <group-members-editor
          :group="localGroup"
          @add-member="addGroupMember"
          @remove-member="removeGroupMember"
          @change-role-to-member="changeRoleToMember"
          @change-role-to-admin="changeRoleToAdmin"
        />
      </b-card>
    </b-form>
    <div class="fixed-footer">
      <b-button @click="submitForm" variant="primary">Submit</b-button>  
    </div>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import GroupMembersEditor from "./GroupMembersEditor.vue";

export default {
  props: {
    group: {
      type: models.Group,
      required: true,
    },
  },
  data() {
    return {
      localGroup: this.group.clone(),
      showDismissibleAlert: {
        variant: "success",
        message: "no data",
        dismissable: false,
      },
      userProfiles: [],
    };
  },
  components: {
    GroupMembersEditor,
  },
  methods: {
    submitForm() {
      let saveOperation = this.localGroup.id
        ? services.GroupService.update({
            lookup: this.localGroup.id,
            data: this.localGroup,
          })
        : services.GroupService.create({ data: this.localGroup });
      saveOperation
        .then((group) => {
          this.$emit("saved", group);
        })
        .catch((error) => {
          this.showDismissibleAlert.dismissable = true;
          this.showDismissibleAlert.message = "Error: " + error.data;
          this.showDismissibleAlert.variant = "danger";
        });
    },
    addGroupMember(airavataInternalUserId) {
      this.localGroup.members.push(airavataInternalUserId);
    },
    removeGroupMember(airavataInternalUserId) {
      const index = this.localGroup.members.indexOf(airavataInternalUserId);
      this.localGroup.members.splice(index, 1);
      this.removeAdminMember(airavataInternalUserId);
    },
    removeAdminMember(airavataInternalUserId) {
      const adminIndex = this.localGroup.admins.indexOf(airavataInternalUserId);
      if (adminIndex >= 0) {
        this.localGroup.admins.splice(adminIndex, 1);
      }
    },
    changeRoleToMember(airavataInternalUserId) {
      this.removeAdminMember(airavataInternalUserId);
    },
    changeRoleToAdmin(airavataInternalUserId) {
      const adminIndex = this.localGroup.admins.indexOf(airavataInternalUserId);
      if (adminIndex < 0) {
        this.localGroup.admins.push(airavataInternalUserId);
      }
    },
  },
};
</script>
