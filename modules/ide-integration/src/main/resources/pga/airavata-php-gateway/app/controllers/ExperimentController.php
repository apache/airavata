<?php

use Airavata\API\Error\AuthorizationException;
use Airavata\API\Error\ExperimentNotFoundException;
use Airavata\Model\Status\JobState;
use Airavata\Model\Group\ResourceType;

class ExperimentController extends BaseController
{

    /**
     * Limit used in fetching paginated results
     * @var int
     */
    var $limit = 20;

    /**
     *    Instantiate a new ExperimentController Instance
     **/

    public function __construct()
    {
        $this->beforeFilter('verifylogin');
        $this->beforeFilter('verifyauthorizeduser');
        Session::put("nav-active", "experiment");
    }

    public function createView()
    {
        Session::forget('exp_create_app_id');
        Session::forget('exp_create_continue');
        return View::make('experiment/create');
    }

    public function createSubmit()
    {
        if (isset($_POST['continue'])) {
            Session::put('exp_create_continue', true);
            $appInterfaces = AppUtilities::get_all_applications();
            foreach($appInterfaces as $id=>$name){
                if($id == $_POST['application']){
                    Session::put('exp_create_app_id', AppUtilities::get_application_interface($id)->applicationModules[0]);
                }
            }

            $computeResources = CRUtilities::create_compute_resources_select($_POST['application'], null);

            $nodeCount = Config::get('pga_config.airavata')["node-count"];
            $cpuCount = Config::get('pga_config.airavata')["total-cpu-count"];
            $wallTimeLimit = Config::get('pga_config.airavata')["wall-time-limit"];

            $queueDefaults = array("queueName" => Config::get('pga_config.airavata')["queue-name"],
                "nodeCount" => $nodeCount,
                "cpuCount" => $cpuCount,
                "wallTimeLimit" => $wallTimeLimit
            );

            $experimentInputs = array(
                "disabled" => ' disabled',
                "experimentName" => $_POST['experiment-name'],
                "experimentDescription" => $_POST['experiment-description'] . ' ',
                "project" => $_POST['project'],
                "application" => $_POST['application'],
                "echo" => ($_POST['application'] == 'Echo') ? ' selected' : '',
                "wrf" => ($_POST['application'] == 'WRF') ? ' selected' : '',
                "queueDefaults" => $queueDefaults,
                "advancedOptions" => Config::get('pga_config.airavata')["advanced-experiment-options"],
                "computeResources" => $computeResources,
                "resourceHostId" => null,
                "advancedOptions" => Config::get('pga_config.airavata')["advanced-experiment-options"],
                "allowedFileSize" => $this->getAllowedFileSize()
            );

            if(Config::get('pga_config.airavata')["data-sharing-enabled"]){
                $users = SharingUtilities::getProfilesForSharedUsers($_POST['project'], ResourceType::PROJECT);
                $owner = array();

                $projectOwner = array();
                $sharedProjectOwner = SharingUtilities::getSharedResourceOwner($_POST['project'], ResourceType::PROJECT);
                if (Session::get("username") !== $sharedProjectOwner) {
                    $projectOwner[$sharedProjectOwner] = $users[$sharedProjectOwner];
                    $users = array_diff_key($users, $projectOwner);
                }

                return View::make("experiment/create-complete", array("expInputs" => $experimentInputs,
                    "users" => json_encode($users), "owner" => json_encode($owner),
                    "canEditSharing" => true,
                    "projectOwner" => json_encode($projectOwner),
                    "updateSharingViaAjax" => false));
            }else{
                return View::make("experiment/no-sharing-create-complete", array("expInputs" => $experimentInputs));
            }

        } else if (isset($_POST['save']) || isset($_POST['launch'])) {
            try {
                $computeResourceId = Input::get("compute-resource");
                //Validate entered queue details
                $queueValues = array("queueName" => Input::get("queue-name"),
                    "nodeCount" => Input::get("node-count"),
                    "cpuCount" => Input::get("cpu-count"),
                    "wallTimeLimit" => Input::get("walltime-count")
                );
                if($this->validateQueueData($computeResourceId, $queueValues))
                    $expId = ExperimentUtilities::create_experiment();
                else
                    return Redirect::to("experiment/create")->with("error-message", "Validate the number of nodes, CPUs and the wall time limit");
            } catch (Exception $ex) {
                Log::error("Failed to create experiment!");
                Log::error($ex);
                return Redirect::to("experiment/create")->with("error-message", "Failed to create experiment: " . $ex->getMessage());
            }

            if (isset($_POST['launch']) && $expId) {
                ExperimentUtilities::launch_experiment($expId);
            }
            /* Not required.
            else
            {
                CommonUtilities::print_success_message("<p>Experiment {$_POST['experiment-name']} created!</p>" .
                    '<p>You will be redirected to the summary page shortly, or you can
                    <a href=' . URL::to('/') . '"/experiment/summary?expId=' . $expId . '">go directly</a> to experiment summary page.</p>');

            }*/
            return Redirect::to('experiment/summary?expId=' . urlencode($expId));
        } else
            return Redirect::to("home")->with("message", "Something went wrong here. Please file a bug report using the link in the Help menu.");
    }

