<template>
  <div>
    <b-alert :variant="showDismissibleAlert.variant" dismissible :show="showDismissibleAlert.dismissable" @dismissed="showDismissibleAlert.dismissable=false">
      {{ showDismissibleAlert.message }}
    </b-alert>

    <b-form @submit="onSubmit" v-if="show">

      <b-form-group id="group1" label="Group Name:" label-for="group_name" description="Name should only contain Alpha Characters">
        <b-form-input id="group_name" type="text" v-model="newGroup.name" required placeholder="Enter group name">
        </b-form-input>
      </b-form-group>

      <b-form-group id="group2" label="Description:" label-for="description">
        <b-form-textarea id="description" type="text" :rows="6" v-model="newGroup.description" required placeholder="Enter description of the group">
        </b-form-textarea>
      </b-form-group>

      <b-form-group id="group3">
        <b-btn v-b-toggle.togglemembers size="sm" variant="primary">Add Members&nbsp;&nbsp;&#9660;</b-btn>
        <b-collapse id="togglemembers" class="mt-2">
          <b-form-checkbox-group v-model="selected" name="addMembers" :options="membersList"></b-form-checkbox-group>
        </b-collapse>
      </b-form-group>

      <b-button type="submit" variant="success">Submit</b-button>

    </b-form>
  </div>
</template>

<script>

import { models, services } from 'django-airavata-api'

export default {
  data () {
    return {
      newGroup: new models.Group(),
      show: true,
      membersList: ['stephen', 'marcus', 'marlon', 'suresh'],
      selected: [],
      showDismissibleAlert: {'variant':'success', 'message':'no data', 'dismissable':false},
    }
  },
  methods: {
    onSubmit (evt) {
      evt.preventDefault();
      this.newGroup.members = this.selected.toString();
      alert(JSON.stringify(this.newGroup));
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
  }
}
</script>
