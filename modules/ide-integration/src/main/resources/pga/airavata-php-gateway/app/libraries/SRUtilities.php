<?php


//Airavata classes - loaded from app/libraries/Airavata
//Compute Resource classes
use Airavata\Model\Data\Movement\DataMovementProtocol;
use Airavata\Model\AppCatalog\StorageResource\StorageResourceDescription;
use Airavata\Model\AppCatalog\GatewayProfile\StoragePreference;
use Airavata\Model\AppCatalog\GatewayProfile\GatewayResourceProfile;

use Airavata\Model\AppCatalog\ComputeResource\GridFTPDataMovement;
use Airavata\Model\AppCatalog\ComputeResource\JobManagerCommand;
use Airavata\Model\AppCatalog\ComputeResource\JobSubmissionProtocol;
use Airavata\Model\Data\Movement\LOCALDataMovement;
use Airavata\Model\Data\Movement\DMType;
use Airavata\Model\AppCatalog\ComputeResource\MonitorMode;
use Airavata\Model\AppCatalog\ComputeResource\ResourceJobManagerType;
use Airavata\Model\Data\Movement\SCPDataMovement;
use Airavata\Model\Data\Movement\SecurityProtocol;
use Airavata\Model\Data\Movement\UnicoreDataMovement;
//Gateway Classes


class SRUtilities
{
    /**
     * Basic utility functions
     */

//define('ROOT_DIR', __DIR__);

    /**
     * Define configuration constants
     */
    public static function register_or_update_storage_resource($storageResourceDesc, $update = false)
    {
        if ($update) {
            $storageResourceId = $storageResourceDesc->storageResourceId;
            
            if (Airavata::updateStorageResource(Session::get('authz-token'), $storageResourceId, $storageResourceDesc)) {
                $storageResource = Airavata::getStorageResource(Session::get('authz-token'), $storageResourceId);
                return $storageResource;
            } else
                print_r("Something went wrong while updating!");
            exit;
        } else {
            $sr = new StorageResourceDescription( $storageResourceDesc);
            $storageResourceId = Airavata::registerStorageResource(Session::get('authz-token'), $sr);
        }
        $storageResource = Airavata::getStorageResource(Session::get('authz-token'), $storageResourceId);
        return $storageResource;

    }

    /*
     * Getting data for Compute resource inputs
    */

    public static function getEditSRData()
    {
        $rjmt = new ResourceJobManagerType();
        $sp = new SecurityProtocol();
        $dmp = new DataMovementProtocol();
        $jmc = new JobManagerCommand();
        $mm = new MonitorMode();
        return array(
            "resourceJobManagerTypesObject" => $rjmt,
            "resourceJobManagerTypes" => $rjmt::$__names,
            "securityProtocolsObject" => $sp,
            "securityProtocols" => $sp::$__names,
            "dataMovementProtocolsObject" => $dmp,
            "dataMovementProtocols" => $dmp::$__names,
            "jobManagerCommands" => $jmc::$__names,
            "monitorModes" => $mm::$__names
        );
    }