    public function summary()
    {
        $experiment = null;
        $experiment_id = null;
        // set the experiment Id using the jobId
        if(isset($_GET['jobId'])){
            Log::debug("Searching experiment summary using jobId");
            $limit = 1;
            $pageNo = 0;
            $inputs["search-key"] = "jobId";
            $inputs["search-value"] = $_GET['jobId'];
            try {
                $expContainer = ExperimentUtilities::get_expsearch_results_with_pagination($inputs, $limit,
                    $pageNo);
            } catch (AuthorizationException $ae) {

                Log::error("User isn't authorized to see experiment", array("message" => $ae->getMessage(), "username" => Session::get("username"), "gateway_id" => Session::get("gateway_id")));
                return $this->makeInvalidExperimentView();
            } catch (ExperimentNotFoundException $enf) {

                Log::error("Experiment wasn't found", array("message" => $enf->getMessage(), "username" => Session::get("username"), "gateway_id" => Session::get("gateway_id")));
                return $this->makeInvalidExperimentView();
            }
            Log::debug(print_r($inputs, true));
            try{
              $experiment_id=$expContainer[0]['experiment']->experimentId;
            } catch (Exception $e) {
              Log::error("No experiment found for the given jobId");
              return $this->makeInvalidExperimentView();
            }
        }
        // set the experiment Id directly using the input from the screen
        else {
            $experiment_id = $_GET['expId'];
        }

        try {
            $experiment = ExperimentUtilities::get_experiment($experiment_id);

        } catch (ExperimentNotFoundException $enf) {

            Log::error("Experiment wasn't found", array("message" => $enf->getMessage(), "username" => Session::get("username"), "gateway_id" => Session::get("gateway_id")));
            return $this->makeInvalidExperimentView();
        } catch (AuthorizationException $ae) {

            Log::error("User isn't authorized to see experiment", array("message" => $ae->getMessage(), "username" => Session::get("username"), "gateway_id" => Session::get("gateway_id")));
            return $this->makeInvalidExperimentView();
        }
        // Assume that experiment is not null now

        if(isset($_GET['isAutoRefresh']) && $_GET['isAutoRefresh'] == 'true'){
            $autoRefresh = true;
        }else{
            $autoRefresh = false;
        }
        //viewing experiments of other gateways is not allowed if user is not super admin
        if( $experiment->gatewayId != Session::get("gateway_id") && !Session::has("super-admin")){
            Session::put("permissionDenied", true);
            CommonUtilities::print_error_message('It seems that you do not have permissions to view this experiment or it belongs to another gateway.');
            if (Input::has("dashboard"))
                return View::make("partials/experiment-info", array("invalidExperimentId" => 1, "users" => json_encode(array())));
            else
                return View::make("experiment/summary", array("invalidExperimentId" => 1, "users" => json_encode(array())));
        }
        else
            Session::forget("permissionDenied");


        $project = null;
        if(Config::get('pga_config.airavata')["data-sharing-enabled"]){
            if (SharingUtilities::userCanRead(Session::get("username"), $experiment->projectId, ResourceType::PROJECT)) {
                $project = ProjectUtilities::get_project($experiment->projectId);
            }
        } elseif ($experiment->userName == Session::get("username")){
            // When sharing is disabled the backend checks the auth token claims map
            // to make sure the authenticating user is the same as the project
            // owner. So the project can only be loaded when the user is the
            // project owner, which can be inferred from the experiment's owner.
            $project = ProjectUtilities::get_project($experiment->projectId);
        }
        $expVal = ExperimentUtilities::get_experiment_values($experiment);
        $jobDetails = ExperimentUtilities::get_job_details($experiment->experimentId);
//            var_dump( $jobDetails); exit;
        foreach( $jobDetails as $index => $jobDetail){
            if(isset($jobDetail->jobStatuses)){
                  $jobDetails[ $index]->jobStatuses[0]->jobStateName = JobState::$__names[$jobDetail->jobStatuses[0]->jobState];
            }
            else{
                $jobDetails[ $index]->jobStatuses = [new stdClass()];
                $jobDetails[ $index]->jobStatuses[0]->jobStateName = null;
            }
        }
        $expVal["jobDetails"] = $jobDetails;

        $writeableProjects = ProjectUtilities::get_all_user_writeable_projects(Session::get("gateway_id"), Session::get("username"));

        $data = array(
            "expId" => $experiment_id,
            "experiment" => $experiment,
            "project" => $project,
            "jobDetails" => $jobDetails,
            "expVal" => $expVal,
            "autoRefresh"=> $autoRefresh,
            "writeableProjects" => $writeableProjects
        );
        if(Config::get('pga_config.airavata')["data-sharing-enabled"]){
            $users = SharingUtilities::getProfilesForSharedUsers($experiment_id, ResourceType::EXPERIMENT);
            $sharedExperimentOwner = SharingUtilities::getSharedResourceOwner($experiment->experimentId, ResourceType::EXPERIMENT);
            $sharedProjectOwner = SharingUtilities::getSharedResourceOwner($experiment->projectId, ResourceType::PROJECT);

            $owner = array();
            $projectOwner = array();
            if (Session::get("username") !== $sharedExperimentOwner) {
                $owner[$sharedExperimentOwner] = $users[$sharedExperimentOwner];
            }
            if (Session::get("username") !== $sharedProjectOwner) {
                $projectOwner[$sharedProjectOwner] = $users[$sharedProjectOwner];
            }
            // Subtract out the owner and project owner from list of users
            $users = array_diff_key($users, $owner);
            $users = array_diff_key($users, $projectOwner);
            // If project owner is the same as owner, just show the owner, not the project owner
            $projectOwner = array_diff_key($projectOwner, $owner);
            // Only allow editing sharing on the summary page if the owner
            // and the experiment isn't editable. If the experiment is
            // editable, the sharing can be edited on the edit page.
            $canEditSharing = (Session::get("username") === $sharedExperimentOwner) && !$expVal["editable"];
            $data['can_write'] = SharingUtilities::userCanWrite(Session::get("username"), $experiment->experimentId, ResourceType::EXPERIMENT);
            $data["users"] = json_encode($users);
            $data["owner"] = json_encode($owner);
            $data["projectOwner"] = json_encode($projectOwner);
            $data["canEditSharing"] = $canEditSharing;
            // The summary page has it's own Update Sharing button
            $data["updateSharingViaAjax"] = true;
        }

        if( Input::has("dashboard"))
        {
            $detailedExperiment = ExperimentUtilities::get_detailed_experiment( $experiment_id);
            $data["detailedExperiment"] = $detailedExperiment;
        }

        if (Request::ajax()) {
            //admin wants to see an experiment summary
            if (Input::has("dashboard")) {
                $data["dashboard"] = true;
                return View::make("partials/experiment-info", $data);
            } else
                return json_encode($data);
        } else {
            return View::make("experiment/summary", $data);
        }
    }

