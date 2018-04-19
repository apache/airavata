export default {
  watch: {
    data: {
      handler: function (newValue) {
        this.$emit('input', newValue)
      },
      deep:true
    }
  },
  data: function () {
    return {
      data: this.value
    }
  },
  props: {
    value: {
      required: true
    }
  },
}
