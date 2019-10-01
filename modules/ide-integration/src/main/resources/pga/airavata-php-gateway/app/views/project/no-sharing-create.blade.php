@extends('layout.basic')
@section('page-header')
@parent
@stop

@section('content')

<div class="container" style="max-width: 750px">

    <h1>Create a new project</h1>


    <form action="create" method="post" role="form" class="project-creation-form">
        <div class="form-group required">
            <label for="project-name" class="control-label">Project Name</label>
            <input type="text" class="form-control projectName" name="project-name" id="project-name"
                   placeholder="Enter project name" autofocus required maxlength="50">
        </div>

        <div class="form-group">
            <label for="project-description">Project Description</label>
            <textarea class="form-control" name="project-description" id="project-description"
                      placeholder="Optional: Enter a short description of the project" maxlength="200"></textarea>
        </div>

        <input name="save" type="submit" class="btn btn-primary create-project" value="Save">
        <input name="clear" type="reset" class="btn btn-default" value="Clear">

    </form>

</div>

@stop
