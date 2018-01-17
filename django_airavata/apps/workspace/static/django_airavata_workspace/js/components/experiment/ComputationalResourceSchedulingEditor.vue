<template>
    <div>
        <div class="row">
            <div class="col">
                <b-form-group label="Compute Resource" label-for="compute-resource"
                        :feedback="getValidationFeedback('resourceHostId')"
                        :state="getValidationState('resourceHostId')">
                    <b-form-select id="compute-resource"
                        v-model="resourceHostId"
                        :options="computeResourceOptions" required
                        @change="computeResourceChanged"
                        :state="getValidationState('resourceHostId')">
                        <template slot="first">
                            <option :value="null" disabled>Select a Compute Resource</option>
                        </template>
                    </b-form-select>
                </b-form-group>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <queue-settings-editor
                    v-model="localComputationalResourceScheduling"
                    v-if="appDeploymentId"
                    :app-deployment-id="appDeploymentId"
                    @input="queueSettingsChanged">
                </queue-settings-editor>
            </div>
        </div>
    </div>
</template>

<script>
import QueueSettingsEditor from './QueueSettingsEditor.vue'
import {models, services} from 'django-airavata-api'
import {utils} from 'django-airavata-common-ui'

export default {
    name: 'computational-resource-scheduling-editor',
    props: {
        value: {
            type: models.ComputationalResourceSchedulingModel,
            required: true
        },
        appModuleId: {
            type: String,
            required: true
        },
        appInterfaceId: {
            type: String,
            required: true
        },
    },
    data () {
        return {
            localComputationalResourceScheduling: this.value.clone(),
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
        this.loadApplicationDeployments(this.appModuleId);
        this.loadComputeResourcesForApplicationInterface(this.appInterfaceId);
    },
    computed: {
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
            this.localComputationalResourceScheduling.resourceHostId = selectedComputeResourceId;
            this.emitValueChanged();
            // Find application deployment that corresponds to this compute resource
            let selectedApplicationDeployment = this.applicationDeployments.find(dep => dep.computeHostId === selectedComputeResourceId);
            if (!selectedApplicationDeployment) {
                throw new Error("Failed to find application deployment!");
            }
            this.appDeploymentId = selectedApplicationDeployment.appDeploymentId;
        },
        loadApplicationDeployments: function(appModuleId) {
            services.ApplicationModuleService.getApplicationDeployments(appModuleId)
                .then(applicationDeployments => {
                    this.applicationDeployments = applicationDeployments;
                });
        },
        loadComputeResourcesForApplicationInterface: function(appInterfaceId) {
            services.ApplicationInterfaceService.getComputeResources(appInterfaceId)
                .then(computeResources => this.computeResources = computeResources);
        },
        queueSettingsChanged: function() {
            // QueueSettingsEditor updates the full
            // ComputationalResourceSchedulingModel instance but doesn't know
            // the resourceHostId so we need to copy it back into the instance
            // whenever it changes
            this.localComputationalResourceScheduling.resourceHostId = this.resourceHostId;
            this.emitValueChanged();
        },
        emitValueChanged: function() {
            this.$emit('input', this.localComputationalResourceScheduling);
        },
        getValidationFeedback: function(properties) {
            return utils.getProperty(this.localComputationalResourceScheduling.validate(), properties);
        },
        getValidationState: function(properties) {
            return this.getValidationFeedback(properties) ? 'invalid' : null;
        },
    },
    watch: {
    }
}
</script>

<style>
</style>