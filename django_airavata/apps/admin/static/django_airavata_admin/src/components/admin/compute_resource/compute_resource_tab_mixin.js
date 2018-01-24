import {createNamespacedHelpers} from 'vuex'

const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource')

export default {
  computed: {
    ...mapGetters({
      storeData: 'data',
      view:'view'
    })
  },
  methods: {
    ...mapMutations(['updateStore', 'resetStore']),
    ...mapActions(['save']),
    cancel: function () {
      this.resetStore();
      this.tabCreation();
    }
  }
}
