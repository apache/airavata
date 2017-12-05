<template>
    <div class="col-md-6 col-xl-4">
        <div class="card application-card">
            <a href="#" class="card-link text-dark" @click.prevent="handleAppClick">
                <div class="card-body"  >
                    <h2 class="card-title h5">{{appModule.appModuleName}}</h2>
                    <span class="badge badge-primary mr-1" v-for="tag in appModule.tags">{{tag}}</span>
                    <span class="badge badge-primary mr-1" v-if="appModule.appModuleVersion" >{{appModule.appModuleVersion}}</span>
                    <p class="card-text card-text--small mt-3 text-secondary">{{appModule.appModuleDescription}}</p>
                </div>
            </a>
        </div>
    </div>
</template>

<script>
export default {
    name: 'application-card',
    props: ['appModule'],
    data:function () {
        return {
        };
    },
    methods: {
        handleAppClick: function(event) {
            this.$emit('app-selected', this.appModule);
        }
    }
}
</script>

<style>
.application-card {
    height: calc(100% - 30px); /* 30px margin at the botton */
}
.application-card .card-link, .application-card .card-body {
    height: 100%;
}
</style>
