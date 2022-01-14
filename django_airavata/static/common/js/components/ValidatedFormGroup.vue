<template>
  <b-form-group :label="label" :state="state" :description="description">
    <slot></slot>
    <template slot="invalid-feedback">
      <ul v-if="feedbackMessages && feedbackMessages.length > 1">
        <li v-for="feedback in feedbackMessages" :key="feedback">
          {{ feedback }}
        </li>
      </ul>
      <div v-else-if="feedbackMessages && feedbackMessages.length === 1">
        {{ feedbackMessages[0] }}
      </div>
    </template>
  </b-form-group>
</template>

<script>
export default {
  name: "validated-form-group",
  props: {
    label: {
      type: String,
      required: true,
    },
    description: {
      type: String,
    },
    valid: {
      type: Boolean,
      required: true,
    },
    feedbackMessages: {
      type: Array,
      required: true,
    },
  },
  computed: {
    state() {
      return this.valid ? null : false;
    },
  },
};
</script>
