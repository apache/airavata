<?php
namespace Wsis\Stubs;
use SoapClient;

class UserProfileMgtServiceUserProfileException {
  public $UserProfileException; // UserProfileException
}

class isReadOnlyUserStore {
}

class isReadOnlyUserStoreResponse {
  public $return; // boolean
}

class getUserProfiles {
  public $username; // string
}

class getUserProfilesResponse {
  public $return; // UserProfileDTO
}

class setUserProfile {
  public $username; // string
  public $profile; // UserProfileDTO
}

class deleteUserProfile {
  public $username; // string
  public $profileName; // string
}

class getUserProfile {
  public $username; // string
  public $profileName; // string
}

class getUserProfileResponse {
  public $return; // UserProfileDTO
}

class getProfileFieldsForInternalStore {
}

class getProfileFieldsForInternalStoreResponse {
  public $return; // UserProfileDTO
}

class isAddProfileEnabled {
}

class isAddProfileEnabledResponse {
  public $return; // boolean
}

class isAddProfileEnabledForDomain {
  public $domain; // string
}

class isAddProfileEnabledForDomainResponse {
  public $return; // boolean
}

class associateID {
  public $idpID; // string
  public $associatedID; // string
}

class getNameAssociatedWith {
  public $idpID; // string
  public $associatedID; // string
}

class getNameAssociatedWithResponse {
  public $return; // string
}

class getAssociatedIDs {
  public $userName; // string
}

class getAssociatedIDsResponse {
  public $return; // string
}

class removeAssociateID {
  public $idpID; // string
  public $associatedID; // string
}

class getInstance {
}

class getInstanceResponse {
  public $return; // UserProfileAdmin
}

class AbstractAdmin {
}

class UserProfileException {
}

class UserProfileDTO {
  public $fieldValues; // UserFieldDTO
//  public $profileConfigurations; // string
//  public $profileConifuration; // string
  public $profileName; // string
}

class UserFieldDTO {
//  public $checkedAttribute; // boolean
  public $claimUri; // string
//  public $displayName; // string
//  public $displayOrder; // int
  public $fieldValue; // string
//  public $readOnly; // boolean
//  public $regEx; // string
//  public $required; // boolean
}

class UserProfileAdmin {
  public $addProfileEnabled; // boolean
  public $profileFieldsForInternalStore; // UserProfileDTO
  public $readOnlyUserStore; // boolean
}


/**
 * UserProfileManagerStub class
 * 
 *  
 * 
 * @author    {author}
 * @copyright {copyright}
 * @package   {package}
 */
class UserProfileManagerStub extends SoapClient {

  private static $classmap = array(
                                    'UserProfileMgtServiceUserProfileException' => 'UserProfileMgtServiceUserProfileException',
                                    'isReadOnlyUserStore' => 'isReadOnlyUserStore',
                                    'isReadOnlyUserStoreResponse' => 'isReadOnlyUserStoreResponse',
                                    'getUserProfiles' => 'getUserProfiles',
                                    'getUserProfilesResponse' => 'getUserProfilesResponse',
                                    'setUserProfile' => 'setUserProfile',
                                    'deleteUserProfile' => 'deleteUserProfile',
                                    'getUserProfile' => 'getUserProfile',
                                    'getUserProfileResponse' => 'getUserProfileResponse',
                                    'getProfileFieldsForInternalStore' => 'getProfileFieldsForInternalStore',
                                    'getProfileFieldsForInternalStoreResponse' => 'getProfileFieldsForInternalStoreResponse',
                                    'isAddProfileEnabled' => 'isAddProfileEnabled',
                                    'isAddProfileEnabledResponse' => 'isAddProfileEnabledResponse',
                                    'isAddProfileEnabledForDomain' => 'isAddProfileEnabledForDomain',
                                    'isAddProfileEnabledForDomainResponse' => 'isAddProfileEnabledForDomainResponse',
                                    'associateID' => 'associateID',
                                    'getNameAssociatedWith' => 'getNameAssociatedWith',
                                    'getNameAssociatedWithResponse' => 'getNameAssociatedWithResponse',
                                    'getAssociatedIDs' => 'getAssociatedIDs',
                                    'getAssociatedIDsResponse' => 'getAssociatedIDsResponse',
                                    'removeAssociateID' => 'removeAssociateID',
                                    'getInstance' => 'getInstance',
                                    'getInstanceResponse' => 'getInstanceResponse',
                                    'AbstractAdmin' => 'AbstractAdmin',
                                    'UserProfileException' => 'UserProfileException',
                                    'UserProfileDTO' => 'UserProfileDTO',
                                    'UserFieldDTO' => 'UserFieldDTO',
                                    'UserProfileAdmin' => 'UserProfileAdmin',
                                   );

  public function UserProfileManagerStub($wsdl = "UserProfileMgtService.xml", $options = array()) {
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
   * @param isAddProfileEnabled $parameters
   * @return isAddProfileEnabledResponse
   */
  public function isAddProfileEnabled(isAddProfileEnabled $parameters) {
    return $this->__soapCall('isAddProfileEnabled', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getInstance $parameters
   * @return getInstanceResponse
   */
  public function getInstance(getInstance $parameters) {
    return $this->__soapCall('getInstance', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param isReadOnlyUserStore $parameters
   * @return isReadOnlyUserStoreResponse
   */
  public function isReadOnlyUserStore(isReadOnlyUserStore $parameters) {
    return $this->__soapCall('isReadOnlyUserStore', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param deleteUserProfile $parameters
   * @return void
   */
  public function deleteUserProfile(deleteUserProfile $parameters) {
    return $this->__soapCall('deleteUserProfile', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param isAddProfileEnabledForDomain $parameters
   * @return isAddProfileEnabledForDomainResponse
   */
  public function isAddProfileEnabledForDomain(isAddProfileEnabledForDomain $parameters) {
    return $this->__soapCall('isAddProfileEnabledForDomain', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getUserProfile $parameters
   * @return getUserProfileResponse
   */
  public function getUserProfile(getUserProfile $parameters) {
    return $this->__soapCall('getUserProfile', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getProfileFieldsForInternalStore $parameters
   * @return getProfileFieldsForInternalStoreResponse
   */
  public function getProfileFieldsForInternalStore(getProfileFieldsForInternalStore $parameters) {
    return $this->__soapCall('getProfileFieldsForInternalStore', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param associateID $parameters
   * @return void
   */
  public function associateID(associateID $parameters) {
    return $this->__soapCall('associateID', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param removeAssociateID $parameters
   * @return void
   */
  public function removeAssociateID(removeAssociateID $parameters) {
    return $this->__soapCall('removeAssociateID', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getAssociatedIDs $parameters
   * @return getAssociatedIDsResponse
   */
  public function getAssociatedIDs(getAssociatedIDs $parameters) {
    return $this->__soapCall('getAssociatedIDs', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getUserProfiles $parameters
   * @return getUserProfilesResponse
   */
  public function getUserProfiles(getUserProfiles $parameters) {
    return $this->__soapCall('getUserProfiles', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param setUserProfile $parameters
   * @return void
   */
  public function setUserProfile(setUserProfile $parameters) {
    return $this->__soapCall('setUserProfile', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getNameAssociatedWith $parameters
   * @return getNameAssociatedWithResponse
   */
  public function getNameAssociatedWith(getNameAssociatedWith $parameters) {
    return $this->__soapCall('getNameAssociatedWith', array($parameters),       array(
            'uri' => 'http://mgt.profile.user.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

}

?>
