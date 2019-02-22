<?php
namespace Keycloak\API;

/**
 * Roles class
 *
 * This class provide an easy to use interface for
 * the Keycloak Roles REST API.
 */
class Roles extends BaseKeycloakAPIEndpoint {

    /**
     * Get representations of all of a realm's roles
     * GET /admin/realms/{realm}/roles
     * Returns Array of RoleRepresentation
     */
    public function getRoles($realm){

        // get access token for admin API
        $access_token = $this->getAPIAccessToken($realm);
        $r = curl_init($this->base_endpoint_url . '/admin/realms/' . rawurlencode($realm) . '/roles');
        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($r, CURLOPT_ENCODING, 1);
        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $this->verify_peer);
        curl_setopt($r, CURLOPT_SSL_VERIFYHOST, $this->verify_peer);
        if($this->verify_peer){
            curl_setopt($r, CURLOPT_CAINFO, $this->cafile_path);
        }
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Bearer " . $access_token
        ));

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }
        $result = json_decode($response);
        // Log::debug("getRealmRoleMappingsForUser result", array($result));
        return $result;
    }
}
