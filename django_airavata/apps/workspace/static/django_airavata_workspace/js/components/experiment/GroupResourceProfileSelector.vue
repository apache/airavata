<template>
    <div class="row">
        <div class="col">
            <b-form-group label="Allocation" label-for="group-resource-profile">
                <b-form-select id="group-resource-profile"
                    v-model="groupResourceProfileId"
                    :options="groupResourceProfileOptions" required
                    @change="groupResourceProfileChanged">
                    <template slot="first">
                        <option :value="null" disabled>Select an allocation</option>
                    </template>
                </b-form-select>
            </b-form-group>
        </div>
    </div>
</template>

<script>
import {services} from 'django-airavata-api'

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
    },
    methods: {
        loadGroupResourceProfiles: function() {
            services.GroupResourceProfileService.list()
                .then(groupResourceProfiles => {
                    this.groupResourceProfiles = groupResourceProfiles;
                    if ((!this.value || !this.selectedValueInGroupResourceProfileList(groupResourceProfiles)) && this.groupResourceProfiles && this.groupResourceProfiles.length > 0) {
                        // Automatically pick the first one for now if none selected
                        // TODO: automatically select the last one user selected
                        this.groupResourceProfileId = this.groupResourceProfiles[0].groupResourceProfileId;
                        this.emitValueChanged();
                    }
                })
        },
        groupResourceProfileChanged: function(groupResourceProfileId) {
            this.groupResourceProfileId = groupResourceProfileId
            this.emitValueChanged();
        },
        emitValueChanged: function() {
            this.$emit('input', this.groupResourceProfileId);
        },
        selectedValueInGroupResourceProfileList(groupResourceProfiles) {
          return groupResourceProfiles.map(grp => grp.groupResourceProfileId).indexOf(this.value) >= 0;
        }
    },
    watch: {
    }
}
</script>

<style>
</style>
