<template>
    <b-form-file :id="id" v-model="data"
        :placeholder="experimentInput.userFriendlyDescription"
        :state="componentValidState"
        @input="valueChanged"/>
</template>

<script>
import {InputEditorMixin} from 'django-airavata-workspace-plugin-api'
export default {
    name: 'file-input-editor',
    mixins: [InputEditorMixin],
}
</script>
