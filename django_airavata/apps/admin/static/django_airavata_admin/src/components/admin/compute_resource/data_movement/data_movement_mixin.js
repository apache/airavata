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
    let storeData = this.storeData(this.id)
    storeData.then((value) => this.data = value);
  },
  beforeDestroy: function () {
    this.updateData({data: this.data, id: this.id})
  },
}
