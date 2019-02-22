@extends('layout.desktop-login')

@section('page-header')
@parent
@stop

@section('content')

@if(count($auth_code_options) > 0)
    @if($has_auth_code_and_password_options)
    <div class="col-md-offset-1 col-md-4 center-column">
    @else
    <div class="col-md-offset-4 col-md-4 center-column">
    @endif

    @foreach ($auth_code_options as $auth_code_option)
        @include('partials/login-external', array("auth_code_option" => $auth_code_option))
    @endforeach
    </div>
@endif

@if($has_auth_code_and_password_options)
    <div class="col-md-2 center-column">
        <h3 id="login-option-separator" class="horizontal-rule">OR</h3>
    </div>
@endif

@if(!empty($auth_password_option))
    @if($has_auth_code_and_password_options)
    <div class="col-md-4 center-column">
    @else
    <div class="col-md-offset-4 col-md-4 center-column">
    @endif

    @include('partials/login-form', array("auth_name" => $auth_password_option["name"], "desktop" => true))
    </div>
@endif

@stop