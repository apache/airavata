<template>
  <div>
    <b-alert :variant="showDismissibleAlert.variant" dismissible :show="showDismissibleAlert.dismissable" @dismissed="showDismissibleAlert.dismissable=false">
      {{ showDismissibleAlert.message }}
    </b-alert>

    <b-form v-if="show">

      <b-form-group id="group1" label="Group Name:" label-for="group_name" description="Name should only contain Alpha Characters">
        <b-form-input id="group_name" type="text" v-model="newGroup.name" required placeholder="Enter group name">
        </b-form-input>
      </b-form-group>

      <b-form-group id="group2" label="Description:" label-for="description">
        <b-form-textarea id="description" type="text" :rows="6" v-model="newGroup.description" required placeholder="Enter description of the group">
        </b-form-textarea>
      </b-form-group>

      <b-form-group id="group3" label="Add Members:" label-for="members">
        <autocomplete id="members" :suggestions="suggestions" v-model="selection" v-on:updateSelected="updateSelectedValue"></autocomplete>
      </b-form-group>

      <b-button @click="submitForm" variant="primary">Submit</b-button>
    </b-form>
  </div>
</template>

<script>

import { models, services } from 'django-airavata-api'

import Autocomplete from './Autocomplete.vue'

export default {
    data () {
        return {
            selection: '',
            newGroup: new models.Group(),
            show: true,
            selected: [],
            showDismissibleAlert: {'variant':'success', 'message':'no data', 'dismissable':false},
            userProfiles: [],
        }
    },
    components: {
        Autocomplete
    },
    methods: {
        submitForm () {
            var temp = [];
            for(var i=0;i<this.selected.length;i++) {
                temp.push(this.selected[i].id);
            }
            this.newGroup.members = temp;
            console.log(JSON.stringify(this.newGroup));
            services.GroupService.create(this.newGroup)
            .then(result => {
                // TODO: redirect to the group view page
                window.location.assign("/groups/");
            })
            .catch(error => {
                this.showDismissibleAlert.dismissable = true;
                this.showDismissibleAlert.message = "Error: "+error.data;
                this.showDismissibleAlert.variant = "danger";
            });
        },
        updateSelectedValue(data) {
            this.selected = data;
        },
    },
    computed: {
        suggestions: function() {
            return this.userProfiles.map(userProfile => {
                return {
                    id: userProfile.airavataInternalUserId,
                    name: userProfile.firstName + ' ' + userProfile.lastName + ' (' + userProfile.userId + ')'
                }
            })
        }
    },
    mounted: function () {
        services.UserProfileService.list()
            .then(userProfiles => {
                this.userProfiles = userProfiles;
            });
    },
}
</script>
