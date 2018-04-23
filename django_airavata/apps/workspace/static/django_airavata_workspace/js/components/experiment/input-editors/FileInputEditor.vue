<template>
    <b-form-group :label="experimentInput.name" :label-for="experimentInput.name">
        <b-form-file :id="experimentInput.name" v-model="data" required
            :placeholder="experimentInput.userFriendlyDescription"
            @input="emitValueChanged"/>
    </b-form-group>
</template>

<script>
import {models} from 'django-airavata-api'
export default {
    name: 'file-input-editor',
    props: {
        value: {
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
        }
    },
    methods: {
        emitValueChanged: function() {
            this.$emit('input', this.data);
        },
    },
}
</script>
