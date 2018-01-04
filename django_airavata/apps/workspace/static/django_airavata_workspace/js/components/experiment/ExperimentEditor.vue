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
                        <h2 class="h5 mb-3">
                            Resource Selection
                        </h2>
                        <b-form novalidate>
                            <!-- TODO: combine compute resource selector with
                            queue-settings-editor to create a
                            ComputationalResourceSchedulingModelEditor component -->
                            <b-form-group label="Compute Resource" label-for="compute-resource">
                                <b-form-select id="compute-resource"
                                    v-model="resourceHostId"
                                    :options="computeResourceOptions" required
                                    @change="computeResourceChanged">
                                    <template slot="first">
                                        <option :value="null" disabled>Select a Compute Resource</option>
                                    </template>
                                </b-form-select>
                            </b-form-group>

                            <queue-settings-editor
                                v-model="experiment.userConfigurationData.computationalResourceScheduling"
                                v-if="appDeploymentId && resourceHostId"
                                :app-deployment-id="appDeploymentId"
                                :resource-host-id="resourceHostId">
                            </queue-settings-editor>
                        </b-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import QueueSettingsEditor from './QueueSettingsEditor.vue'
import {models, services} from 'django-airavata-api'

export default {
    name: 'edit-experiment',
    // TODO: clone experiment instead of editing it directly
    props: ['experiment', 'appModule', 'appInterface'],
    data () {
        return {
            projects: [],
            computeResources: {},
            applicationDeployments: [],
            appDeploymentId: null,
            resourceHostId: null,
        }
    },
    components: {
        QueueSettingsEditor,
    },
    mounted: function () {
        services.ProjectService.listAll()
            .then(projects => this.projects = projects);
        services.ApplicationModuleService.getApplicationDeployments(this.appModule.appModuleId)
            .then(applicationDeployments => {
                this.applicationDeployments = applicationDeployments;
            });
        services.ApplicationInterfaceService.getComputeResources(this.appInterface.applicationInterfaceId)
            .then(computeResources => this.computeResources = computeResources);
    },
    computed: {
        projectOptions: function() {
            return this.projects.map(project => ({
                value: project.projectID,
                text: project.name,
            }));
        },
        computeResourceOptions: function() {
            const computeResourceOptions = [];
            for (let computeResourceId in this.computeResources) {
                if (this.computeResources.hasOwnProperty(computeResourceId)) {
                    computeResourceOptions.push({
                        value: computeResourceId,
                        text: this.computeResources[computeResourceId],
                    })
                }
            }
            computeResourceOptions.sort((a, b) => a.text.localeCompare(b.text));
            return computeResourceOptions;
        },
    },
    methods: {
        computeResourceChanged: function(selectedComputeResourceId) {
            // Find application deployment that corresponds to this compute resource
            let selectedApplicationDeployment = this.applicationDeployments.find(dep => dep.computeHostId === selectedComputeResourceId);
            if (!selectedApplicationDeployment) {
                throw new Error("Failed to find application deployment!");
            }
            this.appDeploymentId = selectedApplicationDeployment.appDeploymentId;
        },
    },
    watch: {
        appInterface: function() {
            if (this.appInterface) {
                services.ApplicationInterfaceService.getComputeResources(this.appInterface.applicationInterfaceId)
                    .then(computeResources => this.computeResources = computeResources);
            }
        },
    }
}
</script>

<style>
.application-name {
    font-size: 12px;
}
</style>