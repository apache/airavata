<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">
                    Application Details
                </h1>
                <b-form-group label="Application Name" label-for="application-name">
                    <b-form-input id="application-name"
                    type="text" v-model="appModule.appModuleName" required
                    @input="emitChanged"></b-form-input>
                </b-form-group>
                <b-form-group label="Application Version" label-for="application-version">
                    <b-form-input id="application-version"
                    type="text" v-model="appModule.appModuleVersion"
                    @input="emitChanged"></b-form-input>
                </b-form-group>
                <b-form-group label="Application Description" label-for="application-description">
                    <b-form-textarea id="application-description"
                        v-model="appModule.appModuleDescription"
                        :rows="3"></b-form-textarea>
                </b-form-group>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <b-button variant="primary" @click="save">
                    Save
                </b-button>
                <b-button variant="secondary" @click="cancel">
                    Cancel
                </b-button>
            </div>
        </div>
    </div>
</template>

<script>
import {models} from 'django-airavata-api'

export default {
    name: 'application-module-editor',
    props: {
        value: {
            type: models.ApplicationModule,
            required: true,
        }
    },
    data: function() {
        return {
            appModule: this.value.clone(),
        }
    },
    methods: {
        emitChanged() {
            this.$emit('input', this.appModule);
        },
        save() {
            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        }
    },
    watch: {
        value: function(newValue) {
            this.appModule = newValue.clone();
        }
    }
}
</script>

