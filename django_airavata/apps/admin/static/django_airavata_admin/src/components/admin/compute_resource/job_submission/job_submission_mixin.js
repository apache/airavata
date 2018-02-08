import {createNamespacedHelpers} from 'vuex'

const {mapGetters} = createNamespacedHelpers('computeResource')

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
  }, computed: {
    ...mapGetters({
      editable: 'editable',
    })
  },
  beforeDestroy: function () {
    this.updateData({data: this.data, id: this.id})
  }
}
