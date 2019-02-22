
<h3>
    Login with {{{ $auth_name }}}
    @if (!isset($desktop))
        <small>
            <small> (Not registered? <a href="create">Create account</a>)</small>
        </small>
    @endif
</h3>


<form action="login" method="post" role="form">
    @if( Session::has("invalid-credentials") )
    {{ CommonUtilities::print_error_message('Invalid username or password. Please try again.') }}
    @endif
    @if( Session::has("update-password-required") )
    <div class="alert alert-danger">
        Your password has expired. Please <a href="{{URL::to('/') }}/forgot-password">reset your password</a>.
    </div>
    @endif
    @if( Session::has("password-reset-success") )
    <div class="alert alert-success">
        {{{ Session::get("password-reset-success") }}}
    </div>
    @endif
    @if( Session::has("account-created-success") )
    <div class="alert alert-success">
        {{{ Session::get("account-created-success") }}}
    </div>
    @endif

    <div class="form-group">
        <label class="sr-only" for="username">Username</label>
        <input type="text" class="form-control" name="username" placeholder="Username" autofocus required>
    </div>
    <div class="form-group">
        <label class="sr-only" for="password">Password</label>
        <input type="password" class="form-control" name="password" placeholder="Password" required>
    </div>
    <input name="Submit" type="submit" class="btn btn-primary btn-block" value="Sign in with {{{ $auth_name }}}">
</form>

<small>
    @if (!isset($desktop))
        <small> (Forgot Password? Click <a href="{{URL::to('/') }}/forgot-password">here</a>)</small>
    @endif
</small>
