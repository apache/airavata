<template>
    <b-form-group :label="label" :label-for="labelFor" :state="state" :description="description">
        <slot></slot>
        <template slot="invalid-feedback">
            <ul v-if="feedbackMessages && feedbackMessages.length > 1">
                <li v-for="feedback in feedbackMessages" :key="feedback">{{ feedback }}</li>
            </ul>
            <div v-else-if="feedbackMessages && feedbackMessages.length === 1">
                {{ feedbackMessages[0] }}
            </div>
        </template>
    </b-form-group>
</template>

<script>

export default {
    name: 'input-editor-form-group',
    props: {
        label: {
            type: String,
            required: true,
        },
        labelFor: {
            type: String,
            required: true,
        },
        state: {
            type: String,
        },
        feedbackMessages: {
            type: Array,
        },
        description: {
          type: String,
        }
    },
}
</script>
