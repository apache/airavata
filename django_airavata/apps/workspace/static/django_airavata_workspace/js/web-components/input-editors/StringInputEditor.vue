<template>
  <!-- NOTE: experimentInput is late bound, don't create component until it is available -->
  <div>
    <string-input-editor
      ref="inputEditor"
      v-if="experimentInput"
      :id="id"
      :value="data"
      :experiment-input="experimentInput"
      :read-only="readOnly"
      @input="onInput"
    />
  </div>
</template>

<script>
import StringInputEditor from "../../components/experiment/input-editors/StringInputEditor.vue";
import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
import AsyncComputed from "vue-async-computed";
import { utils } from "django-airavata-common-ui";
import store from "../store";
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
  store: store,
  mounted() {
    this.$nextTick(() => {
      // Stop wrapped input editor 'input' event from bubbling up so it doesn't
      // conflict with this component's 'input' event. (see #onInput)
      this.$refs.inputEditor.$el.addEventListener('input', this.stopPropagation);
    })
  },
  destroyed() {
    this.$refs.inputEditor.$el.removeEventListener('input', this.stopPropagation);
  },
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
    stopPropagation(event) {
      event.stopPropagation();
    }
  },
};
</script>

<style>
@import "../styles.css";
</style>
