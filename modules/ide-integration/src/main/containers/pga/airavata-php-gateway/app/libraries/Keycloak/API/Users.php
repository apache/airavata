<?php
namespace Keycloak\API;

use Log;

/**
 * Users class
 *
 * This class provide an easy to use interface for
 * the Keycloak Users REST API.
 */
class Users extends BaseKeycloakAPIEndpoint {

    /**
     * Get representations of all users
     * GET /admin/realms/{realm}/users
     * Returns Array of UserRepresentation
     */
    public function getUsers($realm, $username = null){

        // get access token for admin API
        $access_token = $this->getAPIAccessToken($realm);
        $url = $this->base_endpoint_url . '/admin/realms/' . rawurlencode($realm) . '/users';
        if ($username) {
            $url = $url . '?username=' . rawurlencode($username);
        }
        // Log::debug("getUsers url", array($url));
        $r = curl_init($url);
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
        // Log::debug("getUsers result", array($result));
        return $result;
    }

    public function getUserByUsername($realm, $username){

        # getUsers returns all users that have a username containing $username
        # so we need to check the returned users for one that matches exactly
        $users = $this->getUsers($realm, $username);
        foreach ($users as $user) {
            if ($user->username == $username) {
                return $user;
            }
        }
        return null;
    }

    /**
     * Search users
     * GET /admin/realms/{realm}/users
     * NOTE: the search is a substring search across users' usernames, first and
     * last names, and email address
     * Returns Array of UserRepresentation
     */
    public function searchUsers($realm, $keyword){

        // get access token for admin API
        $access_token = $this->getAPIAccessToken($realm);
        $url = $this->base_endpoint_url . '/admin/realms/' . rawurlencode($realm) . '/users?search=' . rawurlencode($keyword);
        // Log::debug("getUsers url", array($url));
        $r = curl_init($url);
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
        // Log::debug("getUsers result", array($result));
        return $result;
    }

    /**
     * Get representation of a user
     * GET /admin/realms/{realm}/users/{id}
     * Returns a UserRepresentation
     */
    public function getUser($realm, $user_id) {

        // get access token for admin API
        $access_token = $this->getAPIAccessToken($realm);
        $url = $this->base_endpoint_url . '/admin/realms/' . rawurlencode($realm) . '/users/' . rawurlencode($user_id);
        // Log::debug("getUser url", array($url));
        $r = curl_init($url);
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
        // Log::debug("getUsers result", array($result));
        return $result;
    }
}
