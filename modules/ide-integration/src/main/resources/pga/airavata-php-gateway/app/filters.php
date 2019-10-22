<?php

/*
|--------------------------------------------------------------------------
| Application & Route Filters
|--------------------------------------------------------------------------
|
| Below you will find the "before" and "after" events for the application
| which may be used to do any work before or after a request into your
| application. Here you may also register your custom route filters.
|
*/

//        To invalidate the SOAP WSDL caches
//        ini_set('soap.wsdl_cache_enabled',0);
//        ini_set('soap.wsdl_cache_ttl',0);

App::before(function ($request) {
    //Check OAuth token has expired
    if(Session::has('authz-token')){
        $currentTime = time();
        if($currentTime > Session::get('oauth-expiration-time')){
            $response = Keycloak::getRefreshedOAuthToken(Session::get('oauth-refresh-code'));
            if(isset($response->access_token)){
                $accessToken = $response->access_token;
                $refreshToken = $response->refresh_token;
                $expirationTime = time() + $response->expires_in - 300; // 5 minutes safe margin
                $authzToken = Session::get('authz-token');
                $authzToken->accessToken = $accessToken;
                $authzToken->claimsMap['gatewayID'] = Config::get('pga_config.airavata')['gateway-id'];
                $authzToken->claimsMap['userName'] = Session::get('username');
                Session::put('authz-token',$authzToken);
                Session::put('oauth-refresh-code',$refreshToken);
                Session::put('oauth-expiration-time',$expirationTime);
            }else{
                Session::flush();
                return Redirect::to('home');
            }
        }
    }
});

// Check if Airavata is up
App::before(function ($request) {

    // Exclude logout from check so that user can logout
    if ($request->path() == "logout") {
        return;
    }
    if ( Session::has('authorized-user') || Session::has('admin') || Session::has('admin-read-only') ) {
        // Use "airavata-down" flash variable as a way to prevent infinite redirect
        if (!CommonUtilities::isAiravataUp() && !Session::has("airavata-down")) {
            return Redirect::to("home")->with("airavata-down", true);
        }
    }
});


App::after(function ($request, $response) {
    //
    // Test commit.
});

/*
|--------------------------------------------------------------------------
| Authentication Filters
|--------------------------------------------------------------------------
|
| The following filters are used to verify that the user of the current
| session is logged into this application. The "basic" filter easily
| integrates HTTP Basic authentication for quick, simple checking.
|
*/

Route::filter('auth', function () {
    if (Auth::guest()) {
        if (Request::ajax()) {
            return Response::make('Unauthorized', 401);
        } else {
            return Redirect::guest('login');
        }
    }
});


Route::filter('auth.basic', function () {
    return Auth::basic();
});

/*
|--------------------------------------------------------------------------
| Guest Filter
|--------------------------------------------------------------------------
|
| The "guest" filter is the counterpart of the authentication filters as
| it simply checks that the current user is not logged in. A redirect
| response will be issued if they are, which you may freely change.
|
*/

Route::filter('guest', function () {
    if (Auth::check()) return Redirect::to('/');
});

/*
|--------------------------------------------------------------------------
| CSRF Protection Filter
|--------------------------------------------------------------------------
|
| The CSRF filter is responsible for protecting your application against
| cross-site request forgery attacks. If this special token in a user
| session does not match the one given in this request, we'll bail.
|
*/

Route::filter('csrf', function () {
    if (Session::token() != Input::get('_token')) {
        throw new Illuminate\Session\TokenMismatchException;
    }
});


Route::filter('verifylogin', function () {
    if (!CommonUtilities::verify_login())
        return Redirect::to("home")->with("login-alert", true);
});

Route::filter('verifyauthorizeduser', function () {
    if (CommonUtilities::verify_login()) {
        if (!(Session::has("admin") || Session::has("admin-read-only") || Session::has("authorized-user"))) {
            return Redirect::to("home");
        }
    } else
        return Redirect::to("home")->with("login-alert", true);
});


Route::filter('verifyadmin', function () {
    if (CommonUtilities::verify_login()) {
        if (!Session::has("admin") && !Session::has("admin-read-only") && !Session::has("gateway-provider")) {
            return Redirect::to("home")->with("admin-alert", true);
        }
    } else
        return Redirect::to("home")->with("login-alert", true);
});

Route::filter('verifyeditadmin', function () {
    if (CommonUtilities::verify_login()) {
        if (!Session::has("admin")) {
            return Redirect::to("home")->with("admin-alert", true);
        }
    } else
        return Redirect::to("home")->with("login-alert", true);
});
