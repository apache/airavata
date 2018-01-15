<template>
    <div>
        <b-btn v-b-modal.modal-new-group variant="primary">
            <slot>
                New Group <i class="fa fa-plus" aria-hidden="true"></i>
            </slot>
        </b-btn>
        <b-modal id="modal-new-group" ref="modalNewGroup" title="Create New Group"
                v-on:ok="onCreateGroup" v-bind:cancel-disabled="cancelDisabled"
                v-bind:ok-disabled="okDisabled">
            <b-form @submit="onCreateGroup" @input="onUserInput" novalidate>
                <b-form-group label="Group Name" label-for="new-group-name" v-bind:feedback="newGroupNameFeedback" v-bind:state="newGroupNameState">
                    <b-form-input id="new-group-name"
                        type="text" v-model="newGroup.name" required
                        placeholder="Group name"
                        v-bind:state="newGroupNameState"></b-form-input>
                </b-form-group>
                <b-form-group label="Group Description" label-for="new-group-description">
                    <b-form-textarea id="new-group-description"
                        type="text" v-model="newGroup.description"
                        placeholder="(Optional) Group description"
                        :rows="3"></b-form-textarea>
                </b-form-group>
                <b-form-group label="Group Members" label-for="new-group-members">
                    <b-form-input id="new-group-members"
                        type="text" v-model="newGroup.members"
                        placeholder="(Optional) Group Members"
                        :rows="3"></b-form-input>
                </b-form-group>
            </b-form>
        </b-modal>
    </div>
</template>

<script>

import { models, services } from 'django-airavata-api'

export default {
    name: 'group-button-new',
    data () {
        return {
            newGroup: new models.GroupOwner(),
            newGroupServerValidationData: null,
            userBeginsInput: false,
            loading: false,
        }
    },
    methods: {
        onCreateGroup: function(event) {
            // Prevent hiding modal, hide it programmatically when Group gets created
            event.preventDefault();
            this.loading = true;
            services.GroupService.create(this.newGroup)
                .then(result => {
                    this.$refs.modalNewGroup.hide();
                    this.$emit('new-group', result);
                    // Reset state
                    this.newGroup = new models.Group();
                    this.userBeginsInput = false;
                })
                .catch(error => {
                    this.newGroupServerValidationData = error.data;
                })
                .then(() => this.loading = false, () => this.loading = false);
        },
        onUserInput: function(event) {
            this.userBeginsInput = true;
            // Clear server side validation data when user starts typing again
            this.newGroupServerValidationData = null;
        },
    },
    computed: {
        newGroupValidationData: function() {
            return this.userBeginsInput ? this.newGroup.validateForCreate() : null;
        },
        newGroupNameState: function() {
            if (this.newGroupServerValidationData && 'name' in this.newGroupServerValidationData) {
                return 'invalid';
            } else if (this.newGroupValidationData && 'name' in this.newGroupValidationData) {
                return 'invalid';
            } else {
                return null;
            }
        },
        newGroupNameFeedback: function() {
            if (this.newGroupServerValidationData && 'name' in this.newGroupServerValidationData) {
                return this.newGroupServerValidationData.name.join('; ');
            } else if (this.newGroupValidationData && 'name' in this.newGroupValidationData) {
                return this.newGroupValidationData.name.join('; ');
            } else {
                return null;
            }
        },
        formIsValid: function() {
            return this.newGroupNameState == null;
        },
        cancelDisabled: function() {
            return this.loading;
        },
        okDisabled: function() {
            return this.loading || !this.userBeginsInput || !this.formIsValid;
        }
    },
}
</script>
