<?php
namespace Wsis\Stubs;
use SoapClient;
/**
 * This file contains the DTOs and the method stubs for
 * WSO2 IS 5.0.0 TenantMgtAdmin service.
 */
class TenantMgtAdminServiceException {
  public $TenantMgtAdminServiceException; // Exception
}


class deleteTenant {
  public $tenantDomain; // string
}

class updateTenant {
  public $tenantInfoBean; // TenantInfoBean
}

class addTenant {
  public $tenantInfoBean; // TenantInfoBean
}

class addTenantResponse {
  public $return; // string
}

class activateTenant {
  public $tenantDomain; // string
}

class deactivateTenant {
  public $tenantDomain; // string
}

class getTenant {
  public $tenantDomain; // string
}

class getTenantResponse {
  public $return; // TenantInfoBean
}

class retrievePaginatedTenants {
  public $pageNumber; // int
}

class retrievePaginatedTenantsResponse {
  public $return; // PaginatedTenantInfoBean
}

class retrieveTenants {
}

class retrieveTenantsResponse {
  public $return; // TenantInfoBean
}

class retrievePartialSearchTenants {
  public $domain; // string
}

class retrievePartialSearchTenantsResponse {
  public $return; // TenantInfoBean
}

class addSkeletonTenant {
  public $tenantInfoBean; // TenantInfoBean
}

class addSkeletonTenantResponse {
  public $return; // string
}

class retrievePaginatedPartialSearchTenants {
  public $domain; // string
  public $pageNumber; // int
}

class retrievePaginatedPartialSearchTenantsResponse {
  public $return; // PaginatedTenantInfoBean
}

class TenantInfoBean {
  public $active; // boolean
  public $admin; // string
  public $adminPassword; // string
  public $createdDate; // dateTime
  public $email; // string
  public $firstname; // string
  public $lastname; // string
  public $originatedService; // string
  public $successKey; // string
  public $tenantDomain; // string
  public $tenantId; // int
  public $usagePlan; // string
}

class PaginatedTenantInfoBean {
  public $numberOfPages; // int
  public $tenantInfoBeans; // TenantInfoBean
}


/**
 * TenantMgtAdminStub class
 * 
 *  
 * 
 * @author    {author}
 * @copyright {copyright}
 * @package   {package}
 */
class TenantMgtAdminStub extends SoapClient {

  private static $classmap = array(
                                    'TenantMgtAdminServiceException' => 'TenantMgtAdminServiceException',
                                    'deleteTenant' => 'deleteTenant',
                                    'updateTenant' => 'updateTenant',
                                    'addTenant' => 'addTenant',
                                    'addTenantResponse' => 'addTenantResponse',
                                    'activateTenant' => 'activateTenant',
                                    'deactivateTenant' => 'deactivateTenant',
                                    'getTenant' => 'getTenant',
                                    'getTenantResponse' => 'getTenantResponse',
                                    'retrievePaginatedTenants' => 'retrievePaginatedTenants',
                                    'retrievePaginatedTenantsResponse' => 'retrievePaginatedTenantsResponse',
                                    'retrieveTenants' => 'retrieveTenants',
                                    'retrieveTenantsResponse' => 'retrieveTenantsResponse',
                                    'retrievePartialSearchTenants' => 'retrievePartialSearchTenants',
                                    'retrievePartialSearchTenantsResponse' => 'retrievePartialSearchTenantsResponse',
                                    'addSkeletonTenant' => 'addSkeletonTenant',
                                    'addSkeletonTenantResponse' => 'addSkeletonTenantResponse',
                                    'retrievePaginatedPartialSearchTenants' => 'retrievePaginatedPartialSearchTenants',
                                    'retrievePaginatedPartialSearchTenantsResponse' => 'retrievePaginatedPartialSearchTenantsResponse',
                                    'TenantInfoBean' => 'TenantInfoBean',
                                    'PaginatedTenantInfoBean' => 'PaginatedTenantInfoBean',
                                   );

  public function TenantMgtAdminStub($wsdl, $options = array()) {
    foreach(self::$classmap as $key => $value) {
      if(!isset($options['classmap'][$key])) {
        $options['classmap'][$key] = $value;
      }
    }
    parent::__construct($wsdl, $options);
  }

  /**
   *  
   *
   * @param retrieveTenants $parameters
   * @return retrieveTenantsResponse
   */
  public function retrieveTenants(retrieveTenants $parameters) {
    return $this->__soapCall('retrieveTenants', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getTenant $parameters
   * @return getTenantResponse
   */
  public function getTenant(getTenant $parameters) {
    return $this->__soapCall('getTenant', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param deactivateTenant $parameters
   * @return void
   */
  public function deactivateTenant(deactivateTenant $parameters) {
    return $this->__soapCall('deactivateTenant', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param retrievePartialSearchTenants $parameters
   * @return retrievePartialSearchTenantsResponse
   */
  public function retrievePartialSearchTenants(retrievePartialSearchTenants $parameters) {
    return $this->__soapCall('retrievePartialSearchTenants', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param retrievePaginatedTenants $parameters
   * @return retrievePaginatedTenantsResponse
   */
  public function retrievePaginatedTenants(retrievePaginatedTenants $parameters) {
    return $this->__soapCall('retrievePaginatedTenants', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param updateTenant $parameters
   * @return void
   */
  public function updateTenant(updateTenant $parameters) {
    return $this->__soapCall('updateTenant', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param addSkeletonTenant $parameters
   * @return addSkeletonTenantResponse
   */
  public function addSkeletonTenant(addSkeletonTenant $parameters) {
    return $this->__soapCall('addSkeletonTenant', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param addTenant $parameters
   * @return addTenantResponse
   */
  public function addTenant(addTenant $parameters) {
    return $this->__soapCall('addTenant', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param retrievePaginatedPartialSearchTenants $parameters
   * @return retrievePaginatedPartialSearchTenantsResponse
   */
  public function retrievePaginatedPartialSearchTenants(retrievePaginatedPartialSearchTenants $parameters) {
    return $this->__soapCall('retrievePaginatedPartialSearchTenants', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param activateTenant $parameters
   * @return void
   */
  public function activateTenant(activateTenant $parameters) {
    return $this->__soapCall('activateTenant', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param deleteTenant $parameters
   * @return void
   */
  public function deleteTenant(deleteTenant $parameters) {
    return $this->__soapCall('deleteTenant', array($parameters),       array(
            'uri' => 'http://services.mgt.tenant.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

}

?>
