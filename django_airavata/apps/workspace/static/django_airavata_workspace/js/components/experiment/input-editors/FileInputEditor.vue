<template>
    <b-form-file :id="id" v-model="data"
        :placeholder="experimentInput.userFriendlyDescription"
        :state="componentValidState"
        @input="valueChanged"/>
</template>

<script>
import InputEditorMixin from './InputEditorMixin'
export default {
    name: 'file-input-editor',
    mixins: [InputEditorMixin],
}
</script>
