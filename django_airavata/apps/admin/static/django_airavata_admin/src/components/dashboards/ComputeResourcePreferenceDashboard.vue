<template>
  <list-layout @add-new-item="newGroupResourcePreference" :items="groupResourceProfiles"
      title="Group Resource Profiles" new-item-button-text="New Group Resource Profile">
    <template slot="item-list" slot-scope="slotProps">

      <b-table striped hover :fields="fields" :items="slotProps.items">
        <template slot="action" slot-scope="data">
          <a href="#" @click.prevent="clickHandler(data.item)" v-if="data.item.userHasWriteAccess">
            Edit
            <i class="fa fa-edit" aria-hidden="true"></i>
          </a>
        </template>
      </b-table> 
    </template>
  </list-layout>
</template>

<script>
  import {layouts} from 'django-airavata-common-ui'
  import {services} from 'django-airavata-api'
  import moment from 'moment'

  export default {
    name: "compute-resource-preference",
    components: {
      'list-layout': layouts.ListLayout,
    },
    data: function () {
      return {
        groupResourceProfiles: [],
        fields: [
          {
            label: 'Name',
            key: 'groupResourceProfileName',
          },
          {
            label: 'Updated',
            key: 'updatedTime',
            formatter: (value) => moment(new Date(value)).fromNow(),
          },
          {
            label: 'Action',
            key: 'action',
          },
        ],
      }
    },
    methods: {
      clickHandler: function (groupResourceProfile) {
        this.$router.push({
          name: 'group_resource_preference', params: {
            value: groupResourceProfile,
            id: groupResourceProfile.groupResourceProfileId
          }
        });
      },
      newGroupResourcePreference: function () {
        this.$router.push({
          name: 'group_resource_preference', params: {
            newCreation: true
          }
        })
      },
      loadGroupResourceProfiles: function () {
        services.GroupResourceProfileService.list()
          .then(groupResourceProfiles => {
            this.groupResourceProfiles = groupResourceProfiles;
          });
      },
    },
    mounted: function () {
      this.loadGroupResourceProfiles();
    }
  }
</script>