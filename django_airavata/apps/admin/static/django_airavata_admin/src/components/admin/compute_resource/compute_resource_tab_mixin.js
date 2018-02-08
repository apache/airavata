import {createNamespacedHelpers} from 'vuex'

const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource')

export default {
  computed: {
    ...mapGetters({
      storeData: 'data',
      editable: 'editable',
      createBatchQueue: 'createBatchQueue'
    })
  },
  mounted: function () {
    this.tabCreation()
    if (this.editable == false) {
      let inputNodes = document.querySelectorAll('.main_section input,textarea')
      inputNodes.forEach((node) => node.readOnly = true)
    }

  },
  methods: {
    ...mapMutations(['updateStore', 'resetStore']),
    ...mapActions(['save']),
    cancel: function () {
      this.resetStore({resetFields: this.fields});
      this.tabCreation();
    },

  }
}
