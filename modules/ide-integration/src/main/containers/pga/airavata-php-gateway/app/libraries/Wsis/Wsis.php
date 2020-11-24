<?php

namespace Wsis;

use Wsis\Stubs\UserProfileManager;
use Wsis\Stubs\UserStoreManager;
use Wsis\Stubs\TenantManager;
use Wsis\Stubs\UserInformationRecoveryManager;
use Wsis\Stubs\OAuthManager;

use Illuminate\Support\Facades\Config;

class Wsis {

    /**
     * @var UserStoreManager
     * @access private
     */
    private $userStoreManager;

    /**
     * @var
     * @access private
     */
    private $tenantManager;

    /**
     * @var
     * @access private
     */
    private $userProfileManager;

    /**
     * @var
     * @access private
     */
    private $userInfoRecoveryManager;

    /**
     * @var
     * @access private
     */
    private $oauthManger;

    /**
     * @var string
     * @access private
     */
    private $server;

    /**
     * @var string
     * @access private
     */
    private $service_url;


    /**
     * Constructor
     *
     * @param string $admin_username
     * @param string $admin_password
     * @param string $server
     * @param string $service_url
     * @param string $cafile_path
     * @param bool   $verify_peer
     * @param bool   $allow_selfsigned_cer
     * @throws Exception
     */
    public function __construct($admin_username, $admin_password = null, $server,
                                $service_url,$cafile_path, $verify_peer, $allow_selfsigned_cert) {

        $context = stream_context_create(array(
            'ssl' => array(
                'verify_peer' => $verify_peer,
                "allow_self_signed"=> $allow_selfsigned_cert,
                'cafile' => $cafile_path,
//                'CN_match' => $server,
            )
        ));

        $parameters = array(
            'login' => $admin_username,
            'password' => $admin_password,
            'stream_context' => $context,
            'trace' => 1,
            'features' => SOAP_WAIT_ONE_WAY_CALLS,
            'cache_wsdl' => WSDL_CACHE_BOTH
        );

        $this->server = $server;
        $this->service_url = $service_url;

        try {
            $this->userStoreManager = new UserStoreManager($service_url, $parameters);
            $this->tenantManager = new TenantManager($service_url, $parameters);
            $this->userProfileManager = new UserProfileManager($service_url, $parameters);
            $this->userInfoRecoveryManager = new UserInformationRecoveryManager($service_url, $parameters);
            $this->oauthManger = new OAuthManager(Config::get('pga_config.wsis')['service-url'], $verify_peer, $cafile_path);
        } catch (Exception $ex) {
            throw new Exception("Unable to instantiate WSO2 IS client", 0, $ex);
        }
    }


    /**
     * Function to add new user
     *
     * @param string $userName
     * @param string $password
     * @return void
     * @throws Exception
     */
    public function addUser($userName, $password) {
        try {
            $this->userStoreManager->addUser($userName, $password);
        } catch (Exception $ex) {
            throw new Exception("Unable to add new user", 0, $ex);
        }
    }


    /**
     * Function to create a new user account. This user account is not active unless activates by the user via
     * his/her email
     *
     * @param $userName
     * @param $password
     * @param $email
     * @param $firstName
     * @param $lastName
     * @param $tenantDomain
     * @throws Exception
     */
    public function registerAccount($userName, $password, $email, $firstName, $lastName, $tenantDomain){
        try {
            $this->userInfoRecoveryManager->registerAccount($userName, $password, $email, $firstName, $lastName, $tenantDomain);
        } catch (Exception $ex) {
            throw new Exception("Unable to create a new user account", 0, $ex);
        }
    }

    /**
     * Function to delete existing user
     *
     * @param string $username
     * @return void
     * @throws Exception
     */
    public function deleteUser($username) {
        try {
            $this->userStoreManager->deleteUser($username);
        } catch (Exception $ex) {
            throw new Exception("Unable to delete user", 0, $ex);
        }
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
        try {
//            return $this->userStoreManager->authenticate($username, $password);
            return $this->oauthManger->getAccessTokenFromPasswordGrantType(Config::get('pga_config.wsis')['oauth-client-key'],
                Config::get('pga_config.wsis')['oauth-client-secret'], $username, $password);
        } catch (Exception $ex) {
            throw new Exception("Unable to authenticate user", 0, $ex);
        }
    }

    /**
     * Function to get OAuth request code url
     * @return mixed
     */
    public function getOAuthRequestCodeUrl(){
        $url = $this->oauthManger->requestAccessCode(Config::get('pga_config.wsis')['oauth-client-key'],
            Config::get('pga_config.wsis')['oauth-callback-url']);
        return $url;
    }

    /**
     * Function to get OAuth Access token
     * @return string
     */
    public function getOAuthToken($code){
        $response = $this->oauthManger->getAccessToken(Config::get('pga_config.wsis')['oauth-client-key'],
            Config::get('pga_config.wsis')['oauth-client-secret'], $code,
            Config::get('pga_config.wsis')['oauth-callback-url']);
        return $response;
    }

