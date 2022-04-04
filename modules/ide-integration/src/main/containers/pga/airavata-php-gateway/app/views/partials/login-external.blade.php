
<a href="{{ $auth_code_option["auth_url"] }}" class="btn btn-primary btn-block btn-external-login">
    @if (isset($auth_code_option["logo"]))
    <img src="{{ $auth_code_option["logo"] }}">
    @endif
    Sign in with {{{ $auth_code_option["name"] }}}
</a>
