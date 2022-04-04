<?php
namespace Wsis\Stubs;

use Wsis\Stubs\UserStoreManagerStub;

/**
 * UsersStoreManager class
 * 
 * This class provide an easy to use interface for
 * WSO2 IS 5.0.0 RemoteUserStoreManager service.
 */
class UserStoreManager {
    /**
     * @var RemoteUserManagerStub $serviceStub
     * @access private
     */
    private $serviceStub;

    public function __construct($server_url, $options) {
        $this->serviceStub = new UserStoreManagerStub(
                $server_url . "services/RemoteUserStoreManagerService?wsdl", $options
        );
    }
    
    /**
     * Function to get the soap client
     * 
     * @return SoapClient
     */
    public function getSoapClient(){
        return $this->serviceStub;
    }
    
    /**
     * Function to authenticate the user with RemoteUserStoreManager Service
     * @param type $username
     * @param type $password
     */
    public function authenticate($username, $password){
        $parameters = new Authenticate();
        $parameters->userName = $username;
        $parameters->credential = $password;
        return $this->serviceStub->authenticate($parameters)->return;
    }
    
    /**
     * Function to add new user by providing username and password
     * 
     * @param type $userName
     * @param type $password
     */
    public function addUser($userName, $password){
        $parameters = new AddUser();
        $parameters->userName = $userName;
        $parameters->credential = $password;
        $parameters->claims = null;
        $parameters->profileName = "default";
        $parameters->requirePasswordChange = false;
        $parameters->roleList = null;
        $this->serviceStub->addUser($parameters);
    }
    /**
     * Function to delete existing user by providing the username.
     * 
     * @param string $username
     */
    public function deleteUser($username){
        $parameters = new DeleteUser();
        $parameters->userName = $username;
        $this->serviceStub->deleteUser($parameters);
    }
    
    /**
     * Function to check whether a role is existing 
     *
     * @param string $roleName 
     * @return IsExistingRoleResponse
     */
    public function isExistingRole( $roleName) {
        $parameters = new IsExistingRole();
        $parameters->roleName = $roleName;
        $this->serviceStub->isExistingRole( $parameters)->return;
    }
    /**
     * Function to add new role by providing the role name.
     * 
     * @param string $roleName
     */
    public function addRole($roleName){
        $paramerters = new AddRole();
        $paramerters->roleName=$roleName;
        $paramerters->userList=null;
        $paramerters->permissions=null;
        $this->serviceStub->addRole($paramerters);
    }
    
    /**
     * Function to delete an existing role
     * 
     * @param string $roleName
     */
    public function deleteRole($roleName){
        $parameters = new DeleteRole();
        $parameters->roleName = $roleName;
        $this->serviceStub->deleteRole($parameters);
    }
    
    /**
     * Function to get a list of users
     * 
     * @return username list
     */
    public function listUsers(){
        $parameters = new ListUsers();
        $parameters->filter = "*";
        $parameters->maxItemLimit = -1;
        
        return $this->serviceStub->listUsers($parameters)->return;
    }

    /**
     * Function to search users
     * @param $phrase
     * @return string
     */
    public function searchUsers($phrase){
        $parameters = new ListUsers();
        $parameters->filter = "*" . $phrase . "*";
        $parameters->maxItemLimit = -1;
        if(isset($this->serviceStub->listUsers($parameters)->return)){
            return $this->serviceStub->listUsers($parameters)->return;
        }
    }

     /**
     * Function get user list
     *
     * @param GetUserList $parameters
     * @return GetUserListResponse
     */
     public function getUserList(){
        $parameters = new GetUserList();
    }

        
    /**
     * Function to check whether the given username already exists
     * 
     * @param string $username
     * @return boolean
     */
    public function isExistingUser($username) {
        $parameters = new IsExistingUser();
        $parameters->userName = $username;
        
        return $this->serviceStub->isExistingUser($parameters)->return;
    }

    /**
    * Function to get the list of all existing roles
    *
    * @return roles list
    */
    public function getRoleNames( $parameters = null){
        $parameters = new GetRoleNames();
        return $this->serviceStub->getRoleNames( $parameters)->return;
    }

    /**
    * Function to get role of a user
    *
    * @return User Role
    */
    public function getRoleListOfUser( $username){
        $parameters = new GetRoleListOfUser();
        $parameters->userName = $username;
        return $this->serviceStub->GetRoleListOfUser( $parameters)->return;
    }

    /**
     * Function to get the user list of role
     *
     * @param GetUserListOfRole $parameters
     * @return GetUserListOfRoleResponse
     */
    public function getUserListOfRole( $roleName){
        $parameters = new GetUserListOfRole();
        $parameters->roleName = $roleName;
        return $this->serviceStub->getUserListOfRole( $parameters);
    }
    
    /**
     * Function to update role list of user 
     *
     * @param UpdateRoleListOfUser $parameters
     * @return void
     */
    public function updateRoleListOfUser( $username, $roles){
        if(isset($roles["deleted"])) {
            if(!is_array($roles["deleted"]))
                $roles["deleted"] = array($roles["deleted"]);
            foreach ($roles["deleted"] as $role) {
                $parameters = new UpdateRoleListOfUser();
                $parameters->userName = $username;
                $parameters->deletedRoles = $role;
                $this->serviceStub->updateRoleListOfUser($parameters);
            }
        }
        if(isset($roles["new"])){
            if(!is_array($roles["new"]))
                $roles["new"] = array($roles["new"]);
            foreach ($roles["new"] as $role) {
                $parameters = new UpdateRoleListOfUser();
                $parameters->userName = $username;
                $parameters->newRoles = $role;
                $this->serviceStub->updateRoleListOfUser( $parameters);
            }
        }
        return;
    }

    /**
     * Function to get the tenant id
     *
     * @param GetTenantId $parameters
     * @return GetTenantIdResponse
     */
    public function getTenantId(){
        $parameters = new GetTenantId();

        return $this->serviceStub->getTenantId( $parameters);
    }
}