    private function makeInvalidExperimentView() {

        if (Input::has("dashboard"))
            return View::make("partials/experiment-info", array("invalidExperimentId" => 1));
        else
            return View::make("experiment/summary", array("invalidExperimentId" => 1));
    }

    public function expChange()
    {
        //var_dump( Input::all() ); exit;
        $experiment = ExperimentUtilities::get_experiment(Input::get('expId'));
        $expVal = ExperimentUtilities::get_experiment_values($experiment);
        $expVal["jobState"] = ExperimentUtilities::get_job_status($experiment);
        /*if (isset($_POST['save']))
        {
            $updatedExperiment = CommonUtilities::apply_changes_to_experiment($experiment);

            CommonUtilities::update_experiment($experiment->experimentId, $updatedExperiment);
        }*/
        if (isset($_POST['launch'])) {
            ExperimentUtilities::launch_experiment($experiment->experimentId);
            return Redirect::to('experiment/summary?expId=' . urlencode($experiment->experimentId));
        } elseif (isset($_POST['cancel'])) {
            ExperimentUtilities::cancel_experiment($experiment->experimentId);
            return Redirect::to('experiment/summary?expId=' . urlencode($experiment->experimentId));
        } elseif (isset($_POST['update-sharing'])) {
            if(Config::get('pga_config.airavata')["data-sharing-enabled"]){
                $share = $_POST['share-settings'];
                ExperimentUtilities::update_experiment_sharing($experiment->experimentId, json_decode($share));
            }
            return Redirect::to('experiment/summary?expId=' . urlencode($experiment->experimentId));
        }
    }

