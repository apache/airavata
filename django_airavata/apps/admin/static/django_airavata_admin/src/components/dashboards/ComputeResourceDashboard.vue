<template>
  <div>
    <table class="compute_resource">
      <thead>
      <th>Name</th>
      <th>id</th>
      </thead>
      <tbody>
      <tr v-for="host in data">
        <td><a v-on:click="clickHandler(host.host_id)">{{host.host_id}}</a></td>
        <td>{{host.host}}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>

  import ComputeResourceHome from "../admin/compute_resource/ComputeResourceHome";
  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource')
  export default {
    name: "compute-resource-dashboard",
    components: {
      ComputeResourceHome
    },
    data: function () {
      return {}
    },
    methods: {
      clickHandler: function (hostID) {
        this.resetStore()
        this.setComputeResourceId(hostID)
        this.fetch(true)
        this.$router.push({name: 'compute_resource_details'})
      },
      ...mapMutations(['setComputeResourceId', 'fetch','resetStore'])
    }
  }
</script>

<style scoped>
compute_resource {
  width: 80%;
}
</style>
