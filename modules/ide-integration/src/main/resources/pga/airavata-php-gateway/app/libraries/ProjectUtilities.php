<?php

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\InvalidRequestException;
use Airavata\Facades\Airavata;
use Airavata\Model\Workspace\Project;
use Airavata\Model\Group\ResourceType;
use Airavata\Model\Group\ResourcePermissionType;

class ProjectUtilities
{

    /**
     * Get all projects owned by the given user
     * @param $username
     * @return null
     */
    public static function get_all_user_projects($gatewayId, $username)
    {
        $userProjects = null;

        try {
            $userProjects = Airavata::getUserProjects(Session::get('authz-token'), $gatewayId, $username, -1, 0);

            // Add a optionLabel that disambiguates shared projects
            foreach ($userProjects as $project) {

                $optionLabel = $project->name;
                if ($project->owner != Session::get('username')) {
                    $optionLabel = $optionLabel . ' (owned by ' . $project->owner . ')';
                }
                $project->optionLabel = $optionLabel;
            }

            //var_dump( $userProjects); exit;
        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('<p>There was a problem getting the user\'s projects.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>InvalidRequestException: ' . $ire->getMessage() . '</p>');
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('<p>There was a problem getting the user\'s projects.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>Airavata Client Exception: ' . $ace->getMessage() . '</p>');
        } catch (AiravataSystemException $ase) {
            if ($ase->airavataErrorType == 2) // 2 = INTERNAL_ERROR
            {
                CommonUtilities::print_warning_message('<p>You must create a project before you can create an experiment.
                Click <a href="' . URL::to('/') . '/project/create">here</a> to create a project.</p>');
            } else {
                CommonUtilities::print_error_message('<p>There was a problem getting the user\'s projects.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                    '<p>AiravataSystemException: ' . $ase->getMessage() . '</p>');
            }
        }

        return $userProjects;
    }

    public static function get_all_user_writeable_projects($gatewayId, $username)
    {
        $writeableProjects = array();
        $userProjects = ProjectUtilities::get_all_user_projects($gatewayId, $username);
        if (Config::get('pga_config.airavata')["data-sharing-enabled"]){
            foreach($userProjects as $project) {
                if (SharingUtilities::userCanWrite($username, $project->projectID, ResourceType::PROJECT)) {
                    $writeableProjects[] = $project;
                }
            }
        } else {
            $writeableProjects = $userProjects;
        }

        return $writeableProjects;
    }

    /**
     * Get the project with the given ID
     * @param $projectId
     * @return null
     */
    public static function get_project($projectId)
    {

        try {
            return Airavata::getProject(Session::get('authz-token'), $projectId);
        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('<p>There was a problem getting the project.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>InvalidRequestException: ' . $ire->getMessage() . '</p>');
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('<p>There was a problem getting the project.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>AiravataClientException: ' . $ace->getMessage() . '</p>');
        } catch (AiravataSystemException $ase) {
            CommonUtilities::print_error_message('<p>There was a problem getting the project.
            Please try again later or submit a bug report using the link in the Help menu.</p>' .
                '<p>AiravataSystemException!<br><br>' . $ase->getMessage() . '</p>');
        }

    }

    /**
     * Create a select input and populate it with project options from the database
     */
    public static function create_project_select($projectId = null, $editable = true)
    {
        $editable ? $disabled = '' : $disabled = 'disabled';
        if ($editable) {
            // this should only return projects the user can write
            $userProjects = ProjectUtilities::get_all_user_writeable_projects(Session::get("gateway_id"), Session::get('username'));
        } else {
            $userProjects = ProjectUtilities::get_all_user_projects(Session::get("gateway_id"), Session::get('username'));
        }

        echo '<select class="form-control" name="project" id="project" required ' . $disabled . '>';
        if (sizeof($userProjects) > 0) {
            foreach ($userProjects as $project) {
                if ($project->projectID == $projectId) {
                    $selected = 'selected';
                } else {
                    $selected = '';
                }

                echo '<option value="' . htmlspecialchars($project->projectID) . '" ' . $selected . '>' . htmlspecialchars($project->optionLabel) . '</option>';
            }
        }
        echo '</select>';
    }

    //moved from create project view.

    public static function create_project()
    {
        $project = new Project();
        $project->owner = Session::get('username');
        $project->name = $_POST['project-name'];
        $project->description = $_POST['project-description'];
        $project->gatewayId = Config::get('pga_config.airavata')['gateway-id'];

        $projectId = null;

        try {
            $projectId = Airavata::createProject(Session::get('authz-token'), Config::get('pga_config.airavata')['gateway-id'], $project);

            if ($projectId) {
                CommonUtilities::print_success_message("<p>Project " . htmlspecialchars($_POST['project-name']) . " created!</p>" .
                    '<p>You will be redirected to the summary page shortly, or you can
                    <a href="project/summary?projId=' . urlencode($projectId) . '">go directly</a> to the project summary page.</p>');
            } else {
                CommonUtilities::print_error_message("Error creating project ". htmlspecialchars($_POST['project-name']) . "!");
            }
        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('InvalidRequestException!<br><br>' . $ire->getMessage());
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('AiravataClientException!<br><br>' . $ace->getMessage());
        } catch (AiravataSystemException $ase) {
            CommonUtilities::print_error_message('AiravataSystemException!<br><br>' . $ase->getMessage());
        }

        if (Config::get('pga_config.airavata')["data-sharing-enabled"]){
            $share = $_POST['share-settings'];
            ProjectUtilities::share_project($projectId, json_decode($share));
        }

        return $projectId;
    }

    public static function create_default_project($username)
    {
        $project = new Project();
        $project->owner = $username;
        $project->name = "Default Project";
        $project->gatewayId = Config::get('pga_config.airavata')['gateway-id'];
        $project->description = "This is the default project for user " . $project->owner;


        $projectId = null;

        try {
            $projectId = Airavata::createProject(Session::get('authz-token'), Config::get('pga_config.airavata')['gateway-id'], $project);

            if (Config::get('pga_config.airavata')["data-sharing-enabled"]){
                $share = new stdClass();
                $share->{$username} = new stdClass();
                $share->{$username}->read = true;
                $share->{$username}->write = true;
                ProjectUtilities::share_project($projectId, $share);
            }

        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('InvalidRequestException!<br><br>' . $ire->getMessage());
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('AiravataClientException!<br><br>' . $ace->getMessage());
        } catch (AiravataSystemException $ase) {
            CommonUtilities::print_error_message('AiravataSystemException!<br><br>' . $ase->getMessage());
        }

        return $projectId;
    }


    /**
     * Get experiments in project
     * @param $projectId
     * @return array|null
     */
    public static function get_experiments_in_project($projectId)
    {

        $experiments = array();

        try {
            $experiments = Airavata::getExperimentsInProject(Session::get('authz-token'), $projectId, -1, 0);
        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('InvalidRequestException!<br><br>' . $ire->getMessage());
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('AiravataClientException!<br><br>' . $ace->getMessage());
        } catch (AiravataSystemException $ase) {
            CommonUtilities::print_error_message('AiravataSystemException!<br><br>' . $ase->getMessage());
        } catch (TTransportException $tte) {
            CommonUtilities::print_error_message('TTransportException!<br><br>' . $tte->getMessage());
        }

        for($i = 0; $i < count($experiments); $i++) {
            if (!SharingUtilities::userCanRead(Session::get("username"), $experiments[$i]->experimentId, ResourceType::EXPERIMENT)) {
                array_splice($experiments, $i, 1);
            }
        }

        return $experiments;
    }

    public static function update_project($projectId, $projectDetails)
    {
        $updatedProject = new Project();
        $updatedProject->owner = $projectDetails->owner;
        $updatedProject->name = $projectDetails->name;
        $updatedProject->description = $projectDetails->description;
        $updatedProject->gatewayId = Config::get('pga_config.airavata')['gateway-id'];

        Airavata::updateProject(Session::get('authz-token'), $projectId, $updatedProject);

        if (Config::get('pga_config.airavata')["data-sharing-enabled"] && array_key_exists('share-settings', $_POST)){
            $share = $_POST['share-settings'];
            ProjectUtilities::share_project($projectId, json_decode($share));
        }
    }


    public static function get_all_user_accessible_projects_with_pagination($limit, $offset)
    {

        $projects = array();

        try {
            $projects = $projects = Airavata::searchProjects(Session::get('authz-token'), Session::get("gateway_id"),
                Session::get("username"), [], $limit, $offset);
        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('InvalidRequestException!<br><br>' . $ire->getMessage());
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('AiravataClientException!<br><br>' . $ace->getMessage());
        } catch (AiravataSystemException $ase) {
            if ($ase->airavataErrorType == 2) // 2 = INTERNAL_ERROR
            {
                CommonUtilities::print_info_message('<p>You have not created any projects yet, so no results will be returned!</p>
                                <p>Click <a href="create_project.php">here</a> to create a new project.</p>');
            } else {
                CommonUtilities::print_error_message('There was a problem with Airavata. Please try again later, or report a bug using the link in the Help menu.');
                //print_error_message('AiravataSystemException!<br><br>' . $ase->airavataErrorType . ': ' . $ase->getMessage());
            }
        } catch (TTransportException $tte) {
            CommonUtilities::print_error_message('TTransportException!<br><br>' . $tte->getMessage());
        }

        return $projects;
    }


    public static function get_proj_search_results_with_pagination($searchKey, $searchValue, $limit, $offset)
    {

        $projects = array();

        try {
            switch ($searchKey) {
                case 'project-name':
                    $filters[\Airavata\Model\Experiment\ProjectSearchFields::PROJECT_NAME] = $searchValue;
                    $projects = Airavata::searchProjects(Session::get('authz-token'), Session::get("gateway_id"),
                        Session::get("username"), $filters, $limit, $offset);
                    break;
                case 'project-description':
                    $filters[\Airavata\Model\Experiment\ProjectSearchFields::PROJECT_DESCRIPTION] = $searchValue;
                    $projects = Airavata::searchProjects(Session::get('authz-token'), Session::get("gateway_id"),
                        Session::get("username"), $filters, $limit, $offset);
                    break;
            }
        } catch (InvalidRequestException $ire) {
            CommonUtilities::print_error_message('InvalidRequestException!<br><br>' . $ire->getMessage());
        } catch (AiravataClientException $ace) {
            CommonUtilities::print_error_message('AiravataClientException!<br><br>' . $ace->getMessage());
        } catch (AiravataSystemException $ase) {
            if ($ase->airavataErrorType == 2) // 2 = INTERNAL_ERROR
            {
                CommonUtilities::print_info_message('<p>You have not created any projects yet, so no results will be returned!</p>
                                <p>Click <a href="create_project.php">here</a> to create a new project.</p>');
            } else {
                CommonUtilities::print_error_message('There was a problem with Airavata. Please try again later, or report a bug using the link in the Help menu.');
                //print_error_message('AiravataSystemException!<br><br>' . $ase->airavataErrorType . ': ' . $ase->getMessage());
            }
        } catch (TTransportException $tte) {
            CommonUtilities::print_error_message('TTransportException!<br><br>' . $tte->getMessage());
        }

        return $projects;
    }

    /**
     * Set sharing settings for a given project.
     * @param projectId
     * @param $users A map of username => {read_permission, write_permission}
     */
    private static function share_project($projectId, $users) {
        $project = Airavata::getProject(Session::get("authz-token"), $projectId);
        $users->{$project->owner} = new stdClass();
        $users->{$project->owner}->read = true;
        $users->{$project->owner}->write = true;

        $wadd = array();
        $wrevoke = array();
        $radd = array();
        $rrevoke = array();

        foreach ($users as $user => $perms) {
            if ($perms->write) {
                $wadd[$user] = ResourcePermissionType::WRITE;
            }
            else {
                $wrevoke[$user] = ResourcePermissionType::WRITE;
            }

            if ($perms->read) {
                $radd[$user] = ResourcePermissionType::READ;
            }
            else {
                $rrevoke[$user] = ResourcePermissionType::READ;
            }
        }

        GrouperUtilities::shareResourceWithUsers($projectId, ResourceType::PROJECT, $wadd);
        GrouperUtilities::revokeSharingOfResourceFromUsers($projectId, ResourceType::PROJECT, $wrevoke);

        GrouperUtilities::shareResourceWithUsers($projectId, ResourceType::PROJECT, $radd);
        GrouperUtilities::revokeSharingOfResourceFromUsers($projectId, ResourceType::PROJECT, $rrevoke);
    }
}
