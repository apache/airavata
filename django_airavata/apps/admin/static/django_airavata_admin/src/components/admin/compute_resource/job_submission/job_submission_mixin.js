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
    let storeData = this.storeData(this.id)
    storeData.then((value) => this.data = value);
  }, computed: {
    ...mapGetters({
      editable: 'editable',
    })
  },
  beforeDestroy: function () {
    this.updateData({data: this.data, id: this.id})
  }
}
