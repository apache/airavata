<template>
    <b-card title="Groups you are a member of:">  
        <p class="card-text">
            <b-table striped hover :items="items" :fields="fields"></b-table>
        </p>
         <b-pagination align="right" size="sm" :total-rows="totalRows" v-model="currentPage" :per-page="perPage">
    </b-pagination>
    </b-card>
</template>

<script>

let fields = { 'Name': {
  label: 'Group Name',
  sortable: true
}, 'Options': {
  label: 'Privileges',
}};

let items = [{ Name: 'group1', Options: '<a href="#"><i class="fa fa-times-circle fa-lg" aria-hidden="true"></i></a>' },
  { Name: 'group2', Options: '<a href=""><i class="fa fa-times-circle fa-lg" aria-hidden="true"></i></a>' },
  { Name: 'group3', Options: '<a href="#"><i class="fa fa-times-circle fa-lg" aria-hidden="true"></i></a>'},
  { Name: 'group4', Options: '<a href="#"><i class="fa fa-times-circle fa-lg" aria-hidden="true"></i></a>'}
  ];

export default {
    name: 'groups-member',
    props: ['groupsForMembers'],
    data () {
        return {
            items: items,
            fields: fields,
            currentPage: 1,
            totalRows: items.length,
            perPage: 10,
            test: this.groupsForMembers
        }
    }
}
</script>

<style>
</style>
