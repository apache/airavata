
<template>
    <div>
        <b-btn v-b-modal.modal-new-project variant="primary">
            <slot>
                New Project <i class="fa fa-plus" aria-hidden="true"></i>
            </slot>
        </b-btn>
        <b-modal id="modal-new-project" ref="modalNewProject" title="Create New Project" v-on:ok="onCreateProject">
            <b-form @submit="onCreateProject" @input="onUserInput" novalidate>
                <b-form-group label="Project Name" label-for="new-project-name" v-bind:feedback="newProjectNameFeedback" v-bind:state="newProjectNameState">
                    <b-form-input id="new-project-name"
                    type="text" v-model="newProject.name" required
                    placeholder="Project name"
                    v-bind:state="newProjectNameState"></b-form-input>
                </b-form-group>
                <b-form-group label="Project Description" label-for="new-project-description">
                    <b-form-textarea id="new-project-description"
                    type="text" v-model="newProject.description"
                    placeholder="(Optional) Project description"
                    :rows="3"></b-form-textarea>
                </b-form-group>
            </b-form>
        </b-modal>
    </div>
</template>

<script>

import { models, services } from 'django-airavata-api'

export default {
    name: 'project-button-new',
    data () {
        return {
            newProject: new models.Project(),
            newProjectServerValidationData: null,
            userBeginsInput: false,
        }
    },
    components: {
    },
    methods: {
        onCreateProject: function(event) {
            // Prevent hiding modal, hide it programmatically when project gets created
            event.preventDefault();
            services.ProjectService.create(this.newProject)
                .then(result => {
                    this.$refs.modalNewProject.hide();
                    this.$emit('new-project', result);
                    // Reset state
                    this.newProject = new models.Project();
                    this.userBeginsInput = false;
                })
                .catch(error => {
                    this.newProjectServerValidationData = error.data;
                });
        },
        onUserInput: function(event) {
            this.userBeginsInput = true;
            // Clear server side validation data when user starts typing again
            this.newProjectServerValidationData = null;
        },
    },
    computed: {
        newProjectValidationData: function() {
            return this.userBeginsInput ? this.newProject.validateForCreate() : null;
        },
        newProjectNameState: function() {
            if (this.newProjectServerValidationData && 'name' in this.newProjectServerValidationData) {
                return 'invalid';
            } else if (this.newProjectValidationData && 'name' in this.newProjectValidationData) {
                return 'invalid';
            } else {
                return null;
            }
        },
        newProjectNameFeedback: function() {
            if (this.newProjectServerValidationData && 'name' in this.newProjectServerValidationData) {
                return this.newProjectServerValidationData.name.join('; ');
            } else if (this.newProjectValidationData && 'name' in this.newProjectValidationData) {
                return this.newProjectValidationData.name.join('; ');
            } else {
                return null;
            }
        },
    },
}
</script>

<style>
/*#modal-new-project {
    text-align: left;
}*/
</style>
