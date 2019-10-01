<?php
namespace Keycloak;

use Exception;
use Log;

class KeycloakUtil {

    public static function getAPIAccessToken($base_endpoint_url, $realm, $admin_username, $admin_password, $verify_peer, $cafile_path) {

        // http://www.keycloak.org/docs/2.5/server_development/topics/admin-rest-api.html
        // curl -d client_id=admin-cli -d username=username \
        //   -d "password=password" -d grant_type=password https://149.165.156.62:8443/auth/realms/master/protocol/openid-connect/token

        $r = curl_init($base_endpoint_url . '/realms/' . rawurlencode($realm) . '/protocol/openid-connect/token');
        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($r, CURLOPT_ENCODING, 1);
        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $verify_peer);
        curl_setopt($r, CURLOPT_SSL_VERIFYHOST, $verify_peer);
        if($verify_peer){
            curl_setopt($r, CURLOPT_CAINFO, $cafile_path);
        }

        // Assemble POST parameters for the request.
        $post_fields = "client_id=admin-cli&username=" . urlencode($admin_username) . "&password=" . urlencode($admin_password) . "&grant_type=password";

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            Log::error("Failed to retrieve API Access Token");
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        $result = json_decode($response);
        // Log::debug("API Access Token result", array($result));
        return $result->access_token;
    }
}
