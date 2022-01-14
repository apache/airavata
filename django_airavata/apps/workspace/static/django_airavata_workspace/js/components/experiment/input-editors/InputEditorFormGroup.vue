<template>
  <b-form-group
    :label="label"
    :label-for="labelFor"
    :state="state"
    :description="description"
  >
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
    <linkify slot="description">{{ description }}</linkify>
  </b-form-group>
</template>

<script>
import { components } from "django-airavata-common-ui";
export default {
  name: "input-editor-form-group",
  props: {
    label: {
      type: String,
      required: true
    },
    labelFor: {
      type: String,
      required: true
    },
    state: {
      type: Boolean
    },
    feedbackMessages: {
      type: Array
    },
    description: {
      type: String
    }
  },
  components: {
    linkify: components.Linkify
  }
};
</script>
