<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">Browse Projects</h1>
            </div>
            <div id="col-new-project" class="col">
                <b-btn v-b-modal.modal-new-project variant="primary">New Project <i class="fa fa-plus" aria-hidden="true"></i></b-btn>
                <!-- TODO: Validate form before creating the project: do form validation or model validation? Do model validation in the model or in the service? -->
                <!-- TODO: factor modal out into separate, reusable component -->
                <b-modal id="modal-new-project" ref="modalNewProject" title="Create New Project" v-on:ok="onCreateProject">
                    <b-form @submit="onCreateProject" novalidate>
                        <b-form-group label="Project Name" label-for="new-project-name" v-bind:feedback="newProjectFields.name.feedback" v-bind:state="newProjectFields.name.state">
                            <b-form-input id="new-project-name"
                                type="text" v-model="newProject.name" required
                                placeholder="Project name"
                                v-bind:state="newProjectFields.name.state"></b-form-input>
                        </b-form-group>
                        <b-form-group label="Project Description" label-for="new-project-description">
                            <b-form-textarea id="new-project-description"
                                type="text" v-model="newProject.description"
                                placeholder="Project description"
                                :rows="3"></b-form-textarea>
                        </b-form-group>
                    </b-form>
                </b-modal>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <project-list v-bind:projects="projects"></project-list>
                        <pager v-bind:paginator="projectsPaginator"
                        v-on:next="nextProjects" v-on:previous="previousProjects"></pager>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import ProjectList from './ProjectList.vue'

import { models, services } from 'django-airavata-api'
import { components as comps } from 'django-airavata-common-ui'

export default {
    props: ['initialProjectsData'],
    name: 'project-list-container',
    data () {
        return {
            projectsPaginator: null,
            newProject: new models.Project(),
            newProjectFields: this.initialNewProjectFieldsState(),
        }
    },
    components: {
        'project-list': ProjectList,
        'pager': comps.Pager
    },
    methods: {
        nextProjects: function(event) {
            this.projectsPaginator.next();
        },
        previousProjects: function(event) {
            this.projectsPaginator.previous();
        },
        onCreateProject: function(event) {
            // Prevent hiding modal, hide it programmatically when project gets created
            event.preventDefault();
            services.ProjectService.create(this.newProject)
                .then(result => {
                    this.$refs.modalNewProject.hide();
                    this.newProject = new models.Project();
                    this.newProjectFields = this.initialNewProjectFieldsState();
                    // Reload the list of projects
                    return services.ProjectService.list()
                        .then(result => this.projectsPaginator = result);
                })
                .catch(error => {
                    if ('name' in error.data) {
                        this.newProjectFields.name.state = 'invalid';
                        this.newProjectFields.name.feedback = error.data.name.join('; ');
                    }
                });
        },
        initialNewProjectFieldsState: function() {
            return {
                name: {
                    valid: null,
                    feedback: null,
                },
            }
        },
    },
    computed: {
        projects: function() {
            return this.projectsPaginator ? this.projectsPaginator.results : null;
        },
    },
    beforeMount: function () {
        services.ProjectService.list(this.initialProjectsData)
            .then(result => this.projectsPaginator = result);
    }
}
</script>

<style>
#col-new-project {
    text-align: right;
}
#modal-new-project {
    text-align: left;
}
</style>
