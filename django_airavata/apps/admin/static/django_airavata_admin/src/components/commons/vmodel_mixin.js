export default {
  watch: {
    data: function (newValue) {
      this.$emit('input', newValue)
    }
  },
  data:function () {
    return {
      data:this.value
    }
  },
  props: {
    value: {
      required: true
    }
  },
}
