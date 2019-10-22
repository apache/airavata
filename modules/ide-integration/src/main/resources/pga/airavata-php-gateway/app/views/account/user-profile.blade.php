
@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/user-settings.css')}}
@stop

@section('content')
<div class="container">
    <ol class="breadcrumb">
        <li><a href="{{ URL::to('/') }}/account/settings">User Settings</a></li>
        <li class="active">Your Profile</li>
    </ol>

    @if( Session::has("message") )
        <div class="alert alert-success alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert"><span
                    aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
            {{{ Session::get("message") }}}
        </div>
    @endif

    @if( isset($errorMessage) )
        <div class="alert alert-danger" role="alert">
            {{{ $errorMessage }}}
        </div>
    @endif

    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <h1>Profile for {{ Session::get("username") }}</h1>
        </div>
    </div>

    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <form action="{{ URL::to("account/user-profile") }}" method="post" role="form">

                <div class="form-group">
                    <label class="control-label">Email</label>
                    <p class="form-control-static">{{{ $userProfile->emails[0] }}}
                        <a href="{{ URL::to("account/user-profile-update-email") }}" role="button" class="btn btn-primary btn-sm">Update Email</a>
                    </p>
                </div>
                <div class="form-group required">
                    <label class="control-label">First Name</label>
                    <div><input class="form-control" id="firstName" maxlength="50" name="firstName"
                                placeholder="Name" type="text"
                                value="{{{ $userProfile->firstName }}}"/></div>
                </div>
                <div class="form-group required">
                    <label class="control-label">Last Name</label>
                    <div><input class="form-control" id="lastName" maxlength="50" name="lastName"
                                placeholder="Name" type="text"
                                value="{{{ $userProfile->lastName }}}"/></div>
                </div>
                <div class="form-group">
                    <label class="control-label">Organization</label>
                    <div><input class="form-control" id="homeOrganization" name="homeOrganization"
                                placeholder="Organization" type="text"
                                value="{{{ $userProfile->homeOrganization }}}"/>
                    </div>
                </div>

                <input name="update" type="submit" class="btn btn-primary btn-block" value="Update">
            </form>
        </div>
    </div>

</div>

@stop
