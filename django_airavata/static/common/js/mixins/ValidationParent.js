/**
 * Aggregate validation state of child components. Child components should
 * dispatch 'valid' and 'invalid' events and component using this mixin should
 * call recordValidChildComponent or recordInvalidChildComponent, respectively.
 */
export default {
  data: function () {
    return {
      invalidChildComponents: [],
    };
  },
  computed: {
    childComponentsAreValid() {
      return this.invalidChildComponents.length === 0;
    },
  },
  methods: {
    recordInvalidChildComponent(childComponentId) {
      if (!this.invalidChildComponents.includes(childComponentId)) {
        this.invalidChildComponents.push(childComponentId);
      }
    },
    recordValidChildComponent(childComponentId) {
      if (this.invalidChildComponents.includes(childComponentId)) {
        const index = this.invalidChildComponents.indexOf(childComponentId);
        this.invalidChildComponents.splice(index, 1);
      }
    },
  },
};
