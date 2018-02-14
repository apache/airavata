export default {
  mounted: function () {
    console.log("mounted")
    this.tabCreation()
  },
  beforeDestroy: function () {
    this.tabDestruction()
  },
  data: function () {
    return {
      data: {}
    }
  },
  methods: {
    tabCreation: function () {
      this.data = this.storeData
    },
    tabDestruction: function () {
      this.updateStore(this.data)
    },
  },
  watch: {
    storeData: function (newValue, oldValue) {
      console.log("Store Data Change")
      this.tabCreation();
    }
  }

}
