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
        <b-form novalidate>
            <div class="row">
                <div class="col">
                    <b-form-group label="Experiment Name" label-for="experiment-name"
                        :feedback="getValidationFeedback('experimentName')"
                        :state="getValidationState('experimentName')">
                        <b-form-input id="experiment-name"
                        type="text" v-model="localExperiment.experimentName" required
                        placeholder="Experiment name"
                        :state="getValidationState('experimentName')"></b-form-input>
                    </b-form-group>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <b-form-group label="Project" label-for="project"
                        :feedback="getValidationFeedback('projectId')"
                        :state="getValidationState('projectId')">
                        <b-form-select id="project"
                            v-model="localExperiment.projectId" :options="projectOptions" required
                            :state="getValidationState('projectId')">
                            <template slot="first">
                                <option :value="null" disabled>Select a Project</option>
                            </template>
                        </b-form-select>
                    </b-form-group>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <h1 class="h4 mt-5 mb-4">
                        Application Configuration
                    </h1>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <div class="card border-default">
                        <div class="card-body">
                            <h2 class="h6 mb-3">
                                Application Inputs
                            </h2>
                            <component :is="getInputEditorComponentName(experimentInput)"
                                v-for="experimentInput in localExperiment.experimentInputs"
                                :experiment="localExperiment"
                                :experiment-input="experimentInput"
                                v-model="experimentInput.value"
                                :key="experimentInput.name"
                                @invalid="recordInvalidInputEditorValue(experimentInput.name)"
                                @valid="recordValidInputEditorValue(experimentInput.name)"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <h2 class="h4 mt-4 mb-3">
                        Resource Selection
                    </h2>
                </div>
            </div>
            <group-resource-profile-selector v-model="localExperiment.userConfigurationData.groupResourceProfileId">
            </group-resource-profile-selector>
            <div class="row">
                <div class="col">
                    <computational-resource-scheduling-editor
                        v-model="localExperiment.userConfigurationData.computationalResourceScheduling"
                        v-if="localExperiment.userConfigurationData.groupResourceProfileId"
                        :app-module-id="appModule.appModuleId"
                        :app-interface-id="appInterface.applicationInterfaceId"
                        :group-resource-profile-id="localExperiment.userConfigurationData.groupResourceProfileId">
                    </computational-resource-scheduling-editor>
                </div>
            </div>
            <div class="row">
                <div id="col-exp-buttons" class="col">
                    <b-button variant="success" @click="saveAndLaunchExperiment" :disabled="isSaveDisabled">
                        Save and Launch
                    </b-button>
                    <b-button variant="primary" @click="saveExperiment" :disabled="isSaveDisabled">
                        Save
                    </b-button>
                </div>
            </div>
        </b-form>
    </div>
</template>

<script>
import ComputationalResourceSchedulingEditor from './ComputationalResourceSchedulingEditor.vue'
import GroupResourceProfileSelector from './GroupResourceProfileSelector.vue'
import StringInputEditor from './input-editors/StringInputEditor.vue'
import FileInputEditor from './input-editors/FileInputEditor.vue'
import TextareaInputEditor from './input-editors/TextareaInputEditor.vue'
import {models, services, utils as apiUtils} from 'django-airavata-api'
import {utils} from 'django-airavata-common-ui'

export default {
    name: 'edit-experiment',
    props: {
        experiment: {
            type: models.Experiment,
            required: true
        },
        appModule: {
            type: models.ApplicationModule,
            required: true
        },
        appInterface: {
            type: models.ApplicationInterface,
            required: true
        }
    },
    data () {
        return {
            projects: [],
            localExperiment: this.experiment.clone(),
            invalidInputs: [],
        }
    },
    components: {
        ComputationalResourceSchedulingEditor,
        GroupResourceProfileSelector,
        StringInputEditor,
        FileInputEditor,
        TextareaInputEditor,
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
        },
        valid: function() {
            const validation = this.localExperiment.validate();
            return Object.keys(validation).length === 0 && this.invalidInputs.length === 0;
        },
        isSaveDisabled: function() {
            return !this.valid;
        },
    },
    methods: {
        saveExperiment: function() {
            return this.uploadInputFiles()
                .then(uploadResults => {
                    return services.ExperimentService.save(this.localExperiment)
                        .then(experiment => {
                            this.localExperiment = experiment;
                            console.log(JSON.stringify(experiment));
                            this.$emit('saved', experiment);
                        });
                })
                .catch(result => {
                    console.log("Save failed!", result);
                });
        },
        saveAndLaunchExperiment: function() {
            return this.uploadInputFiles()
                .then(uploadResults => {
                    return services.ExperimentService.save(this.localExperiment)
                        .then(experiment => {
                            this.localExperiment = experiment;
                            return services.ExperimentService.launch(experiment.experimentId)
                            .then(result => {
                                this.$emit('savedAndLaunched', experiment);
                            });
                        })
                })
                .catch(result => {
                    console.log("Launch failed!", result);
                });
        },
        uploadInputFiles: function() {
            let uploads = [];
            this.localExperiment.experimentInputs.forEach(input => {
                if (input.type === models.DataType.URI && input.value) {
                    let data = new FormData();
                    data.append('file', input.value);
                    data.append('project-id', this.localExperiment.projectId);
                    data.append('experiment-name', this.localExperiment.experimentName);
                    let uploadRequest = apiUtils.FetchUtils.post('/api/upload', data)
                        .then(result => input.value = result['data-product-uri'])
                    uploads.push(uploadRequest);
                }
            });
            return Promise.all(uploads);
        },
        getValidationFeedback: function(properties) {
            return utils.getProperty(this.localExperiment.validate(), properties);
        },
        getValidationState: function(properties) {
            return this.getValidationFeedback(properties) ? 'invalid' : null;
        },
        getInputEditorComponentName: function(experimentInput) {
            // If input specifices an editor UI component, use that
            if (experimentInput.editorUIComponentId) {
                return experimentInput.editorUIComponentId;
            }
            // Default UI components based on input type
            if (experimentInput.type === models.DataType.STRING) {
                return 'string-input-editor';
            } else if (experimentInput.type === models.DataType.URI) {
                return 'file-input-editor';
            }
            // Default
            return 'string-input-editor';
        },
        recordInvalidInputEditorValue: function(experimentInputName) {
            if (!this.invalidInputs.includes(experimentInputName)) {
                this.invalidInputs.push(experimentInputName);
            }
        },
        recordValidInputEditorValue: function(experimentInputName) {
            if (this.invalidInputs.includes(experimentInputName)) {
                const index = this.invalidInputs.indexOf(experimentInputName);
                this.invalidInputs.splice(index, 1);
            }
        },
    },
    watch: {
        experiment: function(newValue) {
            this.localExperiment = newValue.clone();
        },
    }
}
</script>

<style>
.application-name {
    font-size: 12px;
}
#col-exp-buttons {
    text-align: right;
}
</style>
