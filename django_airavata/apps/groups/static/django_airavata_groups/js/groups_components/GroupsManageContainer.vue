<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">Groups you own:</h1>
            </div>
            <div id="col-new-group" class="col-sm-2">
                <group-button-new @new-group="onNewGroup"/>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <group-owner-list v-bind:groupsForOwners="groupsOwners"></group-owner-list>
                        <pager v-bind:paginator="groupOwnersPaginator"
                        v-on:next="nextOwnerGroups" v-on:previous="previousOwnerGroups"></pager>
                    </div>
                </div>
                <h1 class="h4 mb-4">Groups you are a part of:</h1>
                <div class="card">
                    <div class="card-body">
                        <group-member-list v-bind:groupsForMembers="groupsMembers"></group-member-list>
                        <pager v-bind:paginator="groupMembersPaginator"
                        v-on:next="nextMemberGroups" v-on:previous="previousMemberGroups"></pager>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

import GroupButtonNew from './GroupButtonNew.vue'
import GroupOwnerList from './GroupOwnerList.vue';
import GroupMemberList from './GroupMemberList.vue';

import { models, services } from 'django-airavata-api'
import { components as comps } from 'django-airavata-common-ui'

export default {
    name: 'groups-manage-container',
    props: ['groupsDataOwners', 'groupsDataMembers'],
    data () {
        return {
            groupOwnersPaginator: null,
            groupMembersPaginator: null,
        }
    },
    components: {
        'group-owner-list': GroupOwnerList,
        'group-member-list': GroupMemberList,
        'group-button-new': GroupButtonNew,
        'pager': comps.Pager,
    },
    methods: {
        nextMemberGroups: function(event) {
            this.groupMembersPaginator.next();
        },
        previousMemberGroups: function(event) {
            this.groupMembersPaginator.previous();
        },
        nextOwnerGroups: function(event) {
            this.groupOwnersPaginator.next();
        },
        previousOwnerGroups: function(event) {
            this.groupOwnersPaginator.previous();
        },
        onNewGroup: function(group) {
            services.GroupOwnerService.list()
                .then(result => this.groupOwnersPaginator = result);
        },
    },
    computed: {
        groupsOwners: function() {
            return this.groupOwnersPaginator ? this.groupOwnersPaginator.results : null;
        },
        groupsMembers: function() {
            return this.groupMembersPaginator ? this.groupMembersPaginator.results : null;
        },
    },
    beforeMount: function () {
        services.GroupMemberService.list(this.groupsDataMembers)
            .then(result => this.groupMembersPaginator = result);

        services.GroupOwnerService.list(this.groupsDataOwners)
            .then(result => this.groupOwnersPaginator = result);
    },
}
</script>

<style>
#col-new-group {
    text-align: right;
}
#modal-new-group {
    text-align: left;
}
</style>
