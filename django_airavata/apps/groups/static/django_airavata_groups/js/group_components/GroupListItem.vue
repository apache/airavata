<template>
    <tr>
        <td>{{ group.name }}
            <b-badge v-if="group.isGatewayAdminsGroup">Admins</b-badge>
            <b-badge v-if="group.isReadOnlyGatewayAdminsGroup">Read Only Admins</b-badge>
            <b-badge v-if="group.isDefaultGatewayUsersGroup">Default</b-badge>
        </td>
        <td>{{ group.ownerId }}</td>
        <td>{{ group.description }}</td>
        <td>
            <a v-if="group.isOwner || group.isAdmin"
                :href="'/groups/edit/' + encodeURIComponent(group.id) + '/'">
                Edit <i class="fa fa-pencil"></i>
            </a>
            <a href="#" v-if="deleteable" @click="show=true" :variant="deleteButtonVariant">
                Delete <i class="fa fa-trash"></i>
            </a>
            <b-modal :header-bg-variant="headerBgVariant" :header-text-variant="headerTextVariant" :body-bg-variant="bodyBgVariant" v-model="show" :id="'modal'+group.id" title="Are you sure?">
              <p class="my-4">You cannot go back! Do you really want to delete the group '<strong>{{ group.name }}</strong>'?</p>
              <div slot="modal-footer" class="w-100">
                <b-button class="float-right ml-1" :variant="yesButtonVariant" :disabled="deleting" @click="deleteGroup(group.id)">Yes</b-button>
                <b-button class="float-right ml-1" :variant="noButtonVariant" :disabled="deleting" @click="show=false">No</b-button>
              </div>
            </b-modal>
        </td>
    </tr>
</template>

<script>

import { services } from 'django-airavata-api'

export default {
    name: 'group-list-item',
    data() {
      return {
        show: false,
        deleteButtonVariant: 'link',
        yesButtonVariant: 'danger',
        noButtonVariant: 'secondary',
        headerBgVariant: 'danger',
        bodyBgVariant: 'light',
        headerTextVariant: 'light',
        deleting: false,
      }
    },
    props: ['group'],
    computed: {
        deleteable: function() {
            return this.group.isOwner
                // Don't allow deleting "GatewayGroups" groups since they serve
                // a special function in the gateway
                && this.group.isGatewayAdminsGroup === false
                && this.group.isReadOnlyGatewayAdminsGroup === false
                && this.group.isDefaultGatewayUsersGroup === false;
        }
    },
    methods: {
      deleteGroup(id) {
          this.deleting = true;
          services.GroupService.delete(id)
              .then(result => {
                  this.$emit('deleteSuccess','Group Deleted Successfully!');
                  this.show = false;
                  this.deleting = false;
              })
              .catch(error => {
                  console.log(error);
                  this.$emit('deleteFailed', 'Group Delete Failed!');
                  this.show = false;
                  this.deleting = false;
              });
      }
    }
}
</script>

<style>
</style>
