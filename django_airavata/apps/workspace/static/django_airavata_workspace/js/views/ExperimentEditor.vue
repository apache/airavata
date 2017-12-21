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
                            <b-form-group label="Compute Resource" label-for="compute-resource">
                                <b-form-select id="compute-resource"
                                    v-model="experiment.userConfigurationData.computationalResourceScheduling.resourceHostId"
                                    :options="computeResourceOptions" required
                                    @change="computeResourceChanged">
                                    <template slot="first">
                                        <option :value="null" disabled>Select a Compute Resource</option>
                                    </template>
                                </b-form-select>
                            </b-form-group>

                            <div class="card border-default">
                                <div class="card-body">
                                    <h4 class="card-title">Current Settings</h4>
                                </div>
                            </div>
                            <b-form-group label="Select a Queue" label-for="queue">
                                <b-form-select id="queue"
                                    v-model="experiment.userConfigurationData.computationalResourceScheduling.queueName"
                                    :options="queueOptions" required
                                    @change="queueChanged">
                                </b-form-select>
                            </b-form-group>
                            <b-form-group label="Node Count" label-for="node-count">
                                <b-form-input id="node-count" type="number" min="1"
                                    v-model="experiment.userConfigurationData.computationalResourceScheduling.nodeCount" required>
                                </b-form-input>
                            </b-form-group>
                            <b-form-group label="Total Core Count" label-for="core-count">
                                <b-form-input id="core-count" type="number" min="1"
                                    v-model="experiment.userConfigurationData.computationalResourceScheduling.totalCPUCount" required>
                                </b-form-input>
                            </b-form-group>
                            <b-form-group label="Wall Time Limit" label-for="walltime-limit">
                                <b-form-input id="walltime-limit" type="number" min="1"
                                    v-model="experiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit" required>
                                </b-form-input>
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
    props: ['experiment', 'appModule', 'appInterface'],
    data () {
        return {
            projects: [],
            computeResources: {},
            applicationDeployments: [],
            queueDefaults: [],
        }
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
        queueOptions: function() {
            const queueOptions = this.queueDefaults.map(queueDefault => {
                return {
                    value: queueDefault.queueName,
                    text: `${queueDefault.queueName}: ${queueDefault.queueDescription}`,
                }
            });
            return queueOptions;
        },
    },
    methods: {
        computeResourceChanged: function(selectedComputeResourceId) {
            // Find application deployment that corresponds to this compute resource
            // TODO: switch to find()
            let selectedApplicationDeployments = this.applicationDeployments.filter(dep => dep.computeHostId === selectedComputeResourceId);
            if (selectedApplicationDeployments.length === 0) {
                throw new Error("Failed to find application deployment!");
            }
            let selectedApplicationDeployment = selectedApplicationDeployments[0];
            services.ApplicationDeploymentService.getQueues(selectedApplicationDeployment.appDeploymentId)
                .then(queueDefaults => {
                    // Sort queue defaults
                    this.queueDefaults = queueDefaults.sort((a, b) => {
                        // Sort default first, then by alphabetically by name
                        if (a.isDefaultQueue) {
                            return -1;
                        } else if (b.isDefaultQueue) {
                            return 1;
                        } else {
                            return a.queueName.localeCompare(b.queueName);
                        }
                    });
                    // Find the default queue and apply it's settings
                    const defaultQueue = this.queueDefaults[0];

                    // TODO: we really shouldn't be modifying the props right?
                    this.experiment.userConfigurationData.computationalResourceScheduling.queueName = defaultQueue.queueName;
                    this.experiment.userConfigurationData.computationalResourceScheduling.totalCPUCount = defaultQueue.defaultCPUCount;
                    this.experiment.userConfigurationData.computationalResourceScheduling.nodeCount = defaultQueue.defaultNodeCount;
                    this.experiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit = defaultQueue.defaultWalltime;
                });
        },
        queueChanged: function(queueName) {

            const queueDefault = this.queueDefaults.find(queue => queue.queueName === queueName);
            this.experiment.userConfigurationData.computationalResourceScheduling.totalCPUCount = queueDefault.defaultCPUCount;
            this.experiment.userConfigurationData.computationalResourceScheduling.nodeCount = queueDefault.defaultNodeCount;
            this.experiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit = queueDefault.defaultWalltime;
        }
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