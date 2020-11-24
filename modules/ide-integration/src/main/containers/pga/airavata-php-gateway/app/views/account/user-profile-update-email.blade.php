
@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/user-settings.css')}}
@stop

@section('content')
<div class="container">
    <ol class="breadcrumb">
        <li><a href="{{ URL::to('account/settings') }}">User Settings</a></li>
        <li><a href="{{ URL::to('account/user-profile') }}">Your Profile</a></li>
        <li class="active">Update Email</li>
    </ol>

    @if( Session::has("message") )
        <div class="alert alert-success alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert"><span
                    aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
            {{{ Session::get("message") }}}
        </div>
    @endif

    @if( Session::has("errorMessage") )
        <div class="alert alert-danger" role="alert">
            {{{ Session::get("errorMessage") }}}
        </div>
    @endif

    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <h1>Email address update for {{ Session::get("username") }}</h1>
        </div>
    </div>

    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <p>
                Once you submit the following updated email address we'll send
                you an email to confirm the email address.
            </p>

            <form action="{{ URL::to("account/user-profile-update-email") }}" method="post" role="form">

                <div class="form-group required">
                    <label class="control-label">Email</label>
                    <div><input class="form-control" id="newEmail" maxlength="50" name="newEmail"
                                placeholder="Email address" type="text"
                                value="{{{ $email }}}"/></div>
                </div>

                <input name="update" type="submit" class="btn btn-primary btn-block" value="Submit">
            </form>
        </div>
    </div>

</div>

@stop