    /*
     * Creating Data Movement Interface Object.
    */
    public static function create_or_update_DMIObject($inputs, $update = false)
    {

        $storageResource = SRUtilities::get_storage_resource($inputs["srId"]);

        if ($inputs["dataMovementProtocol"] == DataMovementProtocol::LOCAL) /* LOCAL */ {
            $localDataMovement = new LOCALDataMovement();
            $localdmp = Airavata::addLocalDataMovementDetails(Session::get('authz-token'), $storageResource->storageResourceId, DMType::STORAGE_RESOURCE, 0, $localDataMovement);

        } else if ($inputs["dataMovementProtocol"] == DataMovementProtocol::SCP) /* SCP */ {
            $scpDataMovement = new SCPDataMovement(array(
                    "dataMovementInterfaceId" => $inputs["dmiId"],
                    "securityProtocol" => intval($inputs["securityProtocol"]),
                    "alternativeSCPHostName" => $inputs["alternativeSSHHostName"],
                    "sshPort" => intval($inputs["sshPort"])
                )
            );

            if ($update)
                $scpdmp = Airavata::updateSCPDataMovementDetails(Session::get('authz-token'), $inputs["dmiId"], $scpDataMovement);
            else
                $scpdmp = Airavata::addSCPDataMovementDetails(Session::get('authz-token'), $storageResource->storageResourceId, DMType::STORAGE_RESOURCE, 0, $scpDataMovement);
        } else if ($inputs["dataMovementProtocol"] == DataMovementProtocol::GridFTP) /* GridFTP */ {
            $gridFTPDataMovement = new GridFTPDataMovement(array(
                "securityProtocol" => $inputs["securityProtocol"],
                "gridFTPEndPoints" => $inputs["gridFTPEndPoints"]
            ));
            if ($update)
                $gridftpdmp = Airavata::updateGridFTPDataMovementDetails(Session::get('authz-token'), $inputs["dmiId"], $gridFTPDataMovement);
            else
                $gridftpdmp = Airavata::addGridFTPDataMovementDetails(Session::get('authz-token'), $storageResource->storageResourceId, DMType::STORAGE_RESOURCE, 0, $gridFTPDataMovement);
        } else if ($inputs["dataMovementProtocol"] == DataMovementProtocol::UNICORE_STORAGE_SERVICE) /* Unicore Storage Service */ {
            $unicoreDataMovement = new UnicoreDataMovement(array
                (
                    "securityProtocol" => intval($inputs["securityProtocol"]),
                    "unicoreEndPointURL" => $inputs["unicoreEndPointURL"]
                )
            );
            if ($update)
                $unicoredmp = Airavata::updateUnicoreDataMovementDetails(Session::get('authz-token'), $inputs["dmiId"], $unicoreDataMovement);
            else
                $unicoredmp = Airavata::addUnicoreDataMovementDetails(Session::get('authz-token'), $storageResource->storageResourceId, DMType::STORAGE_RESOURCE, 0, $unicoreDataMovement);
        } else /* other data movement protocols */ {
            print_r("Whoops! We haven't coded for this Data Movement Protocol yet. Still working on it. Please click <a href='" . URL::to('/') . "/cr/edit'>here</a> to go back to edit page for compute resource.");
        }
    }

    public static function getAllSRObjects($onlyName = false)
    {
        $srNames = Airavata::getAllStorageResourceNames(Session::get('authz-token'));
        if ($onlyName)
            return $srNames;
        else {
            $srObjects = array();
            foreach ($srNames as $id => $srName) {
                array_push($srObjects, Airavata::getStorageResource(Session::get('authz-token'), $id));
            }
            return $srObjects;
        }

    }

    public static function getBrowseSRData($onlyNames)
    {   /*
        $appDeployments = Airavata::getAllApplicationDeployments(Session::get('authz-token'), 
                                                                        Session::get("gateway_id"));
        */
        return array('srObjects' => SRUtilities::getAllSRObjects($onlyNames) );
    }

    public static function getJobSubmissionDetails($jobSubmissionInterfaceId, $jsp)
    {
        //jsp = job submission protocol type
        if ($jsp == JobSubmissionProtocol::LOCAL)
            return Airavata::getLocalJobSubmission(Session::get('authz-token'), $jobSubmissionInterfaceId);
        else if ($jsp == JobSubmissionProtocol::SSH || $jsp == JobSubmissionProtocol::SSH_FORK)
            return Airavata::getSSHJobSubmission(Session::get('authz-token'), $jobSubmissionInterfaceId);
        else if ($jsp == JobSubmissionProtocol::UNICORE)
            return Airavata::getUnicoreJobSubmission(Session::get('authz-token'), $jobSubmissionInterfaceId);
        else if ($jsp == JobSubmissionProtocol::CLOUD)
            return Airavata::getCloudJobSubmission(Session::get('authz-token'), $jobSubmissionInterfaceId);

        //globus get function not present ??
    }

