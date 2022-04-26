<template>
  <b-form-group
    :label="extendedUserProfileField.name"
    :description="extendedUserProfileField.help_text"
  >
    <b-card
      v-for="link in extendedUserProfileField.links"
      :key="link.id"
      :header="link.label"
    >
      <b-card-text v-if="link.display_inline">
        <iframe :src="link.url" />
      </b-card-text>
      <a
        v-if="link.display_link"
        :href="link.url"
        target="_blank"
        class="card-link"
        >Open '{{ link.label }}' in separate tab.</a
      >
    </b-card>
    <slot />
  </b-form-group>
</template>

<script>
export default {
  props: ["extendedUserProfileField"],
};
</script>

<style scoped>
iframe {
  border: none;
  width: 100%;
  height: 50vh;
}
</style>
