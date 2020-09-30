<template>
  <parser-editor
    v-if="parser"
    :parser="parser"
    @saved="handleSaved"
    @cancelled="handleCancelled"
  ></parser-editor>
</template>

<script>
import ParserEditor from "../parser-components/ParserEditor.vue";

import { services } from "django-airavata-api";

export default {
  name: "parser-edit-container",
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
  components: {
    ParserEditor,
  },
  methods: {
    handleSaved: function () {
      window.location.assign("/dataparsers/");
    },
    handleCancelled: function () {
      window.location.assign("/dataparsers/");
    },
  },
  computed: {},
  mounted: function () {
    services.ParserService.retrieve({ lookup: this.parserId }).then(
      (parser) => (this.parser = parser)
    );
  },
};
</script>