    /**
     * Method to get refreshed access token
     * @param $refreshToken
     * @return mixed
     */
    public function getRefreshedOAutheToken($refreshToken){
        $response = $this->oauthManger->getRefreshedAccessToken(Config::get('pga_config.wsis')['oauth-client-key'],
            Config::get('pga_config.wsis')['oauth-client-secret'], $refreshToken);
        return $response;
    }

    /**
     * Function to get user profile from OAuth token
     * @param $token
     */
    public function getUserProfileFromOAuthToken($token){
        $userProfile = $this->oauthManger->getUserProfile($token);

        //FIXME hacky fix for the CILogon -> OpenID issue in WSO2 IS
        $sub = $userProfile->sub;
        if(0 === strpos($sub, 'http:/')){
            $mod_sub = substr ($sub ,6);
        }else{
            $mod_sub = $sub;
        }
        $userProfile = $this->getUserProfile($mod_sub);
        $lastname = $userProfile['lastname'];
        $firstname = $userProfile['firstname'];
        $email = $userProfile['email'];
        $roles = $this->getUserRoles($mod_sub);
        if(!is_array($roles))
            $roles = explode(",", $roles);
        return array('username'=>$sub, 'firstname'=>$firstname, 'lastname'=>$lastname, 'email'=>$email, 'roles'=>$roles);
    }

    /**
     * Function to get the OAuth logout url
     */
    public function getOAuthLogoutUrl(){
        return $this->oauthManger->getOAuthLogoutUrl(Config::get('pga_config.wsis')['oauth-callback-url'],
            Config::get('pga_config.wsis')['oauth-service-provider-id']);
    }


    /**
     * Function to check whether username exists
     *
     * @param string $username
     * @return boolean
     * @throws Exception
     */
    public function usernameExists($username){
        try {
            return $this->userStoreManager->isExistingUser($username);
        } catch (Exception $ex) {
            throw new Exception("Unable to verify username exists", 0, $ex);
        }
    }

    /**
     * Function to check whether a role is existing
     *
     * @param string $roleName
     * @return IsExistingRoleResponse
     */
    public function isExistingRole( $roleName){
        try {
            return $this->userStoreManager->isExistingRole( $roleName);
        } catch (Exception $ex) {
            throw new Exception("Unable to check if the role exists", 0, $ex);
        }
    }

    /**
     * Function to add new role by providing the role name.
     *
     * @param string $roleName
     */
    public function addRole($roleName){
        try {
            return $this->userStoreManager->addRole( $roleName);
        } catch (Exception $ex) {
            throw new Exception("Unable to add this role", 0, $ex);
        }
    }

    /**
     * Function to delete existing role
     *
     * @param string $roleName
     * @return void
     * @throws Exception
     */
    public function deleteRole($roleName) {
        try {
            $this->userStoreManager->deleteRole($roleName);
        } catch (Exception $ex) {
            throw new Exception("Unable to delete role", 0, $ex);
        }
    }

    /**
     * Function to get the list of all existing roles
     *
     * @return roles list
     */
    public function getAllRoles(){
        try {
            $roles = $this->userStoreManager->getRoleNames();
            return $roles;
//            return array_filter($roles, "Wsis::nonInternalRoles");
//            var_dump($roles);exit;
        } catch (Exception $ex) {
            throw new Exception("Unable to get all roles", 0, $ex);
        }
    }

    public function nonInternalRoles($var){
        return 0 !== strpos($var, 'Internal/');
    }

    /**
     * Function to get role of a user
     *
     * @return user role
     */
    public function getUserRoles( $username){
        try {
            $roles = $this->userStoreManager->getRoleListOfUser( $username);
            return $roles;
//            return array_filter($roles, "Wsis::nonInternalRoles");
        } catch (Exception $ex) {
            throw new Exception("Unable to get User roles.", 0, $ex);
        }
    }

    /**
     * Function to get the user list of role
     *
     * @param GetUserListOfRole $parameters
     * @return GetUserListOfRoleResponse
     */
    public function getUserListOfRole( $role){
        try {
            return $this->userStoreManager->getUserListOfRole( $role);
        } catch (Exception $ex) {
            var_dump( $ex); exit;
            throw new Exception("Unable to get user list of roles.", 0, $ex);
        }
    }

    /**
     * Function to update role list of user
     *
     * @param UpdateRoleListOfUser $parameters
     * @return void
     */
    public function updateUserRoles( $username, $roles){
        try {
            return $this->userStoreManager->updateRoleListOfUser( $username, $roles);
        } catch (Exception $ex) {
            throw new Exception("Unable to update role of the user.", 0, $ex);
        }
    }

    /**
     * Function to list users
     *
     * @param void
     * @return void
     */
    public function listUsers(){
        try {
            return $this->userStoreManager->listUsers();
        } catch (Exception $ex) {
            var_dump( $ex->debug_message);
            throw new Exception("Unable to list users.", 0, $ex);
        }
    }

