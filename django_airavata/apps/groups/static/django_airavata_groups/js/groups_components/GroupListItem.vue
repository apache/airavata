<template>
    <tr>
        <td>{{ group.name }}</td>
        <td><a href="#">View <i class="fa fa-bar-chart" aria-hidden="true"></i></a></td>
    </tr>
</template>

<script>
import moment from 'moment';

export default {
    name: 'group-list-item',
    props: ['group'],
    computed: {
    }
}
</script>

<style>
</style>