    public function editView()
    {
        $experiment = ExperimentUtilities::get_experiment($_GET['expId']);
        $expVal = ExperimentUtilities::get_experiment_values($experiment);
        $expVal["jobState"] = ExperimentUtilities::get_job_status($experiment);


        $appInterfaces = AppUtilities::get_all_applications();
        foreach($appInterfaces as $id=>$name) {
            if ($id == $experiment->executionId) {
                $appId = AppUtilities::get_application_interface($id)->applicationModules[0];
            }
        }

        $nodeCount = Config::get('pga_config.airavata')["node-count"];
        $cpuCount = Config::get('pga_config.airavata')["total-cpu-count"];
        $wallTimeLimit = Config::get('pga_config.airavata')["wall-time-limit"];
        $cpusPerNode = 0;

        $queueDefaults = array("queueName" => Config::get('pga_config.airavata')["queue-name"],
            "nodeCount" => $nodeCount,
            "cpuCount" => $cpuCount,
            "wallTimeLimit" => $wallTimeLimit,
            "cpusPerNode" => $cpusPerNode
        );

        $computeResourceId = $experiment->userConfigurationData->computationalResourceScheduling->resourceHostId;
        $appDeployments = Airavata::getAllApplicationDeployments(Session::get('authz-token'), Session::get("gateway_id"));
        $correctAppDeployment = null;
        foreach($appDeployments as $appDeployment){
            if($appDeployment->computeHostId == $computeResourceId && $appDeployment->appModuleId == $appId){
                $correctAppDeployment = $appDeployment;
                break;
            }
        }

        $appDeploymentDefaults = array();
        if($correctAppDeployment != null){
            $appDeploymentDefaults['nodeCount'] = $correctAppDeployment->defaultNodeCount;
            $appDeploymentDefaults['cpuCount'] = $correctAppDeployment->defaultCPUCount;
            $appDeploymentDefaults['wallTimeLimit'] = $wallTimeLimit;
            $appDeploymentDefaults['queueName'] = $correctAppDeployment->defaultQueueName;
        }

        $computeResources = CRUtilities::create_compute_resources_select($experiment->executionId, $expVal['scheduling']->resourceHostId);

        $userComputeResourcePreferences = URPUtilities::get_all_validated_user_compute_resource_prefs();
        $userHasComputeResourcePreference = array_key_exists($expVal['scheduling']->resourceHostId, $userComputeResourcePreferences);
        $batchQueues = ExperimentUtilities::getQueueDatafromResourceId($computeResourceId);

        $experimentInputs = array(
            "disabled" => ' ',
            "experimentName" => $experiment->experimentName,
            "experimentDescription" => $experiment->description,
            "application" => $experiment->executionId,
            "autoSchedule" => $experiment->userConfigurationData->airavataAutoSchedule,
            "userDN" => $experiment->userConfigurationData->userDN,
            "userHasComputeResourcePreference" => $userHasComputeResourcePreference,
            "useUserCRPref" => $experiment->userConfigurationData->useUserCRPref,
            "allowedFileSize" => $this->getAllowedFileSize(),
            'experiment' => $experiment,
            "queueDefaults" => $queueDefaults,
            'computeResources' => $computeResources,
            "resourceHostId" => $expVal['scheduling']->resourceHostId,
            'project' => $experiment->projectId,
            'expVal' => $expVal,
            'cloning' => true,
            'advancedOptions' => Config::get('pga_config.airavata')["advanced-experiment-options"],
            'batchQueues' => $batchQueues
        );

        if(Config::get('pga_config.airavata')["data-sharing-enabled"]){
            if (SharingUtilities::userCanWrite(Session::get("username"), $_GET['expId'], ResourceType::EXPERIMENT) === true) {
                $users = SharingUtilities::getProfilesForSharedUsers($_GET['expId'], ResourceType::EXPERIMENT);
                $sharedExperimentOwner = SharingUtilities::getSharedResourceOwner($experiment->experimentId, ResourceType::EXPERIMENT);
                $sharedProjectOwner = SharingUtilities::getSharedResourceOwner($experiment->projectId, ResourceType::PROJECT);

                $owner = array();
                $projectOwner = array();
                if (Session::get("username") !==  $sharedExperimentOwner) {
                    $owner[$sharedExperimentOwner] = $users[$sharedExperimentOwner];
                }
                if (Session::get("username") !== $sharedProjectOwner) {
                    $projectOwner[$sharedProjectOwner] = $users[$sharedProjectOwner];
                }
                // Subtract out owner and project owner from list of users
                $users = array_diff_key($users, $owner);
                $users = array_diff_key($users, $projectOwner);
                // If project owner is the same as owner, just show the owner, not the project owner
                $projectOwner = array_diff_key($projectOwner, $owner);

                $canEditSharing = Session::get("username") === $sharedExperimentOwner;

                return View::make("experiment/edit", array("expInputs" => $experimentInputs,
                    "users" => json_encode($users), "owner" => json_encode($owner),
                    "canEditSharing" => $canEditSharing,
                    "projectOwner" => json_encode($projectOwner),
                    "updateSharingViaAjax" => false,
                    "cpusPerNode" => $cpusPerNode,
                    "appDeploymentDefaults" => $appDeploymentDefaults,
                    "queueDefaults" => $queueDefaults
                ));
            }
            else {
                Redirect::to("experiment/summary?expId=" . urlencode($experiment->experimentId))->with("error", "You do not have permission to edit this experiment");
            }
        }else {
            return View::make("experiment/no-sharing-edit", array("expInputs" => $experimentInputs));
        }
    }

