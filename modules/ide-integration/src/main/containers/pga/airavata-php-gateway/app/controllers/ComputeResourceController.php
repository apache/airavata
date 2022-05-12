<?php

class ComputeResourceController extends BaseController
{

    /**
     *    Instantiate a new Compute Resource Controller Instance
     **/

    public function __construct()
    {
        $this->beforeFilter('verifyadmin');
        Session::put("nav-active", "compute-resource");

    }

    public function createView()
    {
        $this->beforeFilter('verifyeditadmin');
        Session::put("admin-nav", "cr-create");
        return View::make("resource/create");
    }

    public function createSubmit()
    {
        $this->beforeFilter('verifyeditadmin');
        $hostAliases = Input::get("hostaliases");
        $ips = Input::get("ips");
        //Compute resource is by default enabled
        $computeDescription = array(
            "hostName" => trim(Input::get("hostname")),
            "hostAliases" => array_unique(array_filter($hostAliases)),
            "ipAddresses" => array_unique(array_filter($ips)),
            "resourceDescription" => Input::get("description"),
            "enabled" => true
        );
        $computeResource = CRUtilities::register_or_update_compute_resource($computeDescription);

        return Redirect::to("cr/edit?crId=" . $computeResource->computeResourceId);
    }

    public function editView()
    {
        $this->beforeFilter('verifyeditadmin');
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
            return View::make("resource/edit", $data);
        } else
            return View::make("resource/browse")->with("login-alert", "Unable to retrieve this Compute Resource. Please report this error to devs.");

    }

    public function editSubmit()
    {
        $this->beforeFilter('verifyeditadmin');
        $tabName = "";
        if (Input::get("cr-edit") == "resDesc") /* Modify compute Resource description */ {
            $computeDescription = CRUtilities::get_compute_resource(Input::get("crId"));
            $computeDescription->hostName = trim(Input::get("hostname"));
            $computeDescription->hostAliases = array_unique(array_filter(Input::get("hostaliases")));
            $computeDescription->ipAddresses = array_unique(array_filter(Input::get("ips")));
            $computeDescription->resourceDescription = Input::get("description");
            $computeDescription->maxMemoryPerNode = Input::get("maxMemoryPerNode");
//            $computeDescription->cpusPerNode = Input::get("cpusPerNode");
//            $computeDescription->defaultNodeCount = Input::get("defaultNodeCount");
//            $computeDescription->defaultCPUCount = Input::get("defaultCPUCount");
//            $computeDescription->defaultWalltime = Input::get("defaultWalltime");
            //var_dump( $computeDescription); exit;

            $computeResource = CRUtilities::register_or_update_compute_resource($computeDescription, true);

            $tabName = "#tab-desc";
        }
        if (Input::get("cr-edit") == "queue") /* Add / Modify a Queue */ {
            $queue = array("queueName" => Input::get("qname"),
                "queueDescription" => Input::get("qdesc"),
                "maxRunTime" => Input::get("qmaxruntime"),
                "maxNodes" => Input::get("qmaxnodes"),
                "maxProcessors" => Input::get("qmaxprocessors"),
                "maxJobsInQueue" => Input::get("qmaxjobsinqueue"),
                "maxMemory" => Input::get("qmaxmemoryinqueue"),
                "cpuPerNode" => Input::get("cpuPerNode"),
                "defaultNodeCount" => Input::get("defaultNodeCount"),
                "defaultCPUCount" => Input::get("defaultCPUCount"),
                "defaultWalltime" => Input::get("defaultWalltime"),
                "queueSpecificMacros" => Input::get("queueSpecificMacros"),
                "isDefaultQueue" => Input::get('isDefaultQueue') == 'on'
            );

            $computeDescription = CRUtilities::get_compute_resource(Input::get("crId"));
            $updatedQueues = [];
            if($queue["isDefaultQueue"]){
                foreach($computeDescription->batchQueues as $aQueue){
                    $aQueue->isDefaultQueue = false;
                    $updatedQueues[] = $aQueue;
                }
            }else{
                $updatedQueues = $computeDescription->batchQueues;
            }
            $updatedQueues[] = CRUtilities::createQueueObject($queue);
            $computeDescription->batchQueues = $updatedQueues;
            $computeResource = CRUtilities::register_or_update_compute_resource($computeDescription, true);
            //var_dump( $computeResource); exit;
            $tabName = "#tab-queues";
        } else if (Input::get("cr-edit") == "delete-queue") {
            CRUtilities::deleteQueue(Input::get("crId"), Input::get("queueName"));
            $tabName = "#tab-queues";
        } else if (Input::get("cr-edit") == "fileSystems") {
            $computeDescription = CRUtilities::get_compute_resource(Input::get("crId"));
            $computeDescription->fileSystems = array_filter(Input::get("fileSystems"), "trim");
            $computeResource = CRUtilities::register_or_update_compute_resource($computeDescription, true);

            $tabName = "#tab-filesystem";
        } else if (Input::get("cr-edit") == "jsp" || Input::get("cr-edit") == "edit-jsp") /* Add / Modify a Job Submission Interface */ {
            $update = false;
            if (Input::get("cr-edit") == "edit-jsp")
                $update = true;

            $jobSubmissionInterface = CRUtilities::create_or_update_JSIObject(Input::all(), $update);

            $tabName = "#tab-jobSubmission";
        } else if (Input::get("cr-edit") == "jsi-priority") {
            $inputs = Input::all();
            $computeDescription = CRUtilities::get_compute_resource(Input::get("crId"));
            foreach ($computeDescription->jobSubmissionInterfaces as $index => $jsi) {
                foreach ($inputs["jsi-id"] as $idIndex => $jsiId) {
                    if ($jsiId == $jsi->jobSubmissionInterfaceId) {
                        $computeDescription->jobSubmissionInterfaces[$index]->priorityOrder = $inputs["jsi-priority"][$idIndex];
                        break;
                    }
                }
            }
            $computeResource = CRUtilities::register_or_update_compute_resource($computeDescription, true);

            return 1; //currently done by ajax.
        } else if (Input::get("cr-edit") == "dmp" || Input::get("cr-edit") == "edit-dmi") /* Add / Modify a Data Movement Interface */ {
            $update = false;
            if (Input::get("cr-edit") == "edit-dmi")
                $update = true;
            $dataMovementInterface = CRUtilities::create_or_update_DMIObject(Input::all(), $update);

            $tabName = "#tab-dataMovement";
        } else if (Input::get("cr-edit") == "dmi-priority") {
            $inputs = Input::all();
            $computeDescription = CRUtilities::get_compute_resource(Input::get("crId"));
            foreach ($computeDescription->dataMovementInterfaces as $index => $dmi) {
                foreach ($inputs["dmi-id"] as $idIndex => $dmiId) {
                    if ($dmiId == $dmi->dataMovementInterfaceId) {
                        $computeDescription->dataMovementInterfaces[$index]->priorityOrder = $inputs["dmi-priority"][$idIndex];
                        break;
                    }
                }
            }
            $computeResource = CRUtilities::register_or_update_compute_resource($computeDescription, true);

            return 1; //currently done by ajax.
        } else if (Input::get("cr-edit") == "enableReporting") {
            $inputs = Input::all();
            $computeDescription = CRUtilities::get_compute_resource(Input::get("crId"));
            //var_dump( $computeDescription); exit;
            if( isset( $inputs["gatewayUsageReporting"]) && $inputs["gatewayUsageReporting"] == 1){
                $computeDescription->gatewayUsageReporting = true;
                $computeDescription->gatewayUsageModuleLoadCommand = $inputs["gatewayUsageModuleLoadCommand"];
                $computeDescription->gatewayUsageExecutable = $inputs["gatewayUsageExecutable"];

            }
            else{
                $computeDescription->gatewayUsageReporting = false;
                $computeDescription->gatewayUsageModuleLoadCommand = null;
                $computeDescription->gatewayUsageExecutable = null;
            }
            //var_dump( $computeDescription); exit;
            $computeResource = CRUtilities::register_or_update_compute_resource($computeDescription, true);
            $tabName = "#tab-reporting";
        }

        return Redirect::to("cr/edit?crId=" . Input::get("crId") . $tabName);
    }

    public function viewView()
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

    public function deleteActions()
    {
        $this->beforeFilter('verifyeditadmin');
        $result = CRUtilities::deleteActions(Input::all());
        if (Input::has("jsiId")) {
            return Redirect::to("cr/edit?crId=" . Input::get("crId") . "#tab-jobSubmission")
                ->with("message", "Job Submission Interface was deleted successfully");
        }
        if (Input::has("dmiId")) {
            return Redirect::to("cr/edit?crId=" . Input::get("crId") . "#tab-dataMovement")
                ->with("message", "Data Movement Protocol was deleted successfully");
        } elseif (Input::has("del-crId")) {
            return Redirect::to("cr/browse")->with("message", "The Compute Resource has been successfully deleted.");
        } else
            return $result;
    }

    public function browseView()
    {
        $data = CRUtilities::getBrowseCRData(false);
        $allCRs = $data["crObjects"];
        $appDeployments = $data["appDeployments"];

        $connectedDeployments = array();
        foreach ((array)$allCRs as $resource) {
            $crId = $resource->computeResourceId;
            $connectedDeployments[$crId] = 0;
            foreach ((array)$appDeployments as $deploymentObject) {
                if ($crId == $deploymentObject->computeHostId)
                    $connectedDeployments[$crId]++;
            }
        }
        Session::put("admin-nav", "cr-browse");
        return View::make("resource/browse", array(
            "allCRs" => $allCRs,
            "connectedDeployments" => $connectedDeployments
        ));

    }

    public function groupedApplicationsView(){
        return CRUtilities::getApplicationsByResource();
    }
}

?>