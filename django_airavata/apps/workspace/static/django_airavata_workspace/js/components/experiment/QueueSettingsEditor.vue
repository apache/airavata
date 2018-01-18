<template>

    <div>
        <div class="row">
            <div class="col">
                <div class="card border-default">
                    <div class="card-body">
                        <h5 class="card-title mb-4">Settings for queue {{ localComputationalResourceScheduling.queueName }}</h5>
                        <div class="row">
                            <div class="col">
                                <h3 class="h5 mb-0">{{ localComputationalResourceScheduling.nodeCount }}</h3>
                                <span class="text-muted text-uppercase">NODE COUNT</span>
                            </div>
                            <div class="col">
                                <h3 class="h5 mb-0">{{ localComputationalResourceScheduling.totalCPUCount }}</h3>
                                <span class="text-muted text-uppercase">CORE COUNT</span>
                            </div>
                            <div class="col">
                                <h3 class="h5 mb-0">{{ localComputationalResourceScheduling.wallTimeLimit }}</h3>
                                <span class="text-muted text-uppercase">TIME LIMIT</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div v-if="!showConfiguration">
                    <i class="fa fa-cog text-secondary" aria-hidden="true"></i>
                    <a class="text-secondary" href="#" @click.prevent="showConfiguration = true">Configure Resource</a>
                </div>
            </div>
        </div>
        <div v-if="showConfiguration">
            <div class="row">
                <div class="col">
                    <b-form-group label="Select a Queue" label-for="queue"
                        :feedback="getValidationFeedback('queueName')"
                        :state="getValidationState('queueName')">
                        <b-form-select id="queue"
                            v-model="localComputationalResourceScheduling.queueName"
                            :options="queueOptions" required
                            @change="queueChanged"
                            :state="getValidationState('queueName')">
                        </b-form-select>
                        <div slot="description">
                            {{ selectedQueueDefault.queueDescription }}
                        </div>
                    </b-form-group>
                    <b-form-group label="Node Count" label-for="node-count"
                        :feedback="getValidationFeedback('nodeCount')"
                        :state="getValidationState('nodeCount')">
                        <b-form-input id="node-count" type="number" min="1"
                            :max="selectedQueueDefault.maxNodes"
                            v-model="localComputationalResourceScheduling.nodeCount" required
                            @input="emitValueChanged"
                            :state="getValidationState('nodeCount')">
                        </b-form-input>
                        <div slot="description">
                            <i class="fa fa-info-circle" aria-hidden="true"></i>
                            Max Allowed Nodes = {{ selectedQueueDefault.maxNodes }}
                        </div>
                    </b-form-group>
                    <b-form-group label="Total Core Count" label-for="core-count"
                        :feedback="getValidationFeedback('totalCPUCount')"
                        :state="getValidationState('totalCPUCount')">
                        <b-form-input id="core-count" type="number" min="1"
                            :max="selectedQueueDefault.maxProcessors"
                            v-model="localComputationalResourceScheduling.totalCPUCount" required
                            @input="emitValueChanged"
                            :state="getValidationState('totalCPUCount')">
                        </b-form-input>
                        <div slot="description">
                            <i class="fa fa-info-circle" aria-hidden="true"></i>
                            Max Allowed Cores = {{ selectedQueueDefault.maxProcessors }}
                        </div>
                    </b-form-group>
                    <b-form-group label="Wall Time Limit" label-for="walltime-limit"
                        :feedback="getValidationFeedback('wallTimeLimit')"
                        :state="getValidationState('wallTimeLimit')">
                        <b-input-group right="minutes">
                            <b-form-input id="walltime-limit" type="number" min="1"
                                :max="selectedQueueDefault.maxRunTime"
                                v-model="localComputationalResourceScheduling.wallTimeLimit" required
                                @input="emitValueChanged"
                                :state="getValidationState('wallTimeLimit')">
                            </b-form-input>
                        </b-input-group>
                        <div slot="description">
                            <i class="fa fa-info-circle" aria-hidden="true"></i>
                            Max Allowed Wall Time = {{ selectedQueueDefault.maxRunTime }}
                        </div>
                    </b-form-group>
                    <div>
                        <i class="fa fa-times text-secondary" aria-hidden="true"></i>
                        <a class="text-secondary" href="#" @click.prevent="showConfiguration = false">Hide Settings</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import {models, services} from 'django-airavata-api'
import {utils} from 'django-airavata-common-ui'

export default {
    name: 'queue-settings-editor',
    props: {
        value: {
            type: models.ComputationalResourceSchedulingModel,
            required: true
        },
        appDeploymentId: {
            type: String,
            required: true
        },
    },
    data () {
        return {
            localComputationalResourceScheduling: this.value.clone(),
            queueDefaults: [],
            showConfiguration: false,
        }
    },
    computed: {
        queueOptions: function() {
            const queueOptions = this.queueDefaults.map(queueDefault => {
                return {
                    value: queueDefault.queueName,
                    text: queueDefault.queueName,
                }
            });
            return queueOptions;
        },
        selectedQueueDefault: function() {
            return this.queueDefaults.find(queue => queue.queueName === this.localComputationalResourceScheduling.queueName);
        },
    },
    methods: {
        queueChanged: function(queueName) {

            const queueDefault = this.queueDefaults.find(queue => queue.queueName === queueName);
            this.localComputationalResourceScheduling.totalCPUCount = queueDefault.defaultCPUCount;
            this.localComputationalResourceScheduling.nodeCount = queueDefault.defaultNodeCount;
            this.localComputationalResourceScheduling.wallTimeLimit = queueDefault.defaultWalltime;
            this.emitValueChanged();
        },
        emitValueChanged: function() {
            this.$emit('input', this.localComputationalResourceScheduling);
        },
        loadQueueDefaults: function() {
            services.ApplicationDeploymentService.getQueues(this.appDeploymentId)
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

                    this.localComputationalResourceScheduling.queueName = defaultQueue.queueName;
                    this.localComputationalResourceScheduling.totalCPUCount = defaultQueue.defaultCPUCount;
                    this.localComputationalResourceScheduling.nodeCount = defaultQueue.defaultNodeCount;
                    this.localComputationalResourceScheduling.wallTimeLimit = defaultQueue.defaultWalltime;
                    this.emitValueChanged();
                });
        },
        getValidationFeedback: function(properties) {
            return utils.getProperty(this.localComputationalResourceScheduling.validate(this.selectedQueueDefault), properties);
        },
        getValidationState: function(properties) {
            return this.getValidationFeedback(properties) ? 'invalid' : null;
        },
    },
    watch: {
        value: function(newValue) {
            this.localComputationalResourceScheduling = newValue.clone();
        },
        appDeploymentId: function(appDeploymentId) {
            this.loadQueueDefaults();
        },
    },
    mounted: function() {
        this.loadQueueDefaults();
    }
}
</script>

<style>
</style>