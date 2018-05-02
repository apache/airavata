<template>
    <input-editor-form-group :label="experimentInput.name" :label-for="experimentInput.name"
        :state="validationState" :feedback-messages="validationFeedback">
        <b-form-file :id="experimentInput.name" v-model="data"
            :placeholder="experimentInput.userFriendlyDescription"
            :state="validationState"
            @input="valueChanged"/>
    </input-editor-form-group>
</template>

<script>
import InputEditorFormGroup from './InputEditorFormGroup.vue'
import InputEditorMixin from './InputEditorMixin'
export default {
    name: 'file-input-editor',
    mixins: [InputEditorMixin],
    components: {
        InputEditorFormGroup,
    },
}
</script>
