@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/user-settings.css')}}
@stop

@section('content')
<div class="container">
    <ol class="breadcrumb">
        <li class="active">User Settings</li>
    </ol>
    <div class="row well user-settings">

        <h3>Profile</h3>

        <a href="{{URL::to('/')}}/account/user-profile">
            <div class=" col-md-12 well">
                <div class="col-md-12">
                    <span class="glyphicon glyphicon-user user-setting-icon"></span>
                </div>
                <div class="col-md-12">
                    <h4>Your Profile</h4>
                </div>
            </div>
        </a>
    </div>

    @if( !Session::has("gateway-provider"))
    <div class="row well user-settings">

        <h3>Manage Personal Computing and Storage Resources</h3>
        <p>Use these settings if you have your own compute and/or
        storage resource accounts that you would like to use.</p>

        <a href="{{URL::to('/')}}/account/user-compute-resources">
            <div class=" col-md-4 well">
                <div class="col-md-12">
                    <span class="glyphicon glyphicon-briefcase user-setting-icon"></span>
                </div>
                <div class="col-md-12">
                    <h4>Compute Resources</h4>
                </div>
            </div>
        </a>

        <a href="{{URL::to('/')}}/account/user-storage-resources">
            <div class=" col-md-4 well">
                <div class="col-md-12">
                    <span class="glyphicon glyphicon-folder-open user-setting-icon"></span>
                </div>
                <div class="col-md-12">
                    <h4>Storage Resources</h4>
                </div>
            </div>
        </a>

        <a href="{{URL::to('/')}}/account/credential-store">
            <div class=" col-md-4 well">
                <div class="col-md-12">
                    <span class="glyphicon glyphicon-lock user-setting-icon"></span>
                </div>
                <div class="col-md-12">
                    <h4>Credential Store</h4>
                </div>
            </div>
        </a>
    </div>
    @endif
</div>

@stop

@section('scripts')
@parent
@stop