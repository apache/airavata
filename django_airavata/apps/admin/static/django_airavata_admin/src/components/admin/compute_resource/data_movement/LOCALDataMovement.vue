<template>

</template>

<script>
  import {createNamespacedHelpers} from 'vuex'
  import dataMovementMixin from './data_movement_mixin'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/dataMovement')

  export default {
    name: "l-o-c-a-l-data-movement",
    mixins: [dataMovementMixin],
    computed: {
      ...mapGetters({
        storeData: 'localData'
      })
    },
    methods: {
      ...mapMutations({
        updateStore: 'updateLocal'
      })
    }
  }
</script>

<style scoped>

</style>
