<?php
namespace Wsis\Stubs;
use SoapClient;

/**
 * This file contains the DTOs and the method stubs for
 * WSO2 IS 4.6.0 RemoteUserStoreManger service.
 */

class ClaimDTO {

    /**
     * @var string $claimUri
     * @access public
     */
    public $claimUri;

    /**
     * @var string $description
     * @access public
     */
    public $description;

    /**
     * @var string $dialectURI
     * @access public
     */
    public $dialectURI;

    /**
     * @var int $displayOrder
     * @access public
     */
    public $displayOrder;

    /**
     * @var string $displayTag
     * @access public
     */
    public $displayTag;

    /**
     * @var string $regEx
     * @access public
     */
    public $regEx;

    /**
     * @var boolean $required
     * @access public
     */
    public $required;

    /**
     * @var boolean $supportedByDefault
     * @access public
     */
    public $supportedByDefault;

    /**
     * @var string $value
     * @access public
     */
    public $value;

}

class PermissionDTO {

    /**
     * @var string $action
     * @access public
     */
    public $action;

    /**
     * @var string $resourceId
     * @access public
     */
    public $resourceId;

}

class Tenant {

    /**
     * @var boolean $active
     * @access public
     */
    public $active;

    /**
     * @var string $adminFirstName
     * @access public
     */
    public $adminFirstName;

    /**
     * @var string $adminFullName
     * @access public
     */
    public $adminFullName;

    /**
     * @var string $adminLastName
     * @access public
     */
    public $adminLastName;

    /**
     * @var string $adminName
     * @access public
     */
    public $adminName;

    /**
     * @var string $adminPassword
     * @access public
     */
    public $adminPassword;

    /**
     * @var string $createdDate YYYY-MM-DD
     * @access public
     */
    public $createdDate;

    /**
     * @var string $domain
     * @access public
     */
    public $domain;

    /**
     * @var string $email
     * @access public
     */
    public $email;

    /**
     * @var int $id
     * @access public
     */
    public $id;

    /**
     * @var RealmConfiguration $realmConfig
     * @access public
     */
    public $realmConfig;

}

class RealmConfiguration {

    /**
     * @var string $addAdmin
     * @access public
     */
    public $addAdmin;

    /**
     * @var string $adminPassword
     * @access public
     */
    public $adminPassword;

    /**
     * @var string $adminRoleName
     * @access public
     */
    public $adminRoleName;

    /**
     * @var string $adminUserName
     * @access public
     */
    public $adminUserName;

    /**
     * @var string $authorizationManagerClass
     * @access public
     */
    public $authorizationManagerClass;

    /**
     * @var string $authzProperties
     * @access public
     */
    public $authzProperties;

    /**
     * @var string $description
     * @access public
     */
    public $description;

    /**
     * @var string $everyOneRoleName
     * @access public
     */
    public $everyOneRoleName;

    /**
     * @var string $multipleCredentialProps
     * @access public
     */
    public $multipleCredentialProps;

    /**
     * @var boolean $passwordsExternallyManaged
     * @access public
     */
    public $passwordsExternallyManaged;

    /**
     * @var string $persistedTimestamp YYYY-MM-DD
     * @access public
     */
    public $persistedTimestamp;

    /**
     * @var boolean $primary
     * @access public
     */
    public $primary;

    /**
     * @var string $realmClassName
     * @access public
     */
    public $realmClassName;

    /**
     * @var string $realmProperties
     * @access public
     */
    public $realmProperties;

    /**
     * @var RealmConfiguration $secondaryRealmConfig
     * @access public
     */
    public $secondaryRealmConfig;

    /**
     * @var int $tenantId
     * @access public
     */
    public $tenantId;

    /**
     * @var string $userStoreClass
     * @access public
     */
    public $userStoreClass;

    /**
     * @var string $userStoreProperties
     * @access public
     */
    public $userStoreProperties;

}

class ClaimValue {

    /**
     * @var string $claimURI
     * @access public
     */
    public $claimURI;

    /**
     * @var string $value
     * @access public
     */
    public $value;

}

