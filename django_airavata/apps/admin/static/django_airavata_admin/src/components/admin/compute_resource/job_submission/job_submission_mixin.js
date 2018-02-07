export default {
  props: {
    id: {
      default: null
    }
  },
  data: function () {
    return {
      data: {}
    }
  },
  mounted: function () {
    this.data = this.storeData(this.id)
  },
  beforeDestroy: function () {
    this.updateData({data: this.data, id: this.id})
  }
}
