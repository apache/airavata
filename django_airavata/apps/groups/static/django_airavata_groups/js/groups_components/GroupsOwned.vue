<template>
    <b-card title="Owned Groups:">
        <p class="card-text">
            <b-table striped hover :items="items" :fields="fields"></b-table>
        </p>
        <b-pagination align="right" size="sm" :total-rows="totalRows" v-model="currentPage" :per-page="perPage"></b-pagination>
        
    </b-card>
</template>

<script>

let fields = { 'Name': {
  label: 'Group Name',
  sortable: true
}, 'Options': {
  label: 'Privileges',
}};

let items = [{ Name: 'first-group', Options: '<a href="#"><i class="fa fa-pencil-square fa-lg"></i></a>&nbsp;&nbsp;<a href="#"><i class="fa fa-trash fa-lg"></i></a>' },
  { Name: 'second-group', Options: '<a href="#"><i class="fa fa-pencil-square fa-lg"></i></a>&nbsp;&nbsp;<a href="#"><i class="fa fa-trash fa-lg"></i></a>' },
  { Name: 'thrid-group', Options: '<a href="#"><i class="fa fa-pencil-square fa-lg"></i></a>&nbsp;&nbsp;<a href="#"><i class="fa fa-trash fa-lg"></i></a>'},
  { Name: 'fourth-group', Options: '<a href="#"><i class="fa fa-pencil-square fa-lg"></i></a>&nbsp;&nbsp;<a href="#"><i class="fa fa-trash fa-lg"></i></a>'}];

export default {
    name: 'groups-owned',
    data () {
        return {
            items: items,
            fields: fields,
            currentPage: 1,
            totalRows: items.length,
            perPage: 10
        }
    }
}
</script>

<style>
</style>
