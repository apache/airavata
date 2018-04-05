<template>
  <div id="temp">
    <div class="main_section compute_resource" v-if="home">
      <div class="entry">
        <input type="text" v-model="search" placeholder="Search"/>
      </div>
      <table>
        <thead>
        <th>Name</th>
        <th>ID</th>
        </thead>
        <tbody>
        <tr v-for="resource,index in resources" v-bind:key="index">
          <td><a href="" v-on:click.prevent="clickHandler(resource.host_id)">{{resource.host_id}}</a></td>
          <td>{{resource.host}}</td>
        </tr>
        </tbody>
      </table>
    </div>
    <compute-resource-details v-else></compute-resource-details>
  </div>
</template>
<script>
  import DjangoAiravataAPI from 'django-airavata-api'
  import ComputeResourceDetails from "../admin/compute_resource/ComputeResourceDetails";
  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource')
  export default {
    name: "compute-resource-dashboard",
    components: {
      ComputeResourceDetails
    },
    data: function () {
      let ret = {
        resources: [],
        allResources: [],
        search: "",
        home: true
      }
      return ret
    },
    mounted: function () {
      this.allResources = DjangoAiravataAPI.services.ComputeResourceService.list();
      this.resources = value;
    },
    methods: {
      clickHandler: function (hostID) {
        this.resetStore({})
        this.setComputeResourceId(hostID)
        this.fetch(true)
        this.home = false
      },
      ...mapMutations(['setComputeResourceId', 'resetStore']),
      ...mapActions(['fetch'])
    },
    watch: {
      search: function (newValue) {
        this.resources = this.allResources.filter((value) => value.host_id.toLowerCase().match(newValue.toLowerCase()) || value.host.toLowerCase().match(newValue.toLowerCase()));
      }
    }
  }
</script>

<style scoped>
  .compute_resource {
    width: 80%;
    background: white;
  }

  .compute_resource table {
    width: 100%;
  }

  .compute_resource thead {
  }

  table.compute_resource {
    border-radius: 10px;
  }

  .compute_resource a {
    color: black;
  }

  .compute_resource .entry {
    margin-bottom: 10px;
    padding-left: 30px;
    padding-right: 30px;
  }

  .compute_resource .entry input {
    text-align: center;
  }

  .compute_resource td {
    border-bottom: solid #999999 1px;
    text-align: center;
    height: 30px;
    width: 50%;
  }

  .compute_resource th {
    font-weight: bold;
    font-size: larger;
    color: white;
    background: #007BFF;
    height: 40px;
    border-bottom: solid #1d2124 2px;
    text-align: center;
    width: 50%;
    height: 50px;
  }
</style>
