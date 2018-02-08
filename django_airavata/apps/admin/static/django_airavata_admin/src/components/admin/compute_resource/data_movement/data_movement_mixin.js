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
  computed: {
    ...mapGetters({
      editable: 'editable',
    })
  },
  mounted: function () {
    console.log("Before data", this.id, this.storeData)
    this.data = this.storeData(this.id)
  },
  beforeDestroy: function () {
    this.updateData({data: this.data, id: this.id})
  },
}
