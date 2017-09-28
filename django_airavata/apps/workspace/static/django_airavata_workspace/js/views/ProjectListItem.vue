<template>
    <li>{{ project.name }}</li>
</template>

<script>
export default {
    name: 'project-list-item',
    props: ['project'],
    data: function () {
        return {
        }
    }
}
</script>

<style>
</style>