    public function cloneExperiment()
    {
        try{
            $cloneId = ExperimentUtilities::clone_experiment(Input::get('expId'), Input::get('projectId'));
            return Redirect::to('experiment/edit?expId=' . urlencode($cloneId));
        }catch (Exception $ex){
            return Redirect::to("experiment/summary?expId=" . urlencode(Input::get('expId')))
                ->with("cloning-error", "Failed to clone experiment: " . $ex->getMessage());
        }
    }

    public function editSubmit()
    {
        $experiment = ExperimentUtilities::get_experiment(Input::get('expId')); // update local experiment variable
        try {
            $computeResourceId = Input::get("compute-resource");
            //Validate entered queue details
            $queueValues = array("queueName" => Input::get("queue-name"),
                "nodeCount" => Input::get("node-count"),
                "cpuCount" => Input::get("cpu-count"),
                "wallTimeLimit" => Input::get("walltime-count")
            );
            if($this->validateQueueData($computeResourceId, $queueValues)) {
                $updatedExperiment = ExperimentUtilities::apply_changes_to_experiment($experiment, Input::all());
            } else {
                $errMessage = "Validate the number of nodes, CPUs and the wall time limit";
                return Redirect::to("experiment/edit?expId=" . urlencode(Input::get('expId')))->with("error-message", $errMessage);
            }
        } catch (Exception $ex) {
            $errMessage = "Failed to update experiment: " . $ex->getMessage();
            Log::error($errMessage);
            Log::error($ex);
            return Redirect::to("experiment/edit?expId=" . urlencode(Input::get('expId')))->with("error-message", $errMessage);
        }

        if(Config::get('pga_config.airavata')["data-sharing-enabled"]){
            if (SharingUtilities::userCanWrite(Session::get("username"), Input::get('expId'), ResourceType::EXPERIMENT)) {
                if (isset($_POST['save']) || isset($_POST['launch'])) {

                    ExperimentUtilities::update_experiment($experiment->experimentId, $updatedExperiment);

                    if (isset($_POST['save'])) {
                        $experiment = ExperimentUtilities::get_experiment(Input::get('expId')); // update local experiment variable
                    }
                    if (isset($_POST['launch'])) {
                        ExperimentUtilities::launch_experiment($experiment->experimentId);
                    }

                    return Redirect::to('experiment/summary?expId=' . urlencode($experiment->experimentId));
                } else
                    return View::make("home");
            }
        }else{
            if (isset($_POST['save']) || isset($_POST['launch'])) {

                ExperimentUtilities::update_experiment($experiment->experimentId, $updatedExperiment);

                if (isset($_POST['save'])) {
                    $experiment = ExperimentUtilities::get_experiment(Input::get('expId')); // update local experiment variable
                }
                if (isset($_POST['launch'])) {
                    ExperimentUtilities::launch_experiment($experiment->experimentId);
                }

                return Redirect::to('experiment/summary?expId=' . urlencode($experiment->experimentId));
            } else
                return View::make("home");
        }
    }

