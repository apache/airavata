import {createNamespacedHelpers} from 'vuex'

const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource')

export default {
  computed: {
    ...mapGetters({
      storeData: 'data',
      view: 'view',
      createBatchQueue: 'createBatchQueue'
    })
  },
  methods: {
    ...mapMutations(['updateStore', 'resetStore']),
    ...mapActions(['save']),
    cancel: function () {
      this.resetStore({resetFields: this.fields});
      this.tabCreation();
    }
  }
}