    public static function getDataMovementDetails($dataMovementInterfaceId, $dmi)
    {
        //jsp = job submission protocol type
        if ($dmi == DataMovementProtocol::LOCAL)
            return Airavata::getLocalDataMovement(Session::get('authz-token'), $dataMovementInterfaceId);
        else if ($dmi == DataMovementProtocol::SCP)
            return Airavata::getSCPDataMovement(Session::get('authz-token'), $dataMovementInterfaceId);
        else if ($dmi == DataMovementProtocol::GridFTP)
            return Airavata::getGridFTPDataMovement(Session::get('authz-token'), $dataMovementInterfaceId);
        else if ($dmi == DataMovementProtocol::UNICORE_STORAGE_SERVICE)
            return Airavata::getUnicoreDataMovement(Session::get('authz-token'), $dataMovementInterfaceId);
        /*
        else if( $dmi == JobSubmissionProtocol::CLOUD)
            return $airavataclient->getCloudJobSubmission( $dataMovementInterfaceId);
        */

        //globus get function not present ??
    }

    public static function deleteActions($inputs)
    {
        if (isset($inputs["srId"])) {
            if (Config::get('pga_config.airavata')['enable-app-catalog-cache']) {
                if (Cache::has('SR-' . $inputs["srId"])) {
                    Cache::forget('SR-' . $inputs["srId"]);
                }
            }
        } elseif (isset($inputs["del-srId"])) {
            if (Config::get('pga_config.airavata')['enable-app-catalog-cache']) {
                if (Cache::has('SR-' . $inputs["del-srId"])) {
                    Cache::forget('SR-' . $inputs["del-srId"]);
                }
            }
        }
        if (isset($inputs["jsiId"]))
            if (Airavata::deleteJobSubmissionInterface(Session::get('authz-token'), $inputs["srId"], $inputs["jsiId"]))
                return 1;
            else
                return 0;
        else if (isset($inputs["dmiId"]))
            if (Airavata::deleteDataMovementInterface(Session::get('authz-token'), $inputs["srId"], $inputs["dmiId"], 1))
                return 1;
            else
                return 0;
        elseif (isset($inputs["del-srId"]))
            if (Airavata::deleteStorageResource(Session::get('authz-token'), $inputs["del-srId"]))
                return 1;
            else
                return 0;
    }

    public static function create_or_update_gateway_profile($inputs, $update = false)
    {

        $computeResourcePreferences = array();
        if (isset($input["crPreferences"]))
            $computeResourcePreferences = $input["crPreferences"];

        $gatewayProfile = new GatewayResourceProfile(array(
                "gatewayName" => $inputs["gatewayName"],
                "gatewayDescription" => $inputs["gatewayDescription"],
                "computeResourcePreferences" => $computeResourcePreferences
            )
        );

        if ($update) {
            $gatewayProfile = new GatewayResourceProfile(array(
                    "gatewayName" => $inputs["gatewayName"],
                    "gatewayDescription" => $inputs["gatewayDescription"]
                )
            );
            $gatewayProfileId = Airavata::updateGatewayResourceProfile(Session::get('authz-token'), $inputs["edit-gpId"], $gatewayProfile);
        } else
            $gatewayProfileId = Airavata::registerGatewayResourceProfile(Session::get('authz-token'), $gatewayProfile);
    }

    public static function updateGatewayProfile( $data){
        $gatewayResourceProfile = Airavata::getGatewayResourceProfile( Session::get('authz-token'), $data["gateway_id"]);
        $gatewayResourceProfile->credentialStoreToken = $data["cst"];
        return Airavata::updateGatewayResourceProfile( Session::get('authz-token'), $data["gateway_id"], $gatewayResourceProfile); 
    }

