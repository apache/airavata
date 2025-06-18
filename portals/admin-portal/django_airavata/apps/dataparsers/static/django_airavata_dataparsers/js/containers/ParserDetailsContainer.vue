<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">
          <div>{{ parserId }}</div>
        </h1>
      </div>
    </div>
    <div class="row" v-if="parser">
      <div class="col">
        <b-form-group label="Image Name" label-for="image-name">
          <b-form-input
            id="image-name"
            type="text"
            v-model="parser.imageName"
          />
        </b-form-group>
      </div>
    </div>
  </div>
</template>

<script>
import { services } from "django-airavata-api";

export default {
  name: "parser-details-container",
  props: {
    parserId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      parser: null,
    };
  },
  created() {
    services.ParserService.retrieve({
      lookup: this.parserId,
    }).then((parser) => (this.parser = parser));
  },
};
</script>
