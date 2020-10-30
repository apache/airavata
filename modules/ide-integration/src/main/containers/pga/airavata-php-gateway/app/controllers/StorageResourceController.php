<?php

class StorageresourceController extends BaseController
{

    /**
     *    Instantiate a new Storage Resource Controller Instance
     **/

    public function __construct()
    {
        $this->beforeFilter('verifyadmin');
        Session::put("nav-active", "storage-resource");

    }

    public function createView()
    {
        $this->beforeFilter('verifyeditadmin');
        Session::put("admin-nav", "sr-create");
        $data = SRUtilities::getEditSRData();
        return View::make("storage-resource/create", $data);
    }

    public function createSubmit()
    {
        $this->beforeFilter('verifyeditadmin');
        $storageDescription = array(
            "hostName" => trim(Input::get("hostname")),
            "storageResourceDescription" => trim(Input::get("description")),
            "enabled" => true
        );
        $storageResource = SRUtilities::register_or_update_storage_resource($storageDescription);

        return Redirect::to("sr/edit?srId=" . $storageResource->storageResourceId);
    }

    public function editView()
    {
        $this->beforeFilter('verifyeditadmin');
        $data = SRUtilities::getEditSRData();
        $storageResourceId = "";
        if (Input::has("srId"))
            $storageResourceId = Input::get("srId");
        else if (Session::has("storageResource")) {
            $storageResource = Session::get("storageResource");
            $storageResourceId = $storageResource->storageResourceId;
        }
        if ($storageResourceId != "") {
            $storageResource = SRUtilities::get_storage_resource($storageResourceId);
            $dataMovementInterfaces = array();
            $addedDMI = array();
            
            if (count($storageResource->dataMovementInterfaces)) {
                foreach ($storageResource->dataMovementInterfaces as $DMI) {
                    $dataMovementInterfaces[] = SRUtilities::getDataMovementDetails($DMI->dataMovementInterfaceId, $DMI->dataMovementProtocol);
                    $addedDMI[] = $DMI->dataMovementProtocol;
                }
            }
            
            $data["storageResource"] = $storageResource;
            $data["dataMovementInterfaces"] = $dataMovementInterfaces;
            $data["addedDMI"] = $addedDMI;
            return View::make("storage-resource/edit", $data);
        } else{
            Session::put("message", "Unable to retrieve this Storage Resource. Please try again later or submit a bug report using the link in the Help menu.");
            return View::make("storage-resource/browse");
        }

    }

    public function editSubmit()
    {   
        $this->beforeFilter('verifyeditadmin');
        $tabName = "";
        if (Input::get("sr-edit") == "resDesc") /* Modify storage Resource description */ {
            $storageResourceDescription = SRUtilities::get_storage_resource(Input::get("srId"));
            $storageResourceDescription->hostName = trim(Input::get("hostname"));
            $storageResourceDescription->resourceDescription = Input::get("description");

            $storageResource = SRUtilities::register_or_update_storage_resource($storageResourceDescription, true);

            $tabName = "#tab-desc";
        }

        if (Input::get("sr-edit") == "dmp" || Input::get("sr-edit") == "edit-dmi") /* Add / Modify a Data Movement Interface */ {
            $update = false;
            if (Input::get("sr-edit") == "edit-dmi")
                $update = true;
            $dataMovementInterface = SRUtilities::create_or_update_DMIObject(Input::all(), $update);

            $tabName = "#tab-dataMovement";
        } else if (Input::get("sr-edit") == "dmi-priority") {
            $inputs = Input::all();
            $storageDescription = SRUtilities::get_storage_resource(Input::get("srId"));
            foreach ($storageDescription->dataMovementInterfaces as $index => $dmi) {
                foreach ($inputs["dmi-id"] as $idIndex => $dmiId) {
                    if ($dmiId == $dmi->dataMovementInterfaceId) {
                        $storageDescription->dataMovementInterfaces[$index]->priorityOrder = $inputs["dmi-priority"][$idIndex];
                        break;
                    }
                }
            }
            $storageResource = SRUtilities::register_or_update_storage_resource($storageDescription, true);

            return 1; //currently done by ajax.
        }

        return Redirect::to("sr/edit?srId=" . Input::get("srId") . $tabName);
    }

    /*
    public function srView()
    {
        $data = CRUtilities::getEditCRData();
        $computeResourceId = "";
        if (Input::has("crId"))
            $computeResourceId = Input::get("crId");
        else if (Session::has("computeResource")) {
            $computeResource = Session::get("computeResource");
            $computeResourceId = $computeResource->computeResourceId;
        }

        if ($computeResourceId != "") {
            $computeResource = CRUtilities::get_compute_resource($computeResourceId);
            $jobSubmissionInterfaces = array();
            $dataMovementInterfaces = array();
            $addedJSP = array();
            $addedDMI = array();
            //var_dump( $computeResource->jobSubmissionInterfaces); exit;
            if (count($computeResource->jobSubmissionInterfaces)) {
                foreach ($computeResource->jobSubmissionInterfaces as $JSI) {
                    $jobSubmissionInterfaces[] = CRUtilities::getJobSubmissionDetails($JSI->jobSubmissionInterfaceId, $JSI->jobSubmissionProtocol);
                    $addedJSP[] = $JSI->jobSubmissionProtocol;
                }
            }
            //var_dump( CRUtilities::getJobSubmissionDetails( $data["computeResource"]->jobSubmissionInterfaces[0]->jobSubmissionInterfaceId, 1) ); exit;
            if (count($computeResource->dataMovementInterfaces)) {
                foreach ($computeResource->dataMovementInterfaces as $DMI) {
                    $dataMovementInterfaces[] = CRUtilities::getDataMovementDetails($DMI->dataMovementInterfaceId, $DMI->dataMovementProtocol);
                    $addedDMI[] = $DMI->dataMovementProtocol;
                }
            }

            $data["computeResource"] = $computeResource;
            $data["jobSubmissionInterfaces"] = $jobSubmissionInterfaces;
            $data["dataMovementInterfaces"] = $dataMovementInterfaces;
            $data["addedJSP"] = $addedJSP;
            $data["addedDMI"] = $addedDMI;
            //var_dump($data["jobSubmissionInterfaces"]); exit;
            return View::make("resource/view", $data);
        } else
            return View::make("resource/browse")->with("login-alert", "Unable to retrieve this Compute Resource. Please report this error to devs.");

    }
    */

    public function deleteActions()
    {
        $this->beforeFilter('verifyeditadmin');
        $result = SRUtilities::deleteActions(Input::all());
        if (Input::has("dmiId")) {
            return Redirect::to("sr/edit?srId=" . Input::get("srId") . "#tab-dataMovement")
                ->with("message", "Data Movement Protocol was deleted successfully");
        } elseif (Input::has("del-srId")) {
            return Redirect::to("sr/browse")->with("message", "The Storage Resource " . Input::get("del-srId") . " has been successfully deleted.");
        } else
            return $result;
    }

    public function browseView()
    {
        $data = SRUtilities::getBrowseSRData(false);
        $allSRs = $data["srObjects"];

        Session::put("admin-nav", "sr-browse");
        return View::make("storage-resource/browse", array(
            "allSRs" => $allSRs
        ));

    }
}

?>