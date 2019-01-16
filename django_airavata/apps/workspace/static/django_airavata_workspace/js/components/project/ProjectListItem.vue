<template>
    <tr>
        <td>{{ project.name }}</td>
        <td>{{ project.owner }}</td>
        <td v-bind:title="project.creationTime">{{ creationTime }}</td>
        <td><a href="#">View <i class="fa fa-chart-bar" aria-hidden="true"></i></a></td>
    </tr>
</template>

<script>
import moment from 'moment';

export default {
    name: 'project-list-item',
    props: ['project'],
    computed: {
        creationTime: function () {
            var dt = new Date(this.project.creationTime);
            return moment(dt).fromNow();
        }
    }
}
</script>

<style>
</style>
