<template>
    <b-form-input :id="id" type="text" v-model="data"
        :state="componentValidState"
        @input="valueChanged"/>
</template>

<script>
import {InputEditorMixin} from 'django-airavata-workspace-plugin-api'

export default {
    name: 'string-input-editor',
    mixins: [InputEditorMixin],
    props: {
        value: {
            type: String,
        },
    },
}
</script>
