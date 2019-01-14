<template>
    <tr>
        <td>{{ parser.id }}</td>
        <td>{{ parser.imageName }}</td>
        <td>{{ parser.executionCommand }}</td>
    </tr>
</template>

<script>

import { services } from 'django-airavata-api'

export default {
    name: 'parser-list-item',
    data() {
      return {
        show: false,
        deleteButtonVariant: 'link',
        yesButtonVariant: 'danger',
        noButtonVariant: 'secondary',
        headerBgVariant: 'danger',
        bodyBgVariant: 'light',
        headerTextVariant: 'light',
        deleting: false,
      }
    },
    props: ['parser'],
    computed: {
    },
    methods: {
    }
}
</script>

<style>
</style>
