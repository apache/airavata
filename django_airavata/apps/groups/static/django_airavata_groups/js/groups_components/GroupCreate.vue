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
      suggestions: [
          { id: 1, name: 'Stephen' },
          { id: 2, name: 'Marcus' },
          { id: 3, name: 'Marlon' },
          { id: 4, name: 'Suresh' },
          { id: 5, name: 'Eroma' },
          { id: 6, name: 'Sachin' },
          { id: 7, name: 'Jerrin' },
          { id: 8, name: 'Eldho' },
          { id: 9, name: 'Dimuthu' },
          { id: 10, name: 'Ameya' },
          { id: 11, name: 'Sneha' },
        ],
      newGroup: new models.Group(),
      show: true,
      selected: [],
      showDismissibleAlert: {'variant':'success', 'message':'no data', 'dismissable':false},
    }
  },
  components: {
    Autocomplete
  },
  methods: {
    submitForm () {
      this.newGroup.members = this.selected;
      console.log(JSON.stringify(this.newGroup));
      services.GroupService.create(this.newGroup)
          .then(result => {
              this.showDismissibleAlert.dismissable = true;
              this.showDismissibleAlert.message = "Successfully created a new group";
              this.showDismissibleAlert.variant = "success";
              this.newGroup = new models.Group();
          })
          .catch(error => {
              this.showDismissibleAlert.dismissable = true;
              this.showDismissibleAlert.message = "Error: "+error.data;
              this.showDismissibleAlert.variant = "danger";
          });
    },
    updateSelectedValue(data) {
      this.selected = data;
    }
  }
}
</script>