class AddUserClaimValues {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var ClaimValue $claims
     * @access public
     */
    public $claims;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class GetUserClaimValuesForClaims {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $claims
     * @access public
     */
    public $claims;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class GetUserClaimValuesForClaimsResponse {

    /**
     * @var ClaimValue $return
     * @access public
     */
    public $return;

}

class GetTenantIdofUser {

    /**
     * @var string $userName
     * @access public
     */
    public $username;

}

class GetTenantIdofUserResponse {

    /**
     * @var int $return
     * @access public
     */
    public $return; // int

}

class AddUserClaimValue {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $claimURI
     * @access public
     */
    public $claimURI;

    /**
     * @var string $claimValue
     * @access public
     */
    public $claimValue;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class GetUserClaimValues {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class GetUserClaimValuesResponse {

    /**
     * @var ClaimDTO $return
     * @access public
     */
    public $return;

}

class GetTenantId {
    
}

class GetTenantIdResponse {

    /**
     * @var int $return
     * @access public
     */
    public $return;

}

class AddUser {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $credential
     * @access public
     */
    public $credential;

    /**
     * @var string $roleList
     * @access public
     */
    public $roleList;

    /**
     * @var ClaimValue $claims
     * @access public
     */
    public $claims;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

    /**
     * @var boolean $requiredPasswordChange
     * @access public
     */
    public $requirePasswordChange;

}

class AddRole {

    /**
     * @var string $roleName
     * @access public
     */
    public $roleName;

    /**
     * @var string $userList
     * @access public
     */
    public $userList;

    /**
     * @var PermissionDTO $permissions
     * @access public
     */
    public $permissions;

}

class GetUserList {

    /**
     * @var string $claimUri
     * @access public
     */
    public $claimUri;

    /**
     * @var string $claimValue
     * @access public
     */
    public $claimValue;

    /**
     * @var string $profile
     * @access public
     */
    public $profile;

}

class GetUserListResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class UpdateCredential {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $newCredential
     * @access public
     */
    public $newCredential;

    /**
     * @var string $oldCredential
     * @access public
     */
    public $oldCredential;

}

class UpdateUserListOfRole {

    /**
     * @var string $roleName
     * @access public
     */
    public $roleName;

    /**
     * @var string $deletedUsers
     * @access public
     */
    public $deletedUsers;

    /**
     * @var string $newUsers
     * @access public
     */
    public $newUsers;

}

class UpdateRoleListOfUser {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $deletedRoles
     * @access public
     */
    public $deletedRoles;

    /**
     * @var string $newRoles
     * @access public
     */
    public $newRoles;

}

class SetUserClaimValue {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $claimURI
     * @access public
     */
    public $claimURI;

    /**
     * @var string $claimValue
     * @access public
     */
    public $claimValue;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class SetUserClaimValues {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var ClaimValue $claims
     * @access public
     */
    public $claims;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class DeleteUserClaimValue {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $claimURI
     * @access public
     */
    public $claimURI;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class DeleteUserClaimValues {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $claims
     * @access public
     */
    public $claims;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class GetHybridRoles {
    
}

class GetHybridRolesResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class GetPasswordExpirationTime {

    /**
     * @var string $username
     * @access public
     */
    public $username;

}

class GetPasswordExpirationTimeResponse {

    /**
     * @var long $return
     * @access public
     */
    public $return;

}

class UpdateRoleName {

    /**
     * @var string $roleName
     * @access public
     */
    public $roleName;

    /**
     * @var string $newRoleName
     * @access public
     */
    public $newRoleName;

}

class ListUsers {

    /**
     * @var string $filter
     * @access public
     */
    public $filter;

    /**
     * @var int $maxItemLimit
     * @access public
     */
    public $maxItemLimit;

}

class ListUsersResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class IsExistingUser {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

}

class IsExistingUserResponse {

    /**
     * @var boolean $return
     * @access public
     */
    public $return;

}

class IsExistingRole {

    /**
     * @var string $roleName
     * @access public
     */
    public $roleName;

}

class IsExistingRoleResponse {

    /**
     * @var boolean $return
     * @access public
     */
    public $return;

}

class GetRoleNames {
    
}

class GetRoleNamesResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class GetProfileNames {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

}

class GetProfileNamesResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class GetUserListOfRole {

    /**
     * @var string $roleName
     * @access public
     */
    public $roleName;

}

class GetUserListOfRoleResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class GetUserClaimValue {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $claim
     * @access public
     */
    public $claim;

    /**
     * @var string $profileName
     * @access public
     */
    public $profileName;

}

class GetUserClaimValueResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class GetAllProfileNames {
    
}

class GetAllProfileNamesResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class UpdateCredentialByAdmin {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $newCredential
     * @access public
     */
    public $newCredential;

}

class DeleteUser {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

}

class DeleteRole {

    /**
     * @var string $roleName
     * @access public
     */
    public $roleName;

}

class GetUserId {

    /**
     * @var string $username
     * @access public
     */
    public $username;

}

class GetUserIdResponse {

    /**
     * @var int $return
     * @access public
     */
    public $return;

}

class GetRoleListOfUser {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

}

class getRoleListOfUserResponse {

    /**
     * @var string $return
     * @access public
     */
    public $return;

}

class GetProperties {

    /**
     * @var Tenant $tenant
     * @access public
     */
    public $tenant;

}

class GetPropertiesResponse {

    /**
     * @var Array<string> $return
     * @access public
     */
    public $return;

}

class IsReadOnly {
    
}

class IsReadOnlyResponse {

    /**
     * @var boolean $return
     * @access public
     */
    public $return;

}

class Authenticate {

    /**
     * @var string $userName
     * @access public
     */
    public $userName;

    /**
     * @var string $credential
     * @access public
     */
    public $credential;

}

class AuthenticateResponse {

    /**
     * @var boolean $return
     * @access public
     */
    public $return;

}

class UserStoreException {
    
}

/**
 * UserStoreManagerService class
 * 
 */
class UserStoreManagerStub extends SoapClient {

    private static $classmap = array(
        'ClaimDTO' => 'ClaimDTO',
        'PermissionDTO' => 'PermissionDTO',
        'Tenant' => 'Tenant',
        'RealmConfiguration' => 'RealmConfiguration',
        'ClaimValue' => 'ClaimValue',
        'addUserClaimValues' => 'AddUserClaimValues',
        'getUserClaimValuesForClaims' => 'GetUserClaimValuesForClaims',
        'getUserClaimValuesForClaimsResponse' => 'GetUserClaimValuesForClaimsResponse',
        'getTenantIdofUser' => 'GetTenantIdofUser',
        'getTenantIdofUserResponse' => 'GetTenantIdofUserResponse',
        'addUserClaimValue' => 'AddUserClaimValue',
        'getUserClaimValues' => 'GetUserClaimValues',
        'getUserClaimValuesResponse' => 'GetUserClaimValuesResponse',
        'getTenantId' => 'GetTenantId',
        'getTenantIdResponse' => 'GetTenantIdResponse',
        'addUser' => 'AddUser',
        'addRole' => 'AddRole',
        'getUserList' => 'GetUserList',
        'getUserListResponse' => 'GetUserListResponse',
        'updateCredential' => 'UpdateCredential',
        'updateUserListOfRole' => 'UpdateUserListOfRole',
        'updateRoleListOfUser' => 'UpdateRoleListOfUser',
        'setUserClaimValue' => 'SetUserClaimValue',
        'setUserClaimValues' => 'SetUserClaimValues',
        'deleteUserClaimValue' => 'DeleteUserClaimValue',
        'deleteUserClaimValues' => 'DeleteUserClaimValues',
        'getHybridRoles' => 'GetHybridRoles',
        'getHybridRolesResponse' => 'GetHybridRolesResponse',
        'getPasswordExpirationTime' => 'GetPasswordExpirationTime',
        'getPasswordExpirationTimeResponse' => 'GetPasswordExpirationTimeResponse',
        'updateRoleName' => 'UpdateRoleName',
        'listUsers' => 'ListUsers',
        'listUsersResponse' => 'ListUsersResponse',
        'isExistingUser' => 'IsExistingUser',
        'isExistingUserResponse' => 'IsExistingUserResponse',
        'isExistingRole' => 'IsExistingRole',
        'isExistingRoleResponse' => 'IsExistingRoleResponse',
        'getRoleNames' => 'GetRoleNames',
        'getRoleNamesResponse' => 'GetRoleNamesResponse',
        'getProfileNames' => 'GetProfileNames',
        'getProfileNamesResponse' => 'GetProfileNamesResponse',
        'getUserListOfRole' => 'GetUserListOfRole',
        'getUserListOfRoleResponse' => 'GetUserListOfRoleResponse',
        'getUserClaimValue' => 'GetUserClaimValue',
        'getUserClaimValueResponse' => 'GetUserClaimValueResponse',
        'getAllProfileNames' => 'GetAllProfileNames',
        'getAllProfileNamesResponse' => 'GetAllProfileNamesResponse',
        'updateCredentialByAdmin' => 'UpdateCredentialByAdmin',
        'deleteUser' => 'DeleteUser',
        'deleteRole' => 'DeleteRole',
        'getUserId' => 'GetUserId',
        'getUserIdResponse' => 'GetUserIdResponse',
        'getRoleListOfUser' => 'GetRoleListOfUser',
        'getRoleListOfUserResponse' => 'GetRoleListOfUserResponse',
        'getProperties' => 'GetProperties',
        'getPropertiesResponse' => 'GetPropertiesResponse',
        'isReadOnly' => 'IsReadOnly',
        'isReadOnlyResponse' => 'IsReadOnlyResponse',
        'authenticate' => 'Authenticate',
        'authenticateResponse' => 'AuthenticateResponse',
        'Tenant' => 'Tenant'
    );

    public function RemoteUserStoreManagerStub($wsdl, $options = array()) {
        foreach (self::$classmap as $key => $value) {
            if (!isset($options['classmap'][$key])) {
                $options['classmap'][$key] = $value;
            }
        }
        parent::__construct($wsdl, $options);
    }

    /**
     * Function to authenticate 
     *
     * @param Authenticate $parameters
     * @return AuthenticateResponse
     */
    public function authenticate(Authenticate $parameters) {
        return $this->__soapCall('authenticate', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function get user list
     *
     * @param GetUserList $parameters
     * @return GetUserListResponse
     */
    public function getUserList(GetUserList $parameters) {
        return $this->__soapCall('getUserList', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get the user claim value
     *
     * @param GetUserClaimValue $parameters
     * @return GetUserClaimValueResponse
     */
    public function getUserClaimValue(GetUserClaimValue $parameters) {
        return $this->__soapCall('getUserClaimValue', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get the user list of role
     *
     * @param GetUserListOfRole $parameters
     * @return GetUserListOfRoleResponse
     */
    public function getUserListOfRole(GetUserListOfRole $parameters) {
        return $this->__soapCall('getUserListOfRole', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to check whether the service is read only 
     *
     * @param IsReadOnly $parameters
     * @return IsReadOnlyResponse
     */
    public function isReadOnly(IsReadOnly $parameters) {
        return $this->__soapCall('isReadOnly', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to update the credentials
     *
     * @param UpdateCredential $parameters
     * @return void
     */
    public function updateCredential(UpdateCredential $parameters) {
        return $this->__soapCall('updateCredential', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to set user claim value 
     *
     * @param SetUserClaimValue $parameters
     * @return void
     */
    public function setUserClaimValue(setUserClaimValue $parameters) {
        return $this->__soapCall('setUserClaimValue', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get the claim values for claims
     *
     * @param GetUserClaimValuesForClaims $parameters
     * @return GetUserClaimValuesForClaimsResponse
     */
    public function getUserClaimValuesForClaims(GetUserClaimValuesForClaims $parameters) {
        return $this->__soapCall('getUserClaimValuesForClaims', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to delete user claim values 
     *
     * @param DeleteUserClaimValues $parameters
     * @return void
     */
    public function deleteUserClaimValues(DeleteUserClaimValues $parameters) {
        return $this->__soapCall('deleteUserClaimValues', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to delete user claim value 
     *
     * @param DeleteUserClaimValue $parameters
     * @return void
     */
    public function deleteUserClaimValue(DeleteUserClaimValue $parameters) {
        return $this->__soapCall('deleteUserClaimValue', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to check whether use is existing 
     *
     * @param IsExistingUser $parameters
     * @return IsExistingUserResponse
     */
    public function isExistingUser(IsExistingUser $parameters) {
        return $this->__soapCall('isExistingUser', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to update credential by admin
     *
     * @param UpdateCredentialByAdmin $parameters
     * @return void
     */
    public function updateCredentialByAdmin(UpdateCredentialByAdmin $parameters) {
        return $this->__soapCall('updateCredentialByAdmin', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get the tenant id
     *
     * @param GetTenantId $parameters
     * @return GetTenantIdResponse
     */
    public function getTenantId(GetTenantId $parameters) {
        return $this->__soapCall('getTenantId', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get role names 
     *
     * @param GetRoleNames $parameters
     * @return GetRoleNamesResponse
     */
    public function getRoleNames(GetRoleNames $parameters) {
        return $this->__soapCall('getRoleNames', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Funtion to get properties
     *
     * @param GetProperties $parameters
     * @return GetPropertiesResponse
     */
    public function getProperties(GetProperties $parameters) {
        return $this->__soapCall('getProperties', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get user id
     *
     * @param GetUserId $parameters
     * @return GetUserIdResponse
     */
    public function getUserId(GetUserId $parameters) {
        return $this->__soapCall('getUserId', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get all the profile names  
     *
     * @param GetAllProfileNames $parameters
     * @return GetAllProfileNamesResponse
     */
    public function getAllProfileNames(GetAllProfileNames $parameters) {
        return $this->__soapCall('getAllProfileNames', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get the password expiration time 
     *
     * @param GetPasswordExpirationTime $parameters
     * @return GetPasswordExpirationTimeResponse
     */
    public function getPasswordExpirationTime(GetPasswordExpirationTime $parameters) {
        return $this->__soapCall('getPasswordExpirationTime', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to list users
     *
     * @param ListUsers $parameters
     * @return ListUsersResponse
     */
    public function listUsers(ListUsers $parameters) {
        return $this->__soapCall('listUsers', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to delete role 
     *
     * @param DeleteRole $parameters
     * @return void
     */
    public function deleteRole(DeleteRole $parameters) {
        return $this->__soapCall('deleteRole', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to delete user 
     *
     * @param DeleteUser $parameters
     * @return void
     */
    public function deleteUser(DeleteUser $parameters) {
        return $this->__soapCall('deleteUser', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function get the role list of the user 
     *
     * @param GetRoleListOfUser $parameters
     * @return GetRoleListOfUserResponse
     */
    public function getRoleListOfUser(GetRoleListOfUser $parameters) {
        return $this->__soapCall('getRoleListOfUser', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to update the role name
     *
     * @param UpdateRoleName $parameters
     * @return void
     */
    public function updateRoleName(UpdateRoleName $parameters) {
        return $this->__soapCall('updateRoleName', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to check whether a role is existing 
     *
     * @param IsExistingRole $parameters
     * @return IsExistingRoleResponse
     */
    public function isExistingRole(IsExistingRole $parameters) {
        return $this->__soapCall('isExistingRole', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to update role list of user 
     *
     * @param UpdateRoleListOfUser $parameters
     * @return void
     */
    public function updateRoleListOfUser(UpdateRoleListOfUser $parameters) {
        return $this->__soapCall('updateRoleListOfUser', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get user claim values 
     *
     * @param GetUserClaimValues $parameters
     * @return GetUserClaimValuesResponse
     */
    public function getUserClaimValues(GetUserClaimValues $parameters) {
        return $this->__soapCall('getUserClaimValues', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get hybrid roles
     *
     * @param GetHybridRoles $parameters
     * @return GetHybridRolesResponse
     */
    public function getHybridRoles(GetHybridRoles $parameters) {
        return $this->__soapCall('getHybridRoles', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to add user claim values 
     *
     * @param AddUserClaimValues $parameters
     * @return void
     */
    public function addUserClaimValues(AddUserClaimValues $parameters) {
        return $this->__soapCall('addUserClaimValues', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to add user 
     *
     * @param AddUser $parameters
     * @return void
     */
    public function addUser(AddUser $parameters) {
        return $this->__soapCall('addUser', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to add role 
     *
     * @param AddRole $parameters
     * @return void
     */
    public function addRole(AddRole $parameters) {
        return $this->__soapCall('addRole', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to update user list of roles 
     *
     * @param UpdateUserListOfRole $parameters
     * @return void
     */
    public function updateUserListOfRole(UpdateUserListOfRole $parameters) {
        return $this->__soapCall('updateUserListOfRole', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get the tenant Id 
     *
     * @param GetTenantIdofUser $parameters
     * @return GetTenantIdofUserResponse
     */
    public function getTenantIdofUser(GetTenantIdofUser $parameters) {
        return $this->__soapCall('getTenantIdofUser', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to set user claim values 
     *
     * @param SetUserClaimValues $parameters
     * @return void
     */
    public function setUserClaimValues(SetUserClaimValues $parameters) {
        return $this->__soapCall('setUserClaimValues', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to add user claim value 
     *
     * @param AddUserClaimValue $parameters
     * @return void
     */
    public function addUserClaimValue(AddUserClaimValue $parameters) {
        return $this->__soapCall('addUserClaimValue', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

    /**
     * Function to get the profile names
     *
     * @param GetProfileNames $parameters
     * @return GetProfileNamesResponse
     */
    public function getProfileNames(GetProfileNames $parameters) {
        return $this->__soapCall('getProfileNames', array($parameters), array(
                    'uri' => 'http://service.ws.um.carbon.wso2.org',
                    'soapaction' => ''
        ));
    }

}

?>
