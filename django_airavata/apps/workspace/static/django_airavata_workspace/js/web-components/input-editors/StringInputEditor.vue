<template>
<!-- NOTE: regarding v-if="ready": experimentInput/experiment/id are late bound,
     don't create component until they is available -->
  <string-input-editor
    v-if="ready"
    :id="id"
    :value="data"
    :experiment-input="experimentInput"
    :experiment="experiment"
    :read-only="readOnly"
    @input="onInput"
  />
</template>

<script>
import StringInputEditor from "../../components/experiment/input-editors/StringInputEditor.vue";
import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
import AsyncComputed from "vue-async-computed";
Vue.use(BootstrapVue);
Vue.use(AsyncComputed);

export default {
  props: {
    value: String,
    experimentInput: Object,
    experiment: Object,
    readOnly: Boolean,
    id: String,
  },
  components: {
    StringInputEditor,
  },
  data() {
    return {
      data: this.value,
    };
  },
  computed: {
    ready() {
      return this.experiment && this.experimentInput && this.id;
    }
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
    }
  },
};
</script>

<style>
@import "../styles.css";
</style>
