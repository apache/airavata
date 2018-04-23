<template>
    <b-form-group :label="experimentInput.name" :label-for="experimentInput.name"
        :feedback="validationFeedback" :state="validationState">
        <b-form-file :id="experimentInput.name" v-model="data"
            :placeholder="experimentInput.userFriendlyDescription"
            :state="validationState"
            @input="valueChanged"/>
    </b-form-group>
</template>

<script>
import InputEditorMixin from './InputEditorMixin'
export default {
    name: 'file-input-editor',
    mixins: [InputEditorMixin],
}
</script>
