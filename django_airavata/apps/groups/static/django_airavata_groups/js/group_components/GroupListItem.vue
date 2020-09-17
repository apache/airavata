<template>
  <tr>
    <td>
      {{ group.name }}
      <gateway-groups-badge
        :group="group"
        v-if="
          group.isGatewayAdminsGroup ||
          group.isReadOnlyGatewayAdminsGroup ||
          group.isDefaultGatewayUsersGroup
        "
      />
    </td>
    <td>{{ ownerUsername }}</td>
    <td>{{ group.description }}</td>
    <td>
      <a
        v-if="group.isOwner || group.isAdmin"
        class="action-link"
        :href="'/groups/edit/' + encodeURIComponent(group.id) + '/'"
      >
        Edit <i class="fa fa-edit"></i>
      </a>
      <a
        href="#"
        v-if="deleteable"
        class="action-link"
        @click="show = true"
        :variant="deleteButtonVariant"
      >
        Delete <i class="fa fa-trash"></i>
      </a>
      <b-modal
        :header-bg-variant="headerBgVariant"
        :header-text-variant="headerTextVariant"
        :body-bg-variant="bodyBgVariant"
        v-model="show"
        :id="'modal' + group.id"
        title="Are you sure?"
      >
        <p class="my-4">
          You cannot go back! Do you really want to delete the group '<strong>{{
            group.name
          }}</strong
          >'?
        </p>
        <div slot="modal-footer" class="w-100">
          <b-button
            class="float-right ml-1"
            :variant="yesButtonVariant"
            :disabled="deleting"
            @click="deleteGroup(group.id)"
            >Yes</b-button
          >
          <b-button
            class="float-right ml-1"
            :variant="noButtonVariant"
            :disabled="deleting"
            @click="show = false"
            >No</b-button
          >
        </div>
      </b-modal>
    </td>
  </tr>
</template>

<script>
import { services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";

export default {
  name: "group-list-item",
  data() {
    return {
      show: false,
      deleteButtonVariant: "link",
      yesButtonVariant: "danger",
      noButtonVariant: "secondary",
      headerBgVariant: "danger",
      bodyBgVariant: "light",
      headerTextVariant: "light",
      deleting: false,
    };
  },
  props: ["group"],
  components: {
    "gateway-groups-badge": components.GatewayGroupsBadge,
  },
  computed: {
    deleteable: function () {
      return (
        this.group.isOwner &&
        // Don't allow deleting "GatewayGroups" groups since they serve
        // a special function in the gateway
        this.group.isGatewayAdminsGroup === false &&
        this.group.isReadOnlyGatewayAdminsGroup === false &&
        this.group.isDefaultGatewayUsersGroup === false
      );
    },
    ownerUsername() {
      const lastAtIndex = this.group.ownerId.lastIndexOf("@");
      if (lastAtIndex > 0) {
        return this.group.ownerId.substring(0, lastAtIndex);
      }
      return this.group.ownerId;
    },
  },
  methods: {
    deleteGroup(id) {
      this.deleting = true;
      services.GroupService.delete({ lookup: id })
        .then(() => {
          this.$emit("deleteSuccess", "Group Deleted Successfully!");
          this.show = false;
          this.deleting = false;
        })
        .catch(() => {
          this.$emit("deleteFailed", "Group Delete Failed!");
          this.show = false;
          this.deleting = false;
        });
    },
  },
};
</script>

<style></style>
