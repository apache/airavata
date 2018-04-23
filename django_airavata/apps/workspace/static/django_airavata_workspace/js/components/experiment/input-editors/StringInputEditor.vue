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
import InputEditorMixin from './InputEditorMixin'

export default {
    name: 'string-input-editor',
    mixins: [InputEditorMixin],
    props: {
        value: {
            type: String,
            required: true,
        },
    },
}
</script>