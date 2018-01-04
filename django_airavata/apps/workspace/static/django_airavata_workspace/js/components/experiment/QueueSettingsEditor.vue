<template>
    <b-form novalidate>

        <div class="card border-default">
            <div class="card-body">
                <h5 class="card-title">Settings for queue {{ localComputationalResourceScheduling.queueName }}</h5>
                <div class="row">
                    <div class="col">
                        <h1>{{ localComputationalResourceScheduling.nodeCount }}</h1>
                        <span>NODE COUNT</span>
                    </div>
                    <div class="col">
                        <h1>{{ localComputationalResourceScheduling.totalCPUCount }}</h1>
                        <span>CORE COUNT</span>
                    </div>
                    <div class="col">
                        <h1>{{ localComputationalResourceScheduling.wallTimeLimit }}</h1>
                        <span>TIME LIMIT</span>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="!showConfiguration">
            <i class="fa fa-cog" aria-hidden="true"></i>
            <a href="#" @click.prevent="showConfiguration = true">Configure Resource</a>
        </div>
        <div v-if="showConfiguration">
            <b-form-group label="Select a Queue" label-for="queue">
                <b-form-select id="queue"
                    v-model="localComputationalResourceScheduling.queueName"
                    :options="queueOptions" required
                    @change="queueChanged">
                </b-form-select>
            </b-form-group>
            <b-form-group label="Node Count" label-for="node-count">
                <b-form-input id="node-count" type="number" min="1"
                    v-model="localComputationalResourceScheduling.nodeCount" required
                    @change="emitValueChanged">
                </b-form-input>
            </b-form-group>
            <b-form-group label="Total Core Count" label-for="core-count">
                <b-form-input id="core-count" type="number" min="1"
                    v-model="localComputationalResourceScheduling.totalCPUCount" required
                    @change="emitValueChanged">
                </b-form-input>
            </b-form-group>
            <b-form-group label="Wall Time Limit" label-for="walltime-limit">
                <b-form-input id="walltime-limit" type="number" min="1"
                    v-model="localComputationalResourceScheduling.wallTimeLimit" required
                    @change="emitValueChanged">
                </b-form-input>
            </b-form-group>
            <div>
                <i class="fa fa-times" aria-hidden="true"></i>
                <a href="#" @click.prevent="showConfiguration = false">Hide Settings</a>
            </div>
        </div>
    </b-form>
</template>

<script>
import {models, services} from 'django-airavata-api'

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
                    text: `${queueDefault.queueName}: ${queueDefault.queueDescription}`,
                }
            });
            return queueOptions;
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
        }
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