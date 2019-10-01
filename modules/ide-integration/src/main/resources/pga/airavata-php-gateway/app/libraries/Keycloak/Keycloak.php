<?php

namespace Keycloak;

use Keycloak\API\RoleMapper;
use Keycloak\API\Roles;
use Keycloak\API\Users;
use Keycloak\KeycloakUtil;

use CommonUtilities;

use Exception;
use Log;
use Illuminate\Routing\UrlGenerator;
use Illuminate\Support\Facades\Config;

class Keycloak {

    private $realm;
    private $openid_connect_discovery_url;
    private $client_id;
    private $client_secret;
    private $callback_url;
    private $cafile_path;
    private $verify_peer;
    private $base_endpoint_url;
    private $admin_username;
    private $admin_password;
    private $gateway_id;

    // API clients
    private $role_mapper;
    private $roles;
    private $users;

    /**
     * Constructor
     *
     */
    public function __construct($realm, $openid_connect_discovery_url, $client_id, $client_secret, $callback_url, $cafile_path, $verify_peer, $base_endpoint_url, $admin_username, $admin_password, $gateway_id) {

        $this->realm = $realm;
        $this->openid_connect_discovery_url = $openid_connect_discovery_url;
        $this->client_id = $client_id;
        $this->client_secret = $client_secret;
        $this->callback_url = $callback_url;
        $this->cafile_path = $cafile_path;
        $this->verify_peer = $verify_peer;
        $this->base_endpoint_url = $base_endpoint_url;
        $this->admin_username = $admin_username;
        $this->admin_password = $admin_password;
        $this->gateway_id = $gateway_id;

        $this->role_mapper = new RoleMapper($base_endpoint_url, $admin_username, $admin_password, $verify_peer, $this->cafile_path);
        $this->roles = new Roles($base_endpoint_url, $admin_username, $admin_password, $verify_peer, $this->cafile_path);
        $this->users = new Users($base_endpoint_url, $admin_username, $admin_password, $verify_peer, $this->cafile_path);
    }

