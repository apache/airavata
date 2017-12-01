<template>
    <div class="col-md-6 col-xl-4 dashboard_item">
        <div class="card">
            <a class="card-link text-dark" v-bind:style="'min-height:'+ height+'px;'" v-on:click="$emit('edit')">
                <div class="card-body"  >
                    <h2 class="card-title h5">{{dashboard_item.appModuleName}}</h2>
                    <span class="badge badge-primary mr-1" v-for="tag in dashboard_item.tags">{{tag}}</span>
                    <span class="badge badge-primary mr-1" v-if="dashboard_item.appModuleVersion" >{{dashboard_item.appModuleVersion}}</span>
                    <p class="card-text card-text--small mt-3 text-secondary">{{dashboard_item.appModuleDescription}}</p>
                </div>
            </a>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'dashboard-item',
        props: ['dashboard_item','tags','height'],
        data:function () {
          return {
            "height":100
          };
        }
    }
</script>
<style>
  .dashboard_item:hover{
    cursor: pointer;
  }
</style>
