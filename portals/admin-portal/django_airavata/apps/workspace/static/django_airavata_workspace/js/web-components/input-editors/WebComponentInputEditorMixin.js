
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
    name: String,
  },
  store: store,
  mounted() {
    this.$nextTick(() => {
      for (const key of Object.keys(this.$props)) {
        // workaround for issues around setting props before WC connected,
        // see https://github.com/vuejs/vue-web-component-wrapper/pull/81

        // copy properties set on host element to wrapper component
        // (mostly this is done so that the options array can be set by client code)
        this.$parent.props[key] = this.$el.getRootNode().host[key];
      }
    })
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
    valueChanged(value) {
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
  watch: {
    value(value) {
      this.data = value;
    },
  },
};
