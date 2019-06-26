<?php

namespace Wsis\Stubs;

class OAuthManager
{

    public $CurlHeaders;
    public $ResponseCode;

    private $_AuthorizeUrl;
    private $_AccessTokenUrl;
    private $_UserInfoUrl;
    private $_verifyPeer;
    private $_cafilePath;

    public function __construct($serverUrl, $verifyPeer, $cafilePath)
    {
        $this->_AuthorizeUrl  = $serverUrl . "oauth2/authorize";
        $this->_LogoutUrl  = $serverUrl . "commonauth?commonAuthLogout=true&type=oidc2&sessionDataKey=7fa50562-2d0f-4234-8e39-8a7271b9b273";
        $this->_AccessTokenUrl  = $serverUrl . "oauth2/token";
        $this->_UserInfoUrl = $serverUrl . "oauth2/userinfo?schema=openid";
        $this->_verifyPeer = $verifyPeer;
        $this->_cafilePath = $cafilePath;
        $this->CurlHeaders = array();
        $this->ResponseCode = 0;
    }

    public function requestAccessCode($client_id, $redirect_url)
    {
        return ($this->_AuthorizeUrl . "?client_id=" . $client_id . "&response_type=code&scope=openid&redirect_uri=" . $redirect_url);
    }

    // Convert an authorization code from callback into an access token.
    public function getAccessToken($client_id, $client_secret, $auth_code, $redirect_url)
    {
        // Init cUrl.
        $r = $this->initCurl($this->_AccessTokenUrl);

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($client_id . ":" . $client_secret),
        ));

        // Assemble POST parameters for the request.
        $post_fields = "code=" . urlencode($auth_code) . "&grant_type=authorization_code&redirect_uri=" . $redirect_url;

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }


    public function getAccessTokenFromPasswordGrantType($client_key, $client_secret, $username, $password)
    {
        // Init cUrl.
        $r = $this->initCurl($this->_AccessTokenUrl);

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($client_key. ":" . $client_secret)
        ));

        // Assemble POST parameters for the request.
        $post_fields = "grant_type=password&username=" . $username . "&password=" . $password . "&scope=openid";

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }

    // To get a refreshed access token
    public function getRefreshedAccessToken($client_key, $client_secret, $refresh_token)
    {
        // Init cUrl.
        $r = $this->initCurl($this->_AccessTokenUrl);

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($client_key . ":" . $client_secret),
        ));

        // Assemble POST parameters for the request.
        $post_fields = "refresh_token=" . urlencode($refresh_token) . "&grant_type=refresh_token";

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }

    // Function to get OAuth logout url
    // refer http://xacmlinfo.org/2015/01/08/openid-connect-identity-server/ for OpenID Connect logout information
    public function getOAuthLogoutUrl($redirect_url, $applicationName)
    {
        return ($this->_LogoutUrl . "&commonAuthCallerPath=" . $redirect_url . "&relyingParty=" . $applicationName);
    }

    private function initCurl($url)
    {
        $r = null;

        if (($r = @curl_init($url)) == false) {
            header("HTTP/1.1 500", true, 500);
            die("Cannot initialize cUrl session. Is cUrl enabled for your PHP installation?");
        }

        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);

        // Decode compressed responses.
        curl_setopt($r, CURLOPT_ENCODING, 1);

        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $this->_verifyPeer);
        if($this->_verifyPeer){
            curl_setopt($r, CURLOPT_CAINFO, $this->_cafilePath);
        }

        return ($r);
    }


    public function getUserProfile($access_token)
    {
        $r = $this->initCurl($this->_UserInfoUrl);

        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Bearer " . $access_token
        ));

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }

    // A generic function that executes an API request.
    public function execRequest($url, $access_token, $get_params)
    {
        // Create request string.
        $full_url = http_build_query($url, $get_params);

        $r = $this->initCurl($full_url);

        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($access_token)
        ));

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }
}

?>