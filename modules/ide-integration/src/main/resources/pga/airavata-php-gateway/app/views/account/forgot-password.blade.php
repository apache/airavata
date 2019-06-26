@extends('layout.basic')

@section('page-header')
    @parent
@stop

@section('content')

    <div class="col-md-offset-3 col-md-6">

        @if( Session::has("forgot-password-success") )
        <div class="alert alert-success">
            {{{ Session::get("forgot-password-success") }}}
        </div>
        @endif
        @if( Session::has("forgot-password-error") )
        <div class="alert alert-danger">
            {{{ Session::get("forgot-password-error") }}}
        </div>
        @endif
        @if( Session::has("password-reset-error") )
        <div class="alert alert-danger">
            {{{ Session::get("password-reset-error") }}}
        </div>
        @endif
        <h3> Did you forget the password to your account? </h3>
        <h4> Please enter your username, you registered with.</h4>
        <form role="form" method="POST" action="{{ URL::to('/') }}/forgot-password">
            <div class="form-group form-horizontal">
                <div class="col-md-8"><input name="username" type="username" value="" class="form-control" placeholder="username" required/></div>
                <div class="col-md-2"><input type="submit" class="form-control btn btn-primary" value="Submit"/></div>
            </div>
        </form>
    </div>

@stop