<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">
                    <div v-if="appModule" class="application-name text-muted text-uppercase"><i class="fa fa-code" aria-hidden="true"></i> {{ appModule.appModuleName }}</div>
                    <slot name="title">Experiment Editor</slot>
                </h1>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <b-form novalidate>
                            <b-form-group label="Experiment Name" label-for="experiment-name">
                                <b-form-input id="experiment-name"
                                type="text" v-model="experiment.experimentName" required
                                placeholder="Experiment name"></b-form-input>
                            </b-form-group>
                        </b-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'edit-experiment',
    props: ['experiment', 'appModule'],
    data () {
        return {
        }
    },
}
</script>

<style>
.application-name {
    font-size: 12px;
}
</style>