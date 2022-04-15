<template>
  <b-form-group
    :label="extendedUserProfileField.name"
    :description="extendedUserProfileField.help_text"
  >
    <b-form-input v-model="value" />
  </b-form-group>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
export default {
  props: ["extendedUserProfileField"],
  computed: {
    ...mapGetters('extendedUserProfile', ['getTextValue']),
    value: {
      get() {
        return this.getTextValue(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setTextValue({value, id: this.extendedUserProfileField.id})
      }
    }
  },
  methods: {
    ...mapMutations('extendedUserProfile', ['setTextValue']),
  }
};
</script>

<style></style>