    public function getQueueView()
    {
        $appId = Session::get('exp_create_app_id');
        $computeResourceId = Input::get("crId");
        $appDeployments = Airavata::getAllApplicationDeployments(Session::get('authz-token'), Session::get("gateway_id"));

        $nodeCount = Config::get('pga_config.airavata')["node-count"];
        $cpuCount = Config::get('pga_config.airavata')["total-cpu-count"];
        $wallTimeLimit = Config::get('pga_config.airavata')["wall-time-limit"];
        $cpusPerNode = 0;
        $queueName = Config::get('pga_config.airavata')["queue-name"];

        $queueDefaults = array("queueName" => $queueName,
            "nodeCount" => $nodeCount,
            "cpuCount" => $cpuCount,
            "wallTimeLimit" => $wallTimeLimit,
            "cpusPerNode" => $cpusPerNode
        );

        $queues = ExperimentUtilities::getQueueDatafromResourceId($computeResourceId);
        $userComputeResourcePreferences = URPUtilities::get_all_validated_user_compute_resource_prefs();
        $userHasComputeResourcePreference = array_key_exists($computeResourceId, $userComputeResourcePreferences);
        if ($userHasComputeResourcePreference)
        {
            $queueDefaults['queueName'] = $userComputeResourcePreferences[$computeResourceId]->preferredBatchQueue;
        }else{
            foreach($queues as $aQueue){
                if($aQueue->isDefaultQueue){
                    $queueDefaults['queueName'] = $aQueue->queueName;
                    break;
                }
            }
        }

        $correctAppDeployment = null;
        foreach($appDeployments as $appDeployment){
            if($appDeployment->computeHostId == $computeResourceId && $appDeployment->appModuleId == $appId){
                $correctAppDeployment = $appDeployment;
                break;
            }
        }

        $appDeploymentDefaults = array();
        if($correctAppDeployment != null){
            $appDeploymentDefaults['nodeCount'] = $correctAppDeployment->defaultNodeCount;
            $appDeploymentDefaults['cpuCount'] = $correctAppDeployment->defaultCPUCount;
            $appDeploymentDefaults['queueName'] = $correctAppDeployment->defaultQueueName;
        }

        return View::make("partials/experiment-queue-block", array("queues" => $queues,
            "queueDefaults" => $queueDefaults,
            "appDeploymentDefaults" => $appDeploymentDefaults,
            "useUserCRPref" => $userHasComputeResourcePreference,
            "userHasComputeResourcePreference" => $userHasComputeResourcePreference,
            "cpusPerNode" => $cpusPerNode));
    }

