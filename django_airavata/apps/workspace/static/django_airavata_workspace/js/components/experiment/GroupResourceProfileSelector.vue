<template>
    <div class="row">
        <div class="col">
            <b-form-group label="Allocation" label-for="group-resource-profile">
                <b-form-select id="group-resource-profile"
                    v-model="groupResourceProfileId"
                    :options="groupResourceProfileOptions" required
                    @changed="groupResourceProfileChanged"
                    :disabled="loading">
                    <template slot="first">
                        <option :value="null" disabled>Select an allocation</option>
                    </template>
                </b-form-select>
            </b-form-group>
        </div>
    </div>
</template>

<script>
import {models, services} from 'django-airavata-api'
import {utils} from 'django-airavata-common-ui'

export default {
    name: 'group-resource-profile-selector',
    props: {
        value: {
            type: String,
        },
    },
    data () {
        return {
            groupResourceProfileId: this.value,
            groupResourceProfiles: [],
            // TODO: replace this with Loading spinner, better mechanism
            loadingCount: 0,
        }
    },
    mounted: function () {
        this.loadGroupResourceProfiles();
    },
    computed: {
        groupResourceProfileOptions: function() {
            if (this.groupResourceProfiles && this.groupResourceProfiles.length > 0) {
                const groupResourceProfileOptions = this.groupResourceProfiles.map(groupResourceProfile => {
                    return {
                        value: groupResourceProfile.groupResourceProfileId,
                        text: groupResourceProfile.groupResourceProfileName,
                    }
                });
                groupResourceProfileOptions.sort((a, b) => a.text.localeCompare(b.text));
                return groupResourceProfileOptions;
            } else {
                return [];
            }
        },
        loading: function() {
            return this.loadingCount > 0;
        },
    },
    methods: {
        loadGroupResourceProfiles: function() {
            this.loadingCount++;
            services.GroupResourceProfileService.list()
                .then(groupResourceProfiles => {
                    this.groupResourceProfiles = groupResourceProfiles;
                    if (this.groupResourceProfiles && this.groupResourceProfiles.length > 0) {
                        // Automatically pick the first one for now
                        // TODO: automatically select the last one user selected
                        this.groupResourceProfileId = this.groupResourceProfiles[0].groupResourceProfileId;
                        this.emitValueChanged();
                    }
                })
                .then(()=> {this.loadingCount--;}, () => {this.loadingCount--;});
        },
        groupResourceProfileChanged: function() {
            this.emitValueChanged();
        },
        emitValueChanged: function() {
            this.$emit('input', this.groupResourceProfileId);
        },
    },
    watch: {
    }
}
</script>

<style>
</style>