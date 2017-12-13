<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">
                    <div v-if="appModule" class="application-name text-muted text-uppercase"><i class="fa fa-code" aria-hidden="true"></i> {{ appModule.appModuleName }}</div>
                    <slot name="title">Experiment Editor</slot>
                </h1>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <b-form novalidate>
                            <b-form-group label="Experiment Name" label-for="experiment-name">
                                <b-form-input id="experiment-name"
                                type="text" v-model="experiment.experimentName" required
                                placeholder="Experiment name"></b-form-input>
                            </b-form-group>
                            <b-form-group label="Project" label-for="project">
                                <b-form-select id="project"
                                    v-model="experiment.projectId" :options="projectOptions" required>
                                    <template slot="first">
                                        <option :value="null" disabled>Select a Project</option>
                                    </template>
                                </b-form-select>
                            </b-form-group>
                        </b-form>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">
                    Application Configuration
                </h1>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <h2 class="h5 mb-3">
                            Application Inputs
                        </h2>
                        <b-form novalidate>
                            <b-form-group v-for="experimentInput in experiment.experimentInputs"
                                    :label="experimentInput.name" :label-for="experimentInput.name" :key="experimentInput.name">
                                <b-form-input :id="experimentInput.name" type="text" v-model="experimentInput.value" required
                                    :placeholder="experimentInput.userFriendlyDescription"></b-form-input>
                            </b-form-group>
                        </b-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import {models, services} from 'django-airavata-api'

export default {
    name: 'edit-experiment',
    props: ['experiment', 'appModule'],
    data () {
        return {
            'projects': [],
        }
    },
    mounted: function () {
        services.ProjectService.listAll()
            .then(projects => this.projects = projects);
    },
    computed: {
        projectOptions: function() {
            return this.projects.map(project => ({
                value: project.projectID,
                text: project.name,
            }));
        }
    }
}
</script>

<style>
.application-name {
    font-size: 12px;
}
</style>