@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/sharing.css') }}
@stop

@section('content')

<div class="container" style="max-width: 750px;">

    <?php if (Session::has("project_edited")) { ?>
        <div class="alert alert-success">
            The project has been edited
        </div>
        <?php Session::forget("project_edited");

    }
    ?>
    @if (isset($errorMessage))
        <div class="alert alert-danger">
            {{{ $errorMessage }}}
        </div>
    @endif


    <h1>Edit Project</h1>

    <form action="edit" method="post" role="form">
        <div class="form-group">
            <label for="project-name">Project Name</label>
            <input type="text"
                   class="form-control"
                   name="project-name"
                   id="project-name"
                   value="{{{ $project->name }}}" required maxlength="50">
        </div>
        <div class="form-group">
            <label for="project-description">Project Description</label>
            <textarea class="form-control"
                      name="project-description"
                      id="project-description" maxlength="200">{{{ $project->description }}}</textarea>
            <input type="hidden" name="projectId" value="{{{ $projectId }}}"/>
            <input type="hidden" name="projectOwner" value="{{{ $project->owner }}}"/>
        </div>

        <div class="form-group">
            @include('partials/sharing-display-body', array('form' => $canEditSharing))
        </div>

        <div class="btn-toolbar">
            <input name="save" type="submit" class="btn btn-primary" value="Save">
        </div>


    </form>


</div>

{{ HTML::image("assets/Profile_avatar_placeholder_large.png", 'placeholder image', array('class' => 'baseimage')) }}

@include('partials/sharing-form-modal', array("entityName" => "project"))

@stop

@section('scripts')
@parent
<script>
    var users = {{ $users }};
    var owner = {{ $owner }};
    $('#entity-share').data({url: "{{ URL::to('/') }}/project/unshared-users", resourceId: {{ json_encode($projectId) }}})
</script>
{{ HTML::script('js/sharing/sharing_utils.js') }}
{{ HTML::script('js/sharing/share.js') }}

@stop
