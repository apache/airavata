<template>
  <div class="row" v-if="showPGAUrl">
    <div class="col">
      <b-alert variant="info" show
        >You're using the new portal interface. To switch back to the old
        interface, go to
        <b-link :href="settings.pgaUrl">{{ settings.pgaUrl }}</b-link>
      </b-alert>
    </div>
  </div>
</template>

<script>
import { services } from "django-airavata-api";
export default {
  name: "pga-url",
  data() {
    return {
      settings: null,
    };
  },
  computed: {
    showPGAUrl() {
      return this.settings && this.settings.pgaUrl != null;
    },
  },
  beforeMount() {
    services.SettingsService.get().then((s) => {
      this.settings = s;
    });
  },
};
</script>

<style></style>
