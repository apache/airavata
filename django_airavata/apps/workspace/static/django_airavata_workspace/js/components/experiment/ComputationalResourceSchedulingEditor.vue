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
                        :state="getValidationState('resourceHostId')"
                        :disabled="loading">
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
                    :compute-resource-policy="selectedComputeResourcePolicy"
                    :batch-queue-resource-policies="batchQueueResourcePolicies"
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
        groupResourceProfileId: {
            type: String,
            required: true
        }
    },
    data () {
        return {
            localComputationalResourceScheduling: this.value.clone(),
            computeResources: {},
            applicationDeployments: [],
            selectedGroupResourceProfileData: null,
            resourceHostId: null,
            // TODO: replace this with Loading spinner, better mechanism
            loadingCount: 0,
        }
    },
    components: {
        QueueSettingsEditor,
    },
    mounted: function () {
        this.loadApplicationDeployments(this.appModuleId, this.groupResourceProfileId);
        this.loadComputeResourceNames();
        this.loadGroupResourceProfile();
    },
    computed: {
        computeResourceOptions: function() {
            const computeResourceOptions = this.applicationDeployments.map(dep => {
                return {
                    value: dep.computeHostId,
                    text: dep.computeHostId in this.computeResources ? this.computeResources[dep.computeHostId] : "",
                }
            });
            computeResourceOptions.sort((a, b) => a.text.localeCompare(b.text));
            return computeResourceOptions;
        },
        loading: function() {
            return this.loadingCount > 0;
        },
        selectedComputeResourcePolicy: function() {
            if (this.selectedGroupResourceProfile === null) {
                return null;
            }
            return this.selectedGroupResourceProfile.computeResourcePolicies.find(crp => {
                return crp.computeResourceId === this.localComputationalResourceScheduling.resourceHostId;
            });
        },
        batchQueueResourcePolicies: function() {
            if (this.selectedGroupResourceProfile === null) {
                return null;
            }
            return this.selectedGroupResourceProfile.batchQueueResourcePolicies.filter(bqrp => {
                return bqrp.computeResourceId === this.localComputationalResourceScheduling.resourceHostId;
            });
        },
        selectedGroupResourceProfile: function() {
            // Reload selectedGroupResourceProfile when group-resource-profile-id changes
            if (this.selectedGroupResourceProfileData
                    && this.selectedGroupResourceProfileData.groupResourceProfileId !== this.groupResourceProfileId) {
                this.selectedGroupResourceProfileData = null;
                this.loadGroupResourceProfile();
            }
            return this.selectedGroupResourceProfileData;
        },
        appDeploymentId: function() {
            if (!this.resourceHostId) {
                return null;
            }
            // Find application deployment that corresponds to this compute resource
            let selectedApplicationDeployment = this.applicationDeployments.find(dep => dep.computeHostId === this.resourceHostId);
            if (!selectedApplicationDeployment) {
                throw new Error("Failed to find application deployment!");
            }
            return selectedApplicationDeployment.appDeploymentId;
        }
    },
    methods: {
        computeResourceChanged: function(selectedComputeResourceId) {
            this.localComputationalResourceScheduling.resourceHostId = selectedComputeResourceId;
            this.emitValueChanged();
        },
        loadApplicationDeployments: function(appModuleId, groupResourceProfileId) {
            this.loadingCount++;
            services.ServiceFactory.service("ApplicationDeployments").list({appModuleId: appModuleId, groupResourceProfileId: groupResourceProfileId})
                .then(applicationDeployments => {
                    this.applicationDeployments = applicationDeployments;
                })
                .then(()=> {this.loadingCount--;}, () => {this.loadingCount--;});
        },
        loadGroupResourceProfile: function() {
            this.loadingCount++;
            services.GroupResourceProfileService.get(this.groupResourceProfileId)
                .then(groupResourceProfile => {
                    this.selectedGroupResourceProfileData = groupResourceProfile;
                })
                .then(()=> {this.loadingCount--;}, () => {this.loadingCount--;});
        },
        loadComputeResourceNames: function() {
            this.loadingCount++;
            services.ServiceFactory.service("ComputeResources").names()
                .then(computeResourceNames => this.computeResources = computeResourceNames)
                .then(()=> {this.loadingCount--;}, () => {this.loadingCount--;});
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
        computeResourceOptions: function(newOptions) {
            // If the selected resourceHostId is not in the new list of
            // computeResourceOptions, reset it to null
            if (this.resourceHostId !== null && !newOptions.find(opt => opt.value === this.resourceHostId)) {
                this.resourceHostId = null;
            }
        },
        groupResourceProfileId: function(newGroupResourceProfileId) {
            this.loadApplicationDeployments(this.appModuleId, newGroupResourceProfileId);
        }
    }
}
</script>

<style>
</style>