    /**
     * Function to search users
     * @param $phrase
     * @return string
     * @throws Exception
     */
    public function searchUsers($phrase){
        try {
            return $this->userStoreManager->searchUsers($phrase);
        } catch (Exception $ex) {
            var_dump( $ex->debug_message);
            throw new Exception("Unable to list users.", 0, $ex);
        }
    }

    /**
     * Function to get the tenant id
     *
     * @param GetTenantId $parameters
     * @return GetTenantIdResponse
     */
    public function getTenantId(){
        try {
            return $this->userStoreManager->getTenantId();
        } catch (Exception $ex) {
            var_dump( $ex->debug_message);
            throw new Exception("Unable to get the tenant Id.", 0, $ex);
        }
    }

    /**
     * Function create a new Tenant
     * @param $active
     * @param $adminUsername
     * @param $adminPassword
     * @param $email
     * @param $firstName
     * @param $lastName
     * @param $tenantDomain
     * @throws Exception
     */
    public function createTenant($active, $adminUsername, $adminPassword, $email,
                                  $firstName, $lastName, $tenantDomain){
        try {
            $tm = $this->tenantManager->addTenant($active, $adminUsername, $adminPassword, $email,
                $firstName, $lastName, $tenantDomain);
            $wsisConfig = Config::get('pga_config.wsis');
            $context = stream_context_create(array(
                'ssl' => array(
                    'verify_peer' => $wsisConfig['verify-peer'],
                    "allow_self_signed"=> $wsisConfig['allow-self-signed-cert'],
                    'cafile' => $wsisConfig['cafile-path'],
                )
            ));
            $parameters = array(
                'login' => $adminUsername. '@' . $tenantDomain,
                'password' => $adminPassword,
                'stream_context' => $context,
                'trace' => 1,
                'features' => SOAP_WAIT_ONE_WAY_CALLS,
                'cache_wsdl' => WSDL_CACHE_BOTH
            );
            $userProfileManager = new UserProfileManager($wsisConfig['service-url'], $parameters);
            $userProfileManager->updateUserProfile($adminUsername, $email, $firstName, $lastName);
            return $tm;
        } catch (Exception $ex) {
            throw new Exception("Unable to create Tenant.", 0, $ex);
        }
    }

    public function getTenants(){
        try {
            return $this->tenantManager->retrieveTenants();
        } catch (Exception $ex) {
            throw new Exception("Unable to get Tenants.", 0, $ex);
        }
    }

    /**
     * Function to update the user profile
     * @param $username
     * @param $email
     * @param $firstName
     * @param $lastName
     */
    public function updateUserProfile($username, $email, $firstName, $lastName){
        $this->userProfileManager->updateUserProfile($username, $email, $firstName, $lastName);
    }


    /**
     *
     * Function to create a user account. This user account has to be activated by the user via his
     * email account
     * @param $username
     * @param $password
     * @param $email
     * @param $firstName
     * @param $lastName
     * @param $tenantDomain
     */
    public function registerUserAccount($username, $password, $email, $firstName, $lastName, $organization, $address, $country, $telephone, $mobile, $im, $url, $tenantDomain)
    {
        $this->userInfoRecoveryManager->registerAccount($username, $password, $email, $firstName,
            $lastName, $organization, $address, $country, $telephone, $mobile, $im, $url, $tenantDomain);
    }

    /**
     * Function to confirm user registration
     * @param $userName
     * @param $tenantDomain
     */
    public function confirmUserRegistration($userName, $code, $tenantDomain){
        return $this->userInfoRecoveryManager->confirmUserRegistration($userName, $code, $tenantDomain);
    }

    /**
     * Function to get the user profile of a user
     * @param $username
     */
    public function getUserProfile($username){
        //FIXME hacky fix for the CILogon -> OpenID issue in WSO2 IS
        if(0 === strpos($username, 'http:/')){
            $username = substr ($username ,6);
        }
        $username = str_replace("@".Config::get('pga_config.wsis')['tenant-domain'], "", $username);
        return $this->userProfileManager->getUserProfile($username);
    }

    /**
     * Method to validate username
     * @param $username
     */
    public function validateUser($userAnswer, $imagePath, $secretKey, $username){
        return $this->userInfoRecoveryManager->validateUsername($userAnswer, $imagePath, $secretKey, $username);
    }


    /**
     * Method to send password reset notification
     * @param $username
     */
    public function sendPasswordResetNotification($username, $key){
        return $this->userInfoRecoveryManager->sendPasswordResetNotification($username, $key);
    }

    /**
     * Method to validate the password reset email confirmation code
     * @param $username
     * @param $confirmation
     * @return mixed
     */
    public function validateConfirmationCode($username, $confirmation){
        return $this->userInfoRecoveryManager->validateConfirmationCode($username, $confirmation);
    }

    /**
     * Method to reset user password
     * @param $username
     * @param $newPassword
     * @param $key
     * @return mixed
     */
    public function resetPassword($username, $newPassword, $key){
        return $this->userInfoRecoveryManager->resetPassword($username, $newPassword, $key);
    }

    /**
     * Method to get a capatcha
     * @return mixed
     */
    public function getCapatcha(){
        return $this->userInfoRecoveryManager->getCapatcha();
    }
} 