@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/sharing.css') }}
@stop

@section('content')
<div class="col-md-offset-3 col-md-6">

    <h1>Create a new group</h1>

    <form action="{{URL::to('/')}}/group/create" method="POST" role="form" enctype="multipart/form-data">

        <?php

        $disabled = '';
        $groupName = '';
        $groupDescription = '';

        $echo = '';
        $wrf = '';
        ?>

        <div class="form-group required">
            <label for="group-name" class="control-label">Group Name</label>
            <input type="text" class="form-control" name="group-name" id="group-name"
                   placeholder="Enter group name" autofocus required="required" maxlength="50">
        </div>
        <div class="form-group">
            <label for="group-description">Group Description</label>
            <textarea class="form-control" name="group-description" id="group-description"
                      placeholder="Optional: Enter a short description of the group" maxlength="200"></textarea>
        </div>

        <div class="form-group">
            <label for="project-share">Select Group Members</label><br />
            <input id="share-box-filter" class="form-control" type="text" placeholder="Filter the user list" />
            <label>Show</label>
            <div id="show-results-group" class="btn-group" role="group" aria-label="Show Groups or Users">
                <button type="button" class="show-groups show-results-btn btn btn-primary">Groups</button>
                <button type="button" class="show-users show-results-btn btn btn-default">Users</button>
            </div>
            <label>Order By</label>
            <select class="order-results-selector">
                <option value="username">Username</option>
                <option value="firstlast">First, Last Name</option>
                <option value="lastfirst">Last, First Name</option>
                <option value="email">Email</option>
            </select>
            <ul id="share-box-users" class="form-control"></ul>
            <label>Members</label>
            <ul id="share-box-share" class="text-align-center form-control">
                <p>No members yet</p>
            </ul>
            <input id="share-settings" name="share-settings" type="hidden" value="" />
        </div>

        <div class="btn-toolbar">
            <input name="continue" type="submit" class="btn btn-primary" value="Save">
            <input name="clear" type="reset" class="btn btn-default" value="Clear">
        </div>
    </form>

</div>

{{ HTML::image("assets/Profile_avatar_placeholder_large.png", 'placeholder image', array('class' => 'baseimage')) }}

@stop

@section('scripts')
@parent
{{ HTML::script('js/sharing/sharing_utils.js') }}
{{ HTML::script('js/sharing/groups.js') }}
@stop
