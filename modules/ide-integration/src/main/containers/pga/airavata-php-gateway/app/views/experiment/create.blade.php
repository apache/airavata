@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/sharing.css') }}
@stop

@section('content')

@if( Session::has("error-message" ) )
    <div class="alert alert-danger">
        {{{ Session::get("error-message") }}}
    </div>
@endif

<div class="col-md-offset-3 col-md-6">

    <h1>Create a new experiment</h1>

    <form action="{{URL::to('/')}}/experiment/create" method="POST" role="form" enctype="multipart/form-data">

        <?php

        $disabled = '';
        $experimentName = '';
        $experimentDescription = '';
        $project = '';
        $application = '';

        $echo = '';
        $wrf = '';
        ?>

        <div class="form-group required">
            <label for="experiment-name" class="control-label">Experiment Name</label>
            <input type="text" class="form-control" name="experiment-name" id="experiment-name"
                   placeholder="Enter experiment name" autofocus required="required" maxlength="50">
        </div>
        <div class="form-group">
            <label for="experiment-description">Experiment Description</label>
            <textarea class="form-control" name="experiment-description" id="experiment-description"
                      placeholder="Optional: Enter short description of the experiment" maxlength="200"></textarea>
        </div>
        <div class="form-group required">
            <label for="project" class="control-label">Project</label>


            {{ ProjectUtilities::create_project_select($project, !$disabled) }}

        </div>
        <div class="form-group">
            <label for="application">Application</label>

            {{ ExperimentUtilities::create_application_select($application, !$disabled) }}

        </div>

        <div class="btn-toolbar">
            <input name="continue" type="submit" class="btn btn-primary" value="Continue">
            <input name="clear" type="reset" class="btn btn-default" value="Reset values">
        </div>
    </form>

</div>

{{ HTML::image("assets/Profile_avatar_placeholder_large.png", 'placeholder image', array('class' => 'baseimage')) }}

@stop

@section('scripts')
@parent
@stop
