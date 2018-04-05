<template>
  <array-component-view v-bind:homeAction="createHomeAction()" v-bind:maxIndex="value.computePreferences.length-1"
                        v-model="currentIndex">

      <compute-preference v-model="value.computePreferences[currentIndex]" v-bind:key="currentIndex"></compute-preference>
  </array-component-view>
</template>

<script>
  import ArrayComponentView from '../../commons/ArrayComponentView'
  import ComputePreference from './ComputePreference';
  import VModelMixin from '../../commons/vmodel_mixin'

  export default {
    name: "compute-preferences",
    components: {
      ArrayComponentView,
      ComputePreference
    },
    mixins: [VModelMixin],
    props: {
      index: {
        type: Number,
        default: 0
      }
    },
    data: function () {
      return {
        currentIndex: this.index
      }
    },
    methods: {
      createMovableAction: function () {
        return (index) => {
          this.currentIndex = index
        }
      },
      createHomeAction: function () {
        return () => this.$router.push({
          name: 'group_resource_preference', params: {
            data: this.value
          }
        })
      }
    },
  }
</script>

<style scoped>

</style>
