<template>
  <b-form>
    <template v-for="item in items">
      <validated-form-group
        :label="item.label"
        :key="item.key"
        :valid="isValid(item.key)"
        :feedback-messages="getFeedbackMessages(item.key)"
        :description="item.description"
      >
        <slot
          :item="item.item"
          :valid="() => setValid(item.key)"
          :invalid="(messages) => setInvalid(item.key, messages)"
        />
      </validated-form-group>
    </template>
  </b-form>
</template>

<script>
import ValidatedFormGroup from "./ValidatedFormGroup";

export default {
  name: "validated-form",
  props: {
    items: {
      type: Array,
      required: true,
    },
  },
  components: {
    ValidatedFormGroup,
  },
  data() {
    return {
      invalidFormItems: [],
      feedbackMessages: {},
    };
  },
  computed: {
    valid() {
      return this.invalidFormItems.length === 0;
    },
  },
  methods: {
    setValid(key) {
      const wasValid = this.valid;
      if (this.invalidFormItems.includes(key)) {
        const index = this.invalidFormItems.indexOf(key);
        this.invalidFormItems.splice(index, 1);
      }
      if (!wasValid && this.valid) {
        this.$emit("valid");
      }
    },
    setInvalid(key, messages) {
      const wasValid = this.valid;
      if (!this.invalidFormItems.includes(key)) {
        this.invalidFormItems.push(key);
      }
      if (typeof messages === "string") {
        this.feedbackMessages[key] = [messages];
      } else {
        this.feedbackMessages[key] = messages;
      }
      if (wasValid) {
        this.$emit("invalid");
      }
    },
    isValid(key) {
      return !this.invalidFormItems.includes(key);
    },
    getFeedbackMessages(key) {
      if (key in this.feedbackMessages) {
        return this.feedbackMessages[key];
      } else {
        return [];
      }
    },
  },
};
</script>
