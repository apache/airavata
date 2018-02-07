import {createNamespacedHelpers} from 'vuex'

const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/dataMovement')

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
    console.log("Before",this.id,this.storeData)
    this.data = this.storeData(this.id)
  },
  beforeDestroy: function () {
    this.updateData({data: this.data, id: this.id})
  },
}
