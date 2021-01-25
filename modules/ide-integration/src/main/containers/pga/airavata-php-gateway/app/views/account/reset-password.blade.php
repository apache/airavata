@extends('layout.basic')

@section('page-header')
@parent
@stop

@section('content')

<div class="col-md-offset-3 col-md-6">

    <h3>Reset Password</h3>
    @if( Session::has("password-reset-error") )
    <div class="alert alert-danger">
        {{{ Session::get("password-reset-error") }}}
    </div>
    @endif
    @if ($errors->has())
    @foreach ($errors->all() as $error)
    {{ CommonUtilities::print_error_message($error) }}
    @endforeach
    @endif
    <form role="form" method="POST" action="{{ URL::to('/') }}/reset-password">
        <div class="form-group form-horizontal">
            <input name="username" type="hidden" value="{{$username}}" class="form-control"/>
            <input name="code" type="hidden" value="{{{$code}}}" class="form-control"/>
            <div class="form-group required"><label class="control-label">New Password</label>

                <div><input class="form-control" id="new_password" minlength="6" name="new_password" placeholder="New Password"
                            required="required" title="" type="password"
                            data-container="body" data-toggle="popover" data-placement="left"
                            data-content="{{{ $password_regex_tooltip }}}"/>
                </div>
            </div>
            <div class="form-group required"><label class="control-label">New Password (again)</label>
                <div><input class="form-control" id="confirm_new_password" name="confirm_new_password"
                            placeholder="New Password (again)" required="required" title="" type="password"/>
                </div>
            </div>
            <div class="form-group btn-toolbar">
                <div class="btn-group">
                    <input type="submit" class="form-control btn btn-primary" value="Submit"/>
                </div>
            </div>

        </div>
    </form>
</div>
@stop

@section('scripts')
@parent
<script>
    $("#new_password").popover({
        'trigger':'focus'
    });
</script>
@stop
