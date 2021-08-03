<template>
  <!-- NOTE: experimentInput is late bound, don't create component until it is available -->
  <string-input-editor
    v-if="experimentInput"
    :id="id"
    :value="data"
    :experiment-input="experimentInput"
    :read-only="readOnly"
    @input="onInput"
  />
</template>

<script>
import StringInputEditor from "../../components/experiment/input-editors/StringInputEditor.vue";
import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
import AsyncComputed from "vue-async-computed";
import { utils } from "django-airavata-common-ui";
import vuestore from "../vuestore";
Vue.use(BootstrapVue);
Vue.use(AsyncComputed);

export default {
  props: {
    value: String,
    // experimentInput: Object,
    name: String,
  },
  components: {
    StringInputEditor,
  },
  store: vuestore,
  data() {
    return {
      data: this.value,
    };
  },
  computed: {
    readOnly() {
      return this.experimentInput.isReadOnly;
    },
    id() {
      return utils.sanitizeHTMLId(this.experimentInput.name);
    },
    experimentInput() {
      return this.$store.getters.getExperimentInputByName(this.name);
    },
  },
  methods: {
    onInput(value) {
      if (value !== this.data) {
        this.data = value;
        const inputEvent = new CustomEvent("input", {
          detail: [this.data],
          composed: true,
          bubbles: true,
        });
        this.$el.dispatchEvent(inputEvent);
      }
    },
  },
};
</script>

<style>
@import "../styles.css";
</style>
