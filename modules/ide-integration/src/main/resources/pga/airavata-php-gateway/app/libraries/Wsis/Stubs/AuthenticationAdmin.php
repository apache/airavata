<?php
namespace Wsis\Stubs;

use Wsis\Stubs\AuthenticationAdminStub;

/**
 * AuthenticationAdmin class
 * 
 * This class provide an easy to use interface for
 * WSO2 IS 5.0.0 TenantMgtAdmin service.
 */
class AuthenticationAdmin {
    /**
     * @var AuthenticationAdminStub $serviceStub
     * @access private
     */
    private $serviceStub;

    public function __construct($server_url, $options) {
        $this->serviceStub = new AuthenticationAdminStub(
                $server_url . "AuthenticationAdmin?wsdl", $options
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
     * Method to login the user
     * @param $username
     * @param $password
     * @param $tenantDomain
     * @return true/false
     */
    public function login($username, $password, $tenantDomain){
        $parameters = new login();
        $parameters->username = $username. "@" . $tenantDomain;
        $parameters->password = $password;
        return $this->serviceStub->login($parameters);
    }

    /**
     * Method to logout the current user
     */
    public function logout(){
        $parameters = new logout();
        $this->serviceStub->logout($parameters);
    }
}
