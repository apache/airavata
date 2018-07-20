<template>
    <b-form-input :id="id" type="text" v-model="data"
        :placeholder="experimentInput.userFriendlyDescription"
        :state="componentValidState"
        @input="valueChanged"/>
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