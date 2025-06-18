export default {
  data() {
    return {
      inlineOptions: [],
    };
  },
  computed: {
    allOptions() {
      // Copy options
      const result = this.options ? this.options.slice() : [];
      // Copy inlineOptions into result
      result.push(...this.inlineOptions);
      // return null if empty
      return result.length > 0 ? result : null;
    },
  },
  mounted() {
    this.$nextTick(() => {
      // Create default slot programmatically
      this.$refs.optionsSlot.append(document.createElement("slot"));
      this.readInlineOptions();
      this.addInlineOptionsChangeListener();
    });
  },
  destroyed() {
    this.removeInlineOptionsChangeListener();
  },
  methods: {
    readInlineOptions() {
      // Find options in slot and load them in
      const slot = this.$el.querySelector("slot");
      const els = slot.assignedElements();
      this.inlineOptions = [];
      for (const el of els) {
        if (el.tagName === "OPTION") {
          this.inlineOptions.push({ text: el.textContent, value: el.value });
        }
      }
    },
    addInlineOptionsChangeListener() {
      const slot = this.$el.querySelector("slot");
      // listen for changing options https://developer.mozilla.org/en-US/docs/Web/API/HTMLSlotElement#examples
      slot.addEventListener("slotchange", this.readInlineOptions);
    },
    removeInlineOptionsChangeListener() {
      const slot = this.$el.querySelector("slot");
      slot.removeEventListener("slotchange", this.readInlineOptions);
    },
  },
};
