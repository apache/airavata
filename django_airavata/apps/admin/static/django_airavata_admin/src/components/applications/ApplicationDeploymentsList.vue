<template>
    <div>
        Application Deployments List
        <router-link :to="{name: 'application_deployment', params: {id: 'foo', deployment_id: 'bar'}}">Deployment</router-link>
    </div>
</template>

<script>
export default {
    name: 'application-deployments-list'
}
</script>

