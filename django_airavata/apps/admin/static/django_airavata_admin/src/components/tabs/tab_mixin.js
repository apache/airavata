export default {
  mounted: function () {
    this.tabCreation()
  },
  beforeDestroy: function () {
    this.tabDestruction()
  },
  data:function () {
    return {
      data:{

      }
    }
  },
  methods: {
    tabCreation: function () {
      this.data = this.storeData
    },
    tabDestruction: function () {
      this.updateStore(this.data)
    }
  }

}
