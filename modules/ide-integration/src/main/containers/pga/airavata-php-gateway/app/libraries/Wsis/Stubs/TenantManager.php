<?php
namespace Wsis\Stubs;

use Wsis\Stubs\TenantMgtAdminStub;

/**
 * TenantManager class
 * 
 * This class provide an easy to use interface for
 * WSO2 IS 5.0.0 TenantMgtAdmin service.
 */
class TenantManager {
    /**
     * @var TenantMgtAdminStub $serviceStub
     * @access private
     */
    private $serviceStub;

    public function __construct($server_url, $options) {
        $this->serviceStub = new TenantMgtAdminStub(
                $server_url . "services/TenantMgtAdminService?wsdl", $options
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
     * Method to retrieve all  tenant information. Some tenant information
     * such as admin name are not returned by wso2 IS
     * @return mixed
     */
    public function retrieveTenants(){
        $parameters = new retrieveTenants();
        return $this->serviceStub->retrieveTenants($parameters)->return;
    }

    /**
     * Method to get tenant information giving tenant domain
     * @param $domain domain of the tenant
     * @return mixed
     */
    public function getTenant($domain){
        $parameters = new getTenant();
        $parameters->tenantDomain = $domain;
        return $this->serviceStub->getTenant($parameters)->return;
    }

    /**
     * Method to create a new tenant
     * @param $active whether tenant active or not
     * @param $adminUsername
     * @param $adminPassword
     * @param $email
     * @param $firstName Admin's first name
     * @param $lastName Admin's last name
     * @param $tenantDomain
     */
    public function addTenant($active, $adminUsername, $adminPassword, $email,
                              $firstName, $lastName, $tenantDomain){
        $tenantInfoBean = new TenantInfoBean();
        $tenantInfoBean->active = $active;
        $tenantInfoBean->admin = $adminUsername;
        $tenantInfoBean->adminPassword = $adminPassword;
        $tenantInfoBean->email = $email;
        $tenantInfoBean->firstname = $firstName;
        $tenantInfoBean->lastname = $lastName;
        $tenantInfoBean->tenantDomain = $tenantDomain;

        $addTenant  = new addTenant();
        $addTenant->tenantInfoBean = $tenantInfoBean;
        return $this->serviceStub->addTenant($addTenant);
    }

    /**
     * Method to remove an existing tenant giving tenant domain
     * @param $tenantDomain
     */
    public function deleteTenant($tenantDomain){
        $parameters = new deleteTenant();
        $parameters->tenantDomain = $tenantDomain;
        $this->serviceStub->deleteTenant($parameters);
    }

    /**
     * Method to activate a tenant
     * @param $tenantDomain
     */
    public function activateTenant($tenantDomain){
        $parameters = new activateTenant();
        $parameters->tenantDomain = $tenantDomain;
        $this->serviceStub->activateTenant($parameters);
    }

    /**
     * Method to deactivate a tenant
     * @param $tenantDomain
     */
    public function deactivateTenant($tenantDomain){
        $parameters = new deactivateTenant();
        $parameters->tenantDomain = $tenantDomain;
        $this->serviceStub->deactivateTenant($parameters);
    }

    /**
     * Method to update an existing tenant
     * @param $tenantId
     * @param $active
     * @param $adminUsername
     * @param $adminPassword
     * @param $email
     * @param $firstName
     * @param $lastName
     * @param $tenantDomain
     */
    public function updateTenant($tenantId, $active, $adminUsername, $adminPassword, $email,
                              $firstName, $lastName, $tenantDomain){
        $tenantInfoBean = new TenantInfoBean();
        $tenantInfoBean->tenantId = $tenantId;
        $tenantInfoBean->active = $active;
        $tenantInfoBean->admin = $adminUsername;
        $tenantInfoBean->adminPassword = $adminPassword;
        $tenantInfoBean->email = $email;
        $tenantInfoBean->firstName = $firstName;
        $tenantInfoBean->lastName = $lastName;
        $tenantInfoBean->tenantDomain = $tenantDomain;

        $updateTenant  = new updateTenant();
        $updateTenant->tenantInfoBean = $tenantInfoBean;
        $this->serviceStub->updateTenant($updateTenant);
    }

}