    /**
     * Function to authenticate user
     *
     * @param string $username
     * @param string $password
     * @return boolean
     * @throws Exception
     */
     public function authenticate($username, $password){

        $config = $this->getOpenIDConnectDiscoveryConfiguration();
        $token_endpoint = $config->token_endpoint;

        // Init cUrl.
        $r = curl_init($token_endpoint);
        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);
        // Decode compressed responses.
        curl_setopt($r, CURLOPT_ENCODING, 1);
        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $this->verify_peer);
        curl_setopt($r, CURLOPT_SSL_VERIFYHOST, $this->verify_peer);

         if($this->verify_peer){
            curl_setopt($r, CURLOPT_CAINFO, $this->cafile_path);
        }

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($this->client_id . ":" . $this->client_secret),
        ));

        // Assemble POST parameters for the request.
        $post_fields = "client_id=" . urlencode($this->client_id) . "&client_secret=" . urlencode($this->client_secret) . "&grant_type=password";
        $post_fields .= "&username=" . urlencode($username) . "&password=" . urlencode($password);

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error5: " . curl_error($r));
        }

        //Parse JSON return object.
        $result = json_decode($response);
        // Log::debug("password grant type authenciation response", array($result));

        return $result;
     }

    public function getOAuthRequestCodeUrl($extra_params=null){
        $config = $this->getOpenIDConnectDiscoveryConfiguration();
        $authorization_endpoint = $config->authorization_endpoint;

        // TODO: add state variable to request and put into session
        $url = $authorization_endpoint . '?response_type=code&client_id=' . urlencode($this->client_id)
            . '&redirect_uri=' . urlencode($this->callback_url)
            . '&scope=openid';
        if ($extra_params != null) {
            $url = $url . '&' . $extra_params;
        }
        return $url;
    }

    public function getOAuthToken($code){

        $config = $this->getOpenIDConnectDiscoveryConfiguration();
        $token_endpoint = $config->token_endpoint;

        // Init cUrl.
        $r = curl_init($token_endpoint);
        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);
        // Decode compressed responses.
        curl_setopt($r, CURLOPT_ENCODING, 1);
        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $this->verify_peer);
        curl_setopt($r, CURLOPT_SSL_VERIFYHOST, $this->verify_peer);

        if($this->verify_peer){
            curl_setopt($r, CURLOPT_CAINFO, $this->cafile_path);
        }

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($this->client_id . ":" . $this->client_secret),
        ));

        // Assemble POST parameters for the request.
        $post_fields = "code=" . urlencode($code) . "&grant_type=authorization_code&redirect_uri=" . urlencode($this->callback_url);

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error4: " . curl_error($r));
        }

        //Parse JSON return object.
        $result = json_decode($response);
        Log::debug("getOAuthToken response", array($result));

        return $result;
    }

    public function getUserProfileFromOAuthToken($token){

        $config = $this->getOpenIDConnectDiscoveryConfiguration();
        $userinfo_endpoint = $config->userinfo_endpoint;

        $r = curl_init($userinfo_endpoint);
        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);
        // Decode compressed responses.
        curl_setopt($r, CURLOPT_ENCODING, 1);
        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $this->verify_peer);
        curl_setopt($r, CURLOPT_SSL_VERIFYHOST, $this->verify_peer);

        if($this->verify_peer){
            curl_setopt($r, CURLOPT_CAINFO, $this->cafile_path);
        }
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Bearer " . $token
        ));

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error3: " . curl_error($r));
        }

        //Parse JSON return object.
        $userinfo = json_decode($response);
        Log::debug("Keycloak userinfo", array($userinfo));
        $username = $userinfo->preferred_username;
        $firstname = $userinfo->given_name;
        $lastname = $userinfo->family_name;
        $email = $userinfo->email;

        // get roles from Keycloak API
        $role_mappings = $this->role_mapper->getRealmRoleMappingsForUser($this->realm, $userinfo->sub);
        $roles = [];
        foreach ($role_mappings as $role_mapping) {
            $roles[] = $role_mapping->name;
        }
        $roles = CommonUtilities::filterAiravataRoles($roles);
        return array('username'=>$username, 'firstname'=>$firstname, 'lastname'=>$lastname, 'email'=>$email, 'roles'=>$roles);
    }

    /**
     * Method to get refreshed access token
     * @param $refreshToken
     * @return mixed
     */
    public function getRefreshedOAuthToken($refresh_token){

        $config = $this->getOpenIDConnectDiscoveryConfiguration();
        $token_endpoint = $config->token_endpoint;

        // Init cUrl.
        $r = curl_init($token_endpoint);
        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);
        // Decode compressed responses.
        curl_setopt($r, CURLOPT_ENCODING, 1);
        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $this->verify_peer);
        curl_setopt($r, CURLOPT_SSL_VERIFYHOST, $this->verify_peer);

        if($this->verify_peer){
            curl_setopt($r, CURLOPT_CAINFO, $this->cafile_path);
        }

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($this->client_id . ":" . $this->client_secret),
        ));

        // Assemble POST parameters for the request.
        $post_fields = "refresh_token=" . urlencode($refresh_token) . "&grant_type=refresh_token";

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error2: " . curl_error($r));
        }

        //Parse JSON return object.
        $result = json_decode($response);
        // Log::debug("getRefreshedOAuthToken response", array($result));

        return $result;
    }

    /**
     * Function to get the OAuth logout url
     */
    public function getOAuthLogoutUrl($redirect_uri) {
        $config = $this->getOpenIDConnectDiscoveryConfiguration();
        $logout_endpoint = $config->end_session_endpoint;
        return $logout_endpoint . '?redirect_uri=' . rawurlencode($redirect_uri);
    }

    /**
     * Function to list users
     *
     * @return Array of usernames
     */
    public function listUsers(){
        $users = $this->users->getUsers($this->realm);
        $usernames = [];
        foreach ($users as $user) {
            Log::debug("user", array($user));
            array_push($usernames, (object)["firstName"=>$user->firstName,"lastName"=>$user->lastName,"email"=>$user->email,"userEnabled"=>$user->enabled,"userName"=>$user->username]);
        }
        return $usernames;
    }

    /**
     * Function to search users
     * NOTE: Keycloak uses the keyword to search in the username, first and last
     * name and email address
     * @param $keyword
     * @return Array of usernames
     */
    public function searchUsers($phrase){
        $users = $this->users->searchUsers($this->realm, $phrase);
        $usernames = [];
        foreach ($users as $user) {
            array_push($usernames, (object)["firstName"=>$user->firstName,"lastName"=>$user->lastName,"email"=>$user->email,"userEnabled"=>$user->enabled,"userName"=>$user->username]);
        }
        return $usernames;
    }

    /**
     * Function to get the list of all existing roles
     * For Keycloak this is a list of "Realm roles"
     *
     * @return roles list
     */
    public function getAllRoles(){
        try {
            $roles = $this->roles->getRoles($this->realm);
            $role_names = [];
            foreach ($roles as $role) {
                $role_names[] = $role->name;
            }
            return CommonUtilities::filterAiravataRoles($role_names);
        } catch (Exception $ex) {
            throw new Exception("Unable to get all roles", 0, $ex);
        }
    }

    /**
     * Function to get roles of a user
     * For Keycloak this is a list of "Realm roles"
     *
     * @return array of role names
     */
    public function getUserRoles( $username ){
        try {
            // get userid from username
            $user_id = $this->getUserId($username);
            // Get the user's realm roles, then convert to an array of just names
            $roles = $this->role_mapper->getRealmRoleMappingsForUser($this->realm, $user_id);
            $role_names = [];
            foreach ($roles as $role) {
                $role_names[] = $role->name;
            }
            return CommonUtilities::filterAiravataRoles($role_names);
        } catch (Exception $ex) {
            throw new Exception("Unable to get User roles.", 0, $ex);
        }
    }

    /**
     * Function to update role list of user
     *
     * @param $username
     * @param $roles, an Array with two entries, "deleted" and "new", each of
     * which has a value of roles to be removed or added respectively
     * @return void
     */
    public function updateUserRoles( $username, $roles){
        // Log::debug("updateUserRoles", array($user_id, $roles));
        try {
            // get userid from username
            $user_id = $this->getUserId($username);
            // Get all of the roles into an array keyed by role name
            $all_roles = $this->roles->getRoles($this->realm);
            $roles_by_name = [];
            foreach ($all_roles as $role) {
                $roles_by_name[$role->name] = $role;
            }

            // Process the role deletions
            if(isset($roles["deleted"])){
                if(!is_array($roles["deleted"]))
                    $roles["deleted"] = array($roles["deleted"]);
                foreach ($roles["deleted"] as $role) {
                    $this->role_mapper->deleteRealmRoleMappingsToUser($this->realm, $user_id, array($roles_by_name[$role]));
                }
            }

            // Process the role additions
            if(isset($roles["new"])){
                if(!is_array($roles["new"]))
                    $roles["new"] = array($roles["new"]);
                foreach ($roles["new"] as $role) {
                    $this->role_mapper->addRealmRoleMappingsToUser($this->realm, $user_id, array($roles_by_name[$role]));
                }
            }
        } catch (Exception $ex) {
            throw new Exception("Unable to update role of the user.", 0, $ex);
        }
    }

    /**
     * Function to get the user profile of a user
     * @param $username
     */
    public function getUserProfile($username){
        $user = $this->users->getUserByUsername($this->realm, $username);
        if($user != null){
            $result = [];
            $result["email"] = $user->email;
            $result["firstname"] = $user->firstName;
            $result["lastname"] = $user->lastName;
            $result["userEnabled"] = $user->enabled;
            return $result;
        }else{
            return [];
        }

    }

    /**
     * Function to check whether a user exists with the given userId
     * @param $username
     * @return bool
     */
    public function usernameExists($username){
        try{
            $user = $this->users->getUserByUsername($this->realm, $username);
            return $user != null;
        }catch (Exception $ex){
            // Username does not exists
            return false;
        }
    }

    // TODO: move this to IamAdminServices
    public function isUpdatePasswordRequired($username) {

        try{
            $user = $this->users->getUserByUsername($this->realm, $username);
            if ($user != null) {
                return in_array("UPDATE_PASSWORD", $user->requiredActions);
            } else {
                return false;
            }
        }catch (Exception $ex){
            // Username does not exists
            return false;
        }
    }

    public function getAdminAuthzToken() {

        $access_token = KeycloakUtil::getAPIAccessToken($this->base_endpoint_url, $this->realm, $this->admin_username, $this->admin_password, $this->verify_peer, $this->cafile_path);
        $authzToken = new \Airavata\Model\Security\AuthzToken();
        $authzToken->accessToken = $access_token;
        $authzToken->claimsMap['gatewayID'] = $this->gateway_id;
        $authzToken->claimsMap['userName'] = $this->admin_username;
        return $authzToken;
    }

    /**
     * Get the user's Keycloak user_id from their username
     */
    private function getUserId($username) {
        $user = $this->users->getUserByUsername($this->realm, $username);
        if ($user != null) {
            return $user->id;
        } else {
            throw new Exception("No user found with username $username");
        }
    }

    private function getOpenIDConnectDiscoveryConfiguration() {

        // TODO: cache the result of the request
        $r = curl_init($this->openid_connect_discovery_url);
        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);
        // Decode compressed responses.
        curl_setopt($r, CURLOPT_ENCODING, 1);
        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $this->verify_peer);

        curl_setopt($r, CURLOPT_SSL_VERIFYHOST, $this->verify_peer);
        if($this->verify_peer){
            curl_setopt($r, CURLOPT_CAINFO, $this->cafile_path);
        }

        $result = curl_exec($r);
        if ($result == false) {
            die("curl_exec() failed. Error1: " . curl_error($r));
        }

        $json = json_decode($result);

        // Log::debug("openid connect discovery configuration", array($json));
        return $json;
    }
}

