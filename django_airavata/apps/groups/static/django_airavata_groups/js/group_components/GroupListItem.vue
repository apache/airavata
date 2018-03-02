<template>
    <tr>
        <td>{{ group.name }}</td>
        <td>{{ group.ownerId }}</td>
        <td>{{ group.description }}</td>
        <td>
            <a v-if="group.isOwner || group.isAdmin"
                :href="'/groups/edit/' + encodeURIComponent(group.id) + '/'">
                Edit <i class="fa fa-pencil"></i>
            </a>
            <a href="#" v-if="group.isOwner" @click="show=true" :variant="deleteButtonVariant">
                Delete <i class="fa fa-trash"></i>
            </a>
            <b-modal :header-bg-variant="headerBgVariant" :header-text-variant="headerTextVariant" :body-bg-variant="bodyBgVariant" v-model="show" :id="'modal'+group.id" title="Are you sure?">
              <p class="my-4">You cannot go back! Do you really want to delete the group '<strong>{{ group.name }}</strong>'?</p>
              <div slot="modal-footer" class="w-100">
                <b-button class="float-right ml-1" :variant="yesButtonVariant" @click="deleteGroup(group.id)">Yes</b-button>
                <b-button class="float-right ml-1" :variant="noButtonVariant" @click="show=false">No</b-button>
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
      }
    },
    props: ['group'],
    methods: {
      deleteGroup(id) {
        try {
          let ret_msg = services.GroupService.delete(id);
          this.$emit('deleteSuccess','Group Deleted Successfully!');
          this.show = false;
        }
        catch(error) {
            console.log(error);
            this.$emit('deleteFailed', 'Group Delete Failed!');
            this.show = false;
        }
      }
    }
}
</script>

<style>
</style>
