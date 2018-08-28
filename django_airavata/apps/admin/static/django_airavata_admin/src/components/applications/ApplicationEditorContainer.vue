<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">
                    {{ title }}
                </h1>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <b-nav tabs>
                    <b-nav-item exact-active-class="active" exact :to="{name: id ? 'application_module' : 'new_application_module', params: {id: id}}">Details</b-nav-item>
                    <b-nav-item exact-active-class="active" exact :to="{name: 'application_interface', params: {id: id}}" :disabled="!id">Interface</b-nav-item>
                    <b-nav-item active-class="active" :to="{name: 'application_deployments', params: {id: id}}" :disabled="!id">Deployments</b-nav-item>
                </b-nav>
                <router-view name="module"/>
                <router-view name="interface"/>
                <router-view name="deployments"/>
                <router-view name="deployment"/>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'application-editor-container',
    props: {
        id: String
    },
    data: function() {
        return {
            module: null,
            interface: null,
            deployments: null,
            deployment: null,
        }
    },
    computed: {
        title: function() {
            if (this.id) {

                return this.module && this.module.appModuleName ? this.module.appModuleName : "";
            } else {
                return "Create a New Application";
            }
        }
    }
}
</script>

<style>
/* style the containing div, in base.html template */
/* .main-content {
    background-color: #ffffff;
} */
</style>

