<template>
    <b-form-group :label="experimentInput.name" :label-for="experimentInput.name"
        :feedback="validationFeedback" :state="validationState">
        <b-form-input :id="experimentInput.name" type="text" v-model="data"
            :placeholder="experimentInput.userFriendlyDescription"
            :state="validationState"
            @input="valueChanged"/>
    </b-form-group>
</template>

<script>
import {models} from 'django-airavata-api'
export default {
    name: 'string-input-editor',
    props: {
        value: {
            type: String,
            required: false,
        },
        experiment: {
            type: models.Experiment,
            required: true,
        },
        experimentInput: {
            type: models.InputDataObjectType,
            required: true,
        },
    },
    data () {
        return {
            data: this.value,
            inputHasBegun: false,
        }
    },
    computed: {
        validationResults: function() {
            return this.experimentInput.validate(this.experiment, this.data);
        },
        valid: function() {
            return Object.keys(this.validationResults).length === 0;
        },
        validationFeedback: function() {
            // Only display validation feedback after the user has provided
            // input so that missing required value errors are only displayed
            // after interacting with the input editor
            return this.inputHasBegun && 'value' in this.validationResults
                ? this.validationResults['value']
                : null;
        },
        validationState: function() {
            return this.inputHasBegun && 'value' in this.validationResults
                ? 'invalid'
                : null;
        },
    },
    methods: {
        valueChanged: function() {
            this.inputHasBegun = true;
            this.$emit('input', this.data);
            this.checkValidation();
        },
        checkValidation: function() {
            if (this.valid) {
                this.$emit('valid');
            } else {
                this.$emit('invalid');
            }
        }
    },
    mounted: function() {
        this.checkValidation();
    }
}
</script>