    public static function add_or_update_SRP($inputs)
    {
        $storagePreference = new StoragePreference($inputs);

        if (Config::get('pga_config.airavata')['enable-app-catalog-cache']) {
            if (Cache::has('SR-' . $inputs["storageResourceId"])) {
                Cache::forget('SR-' . $inputs["storageResourceId"]);
            }
        }
        return Airavata::addGatewayStoragePreference(   Session::get('authz-token'), 
                                                        $inputs["gatewayId"], 
                                                        $inputs["storageResourceId"],
                                                        $storagePreference
                                                    );
    }

    public static function deleteGP($gpId)
    {
        return Airavata::deleteGatewayResourceProfile(Session::get('authz-token'), $gpId);
    }

    public static function deleteCR($inputs)
    {
        if (Config::get('pga_config.airavata')['enable-app-catalog-cache']) {
            $id = $inputs["rem-crId"];
            if (Cache::has('CR-' . $id)) {
                Cache::forget('CR-' . $id);
            }
        }

        return Airavata::deleteGatewayComputeResourcePreference(Session::get('authz-token'), $inputs["gpId"], $inputs["rem-crId"]);
    }

    /**
     * Get the ComputeResourceDescription with the given ID
     * @param $id
     * @return null
     */
    public static function get_storage_resource($id)
    {
        $computeResource = null;
        try {
            /*
            if (Config::get('pga_config.airavata')['enable-app-catalog-cache']) {
                if (Cache::has('CR-' . $id)) {
                    return Cache::get('CR-' . $id);
                } else {
                    $computeResource = Airavata::getComputeResource(Session::get('authz-token'), $id);
                    Cache::put('CR-' . $id, $computeResource, Config::get('pga_config.airavata')['app-catalog-cache-duration']);
                    return $computeResource;
                }
            } else {
            */
                return Airavata::getStorageResource(Session::get('authz-token'), $id);
            /*
            }
            */

        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('<p>There was a problem getting the storage resource.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>InvalidRequestException: ' . $ire->getMessage() . '</p>');
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('<p>There was a problem getting the storage resource.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>Airavata Client Exception: ' . $ace->getMessage() . '</p>');
        } catch (AiravataSystemException $ase) {
            CommonUtilities::print_error_message('<p>There was a problem getting the storage resource.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>Airavata System Exception: ' . $ase->getMessage() . '</p>');
        }
    }


    /**
     * Create a select input and populate it with compute resources
     * available for the given application ID
     * @param $applicationId
     * @param $resourceHostId
     */
    public static function create_compute_resources_select($applicationId, $resourceHostId)
    {
        return CRUtilities::get_available_app_interface_compute_resources($applicationId);
    }

    /**
     * Get a list of compute resources available for the given application ID
     * @param $id
     * @return null
     */
    public static function get_available_app_interface_compute_resources($id)
    {
        $computeResources = null;

        try {
            $computeResources = Airavata::getAvailableAppInterfaceComputeResources(Session::get('authz-token'), $id);
        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('<p>There was a problem getting compute resources.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>InvalidRequestException: ' . $ire->getMessage() . '</p>');
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('<p>There was a problem getting compute resources.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>Airavata Client Exception: ' . $ace->getMessage() . '</p>');
        } catch (AiravataSystemException $ase) {
            CommonUtilities::print_error_message('<p>There was a problem getting compute resources.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>Airavata System Exception: ' . $ase->getMessage() . '</p>');
        }

        return $computeResources;
    }

    /**
    * Get a list of all Data Storages available 
    * @param null
    * @return 
    **/

    public static function getAllDataStoragePreferences( $gateways){
        $srpArray = array();
        foreach( $gateways as $gateway){
            $srpArray[] = Airavata::getAllGatewayDataStoragePreferences( Session::get('authz-token'), $gateway->gatewayId);
        }
        return $srpArray;
    }

}

?>