    public function browseView()
    {
        $pageNo = Input::get('pageNo');
        $prev = Input::get('prev');
        $isSearch = Input::get('search');
        if (empty($pageNo) || isset($isSearch) ) {
            $pageNo = 1;
        } else {
            if (isset($prev)) {
                $pageNo -= 1;
            } else {
                $pageNo += 1;
            }
        }

        $expContainer = ExperimentUtilities::get_expsearch_results_with_pagination(Input::all(), $this->limit,
            ($pageNo - 1) * $this->limit);
        $experimentStates = ExperimentUtilities::getExpStates();

        $can_write = array();
        foreach ($expContainer as $experiment) {
            if(Config::get('pga_config.airavata')["data-sharing-enabled"]){
                $can_write[$experiment['experiment']->experimentId] = SharingUtilities::userCanWrite(Session::get("username"),
                    $experiment['experiment']->experimentId, ResourceType::EXPERIMENT);
            } else {
                $can_write[$experiment['experiment']->experimentId] = true;
            }
        }

        return View::make('experiment/browse', array(
            'input' => Input::all(),
            'pageNo' => $pageNo,
            'limit' => $this->limit,
            'expStates' => $experimentStates,
            'expContainer' => $expContainer,
            'can_write' => $can_write
        ));
    }

    /**
     * Generate JSON containing permissions information for this project.
     *
     * This function retrieves the user profile and permissions for every user
     * other than the client that has access to the project. In the event that
     * the project does not exist, return an error message.
     */
    public function sharedUsers()
    {
        if (Session::has("authz-token") && array_key_exists('resourceId', $_GET)) {
            return Response::json(SharingUtilities::getProfilesForSharedUsers($_GET['resourceId'], ResourceType::EXPERIMENT));
        }
        else {
            return Response::json(array("error" => "Error: No project specified"));
        }
    }

    public function unsharedUsers()
    {
        if (Session::has("authz-token") && array_key_exists('resourceId', $_GET)) {
            return Response::json(SharingUtilities::getProfilesForUnsharedUsers($_GET['resourceId'], ResourceType::EXPERIMENT));
        }
        else {
            return Response::json(array("error" => "Error: No experiment specified"));
        }
    }

    public function updateSharing()
    {
        try{
            // Convert the JSON array to an object
            $sharing_info = json_decode(json_encode(Input::json()->all()));
            ExperimentUtilities::update_experiment_sharing(Input::get('expId'), $sharing_info);
            return Response::json(array("success" => true));
        }catch (Exception $ex){
            Log::error("failed to update sharing for experiment", array(Input::all()));
            Log::error($ex);
            return Response::json(array("success" => false, "error" => "Error: failed to update sharing: " . $ex->getMessage()));
        }
    }

    private function getAllowedFileSize()
    {
        // Condition added to deal with php ini default value set for post_max_size issue.
        // NOTE: the following assumes that upload_max_filesize and
        // post_max_size are in megabytes (for example, if
        // upload_max_filesize is 8M then $allowedFileSize is 8, but the 'M'
        // is assumed and not considered)
        $allowedFileSize = intval( ini_get( 'upload_max_filesize' ) );
        $serverLimit = intval( ini_get( 'post_max_size' ) );
        if( $serverLimit < $allowedFileSize) {
            $allowedFileSize = $serverLimit;
        }
        return $allowedFileSize;
    }

    private function validateQueueData($computeResourceId, $queue)
    {
        $queues = ExperimentUtilities::getQueueDatafromResourceId($computeResourceId);
        $queueName = $queue['queueName'];

        foreach($queues as $aQueue){
            if($aQueue->queueName == $queueName) {
                if($queue['nodeCount'] <= $aQueue->maxNodes && $queue['cpuCount'] <= $aQueue->maxProcessors && $queue['wallTimeLimit'] <= $aQueue->maxRunTime)
                    return true;
            }
        }

        return false;
    }
}

?>
