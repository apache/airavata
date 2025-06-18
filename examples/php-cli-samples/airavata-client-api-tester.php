<?php
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * @license http://www.apache.org/licenses/LICENSE-2.0 Apache V2
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

/**
 * Bundle all thrift and Airavata stubs into a include file. This is simple but not so elegant way.
 *  Contributions welcome to improve writing PHP Client Samples.
 *
 */
include 'getAiravataClient.php';
global $airavataclient;
global $transport;

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\InvalidRequestException;
use Airavata\API\Error\ExperimentNotFoundException;
use Airavata\Client\AiravataClientFactory;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TBufferedTransport;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;

use Airavata\Model\Workspace\Project;
use Airavata\Model\Workspace\Experiment\Experiment;
use Airavata\Model\Workspace\Experiment\DataObjectType;
use Airavata\Model\Workspace\Experiment\UserConfigurationData;
use Airavata\Model\Workspace\Experiment\ComputationalResourceScheduling;
use Airavata\Model\Workspace\Experiment\DataType;
use Airavata\Model\Workspace\Experiment\ExperimentState;

/* this is the same as the factory */
/* - Temporarity overriding to connect to test server.
$transport = new TSocket($airavataconfig['AIRAVATA_SERVER'], $airavataconfig['AIRAVATA_PORT']);
$transport->setRecvTimeout($airavataconfig['AIRAVATA_TIMEOUT']);
*/

try
{
    if ($argc != 2)
    {
        echo 'php airavata-client-api-tester.php <username>';
    }
    else
    {

        $applicationId = $appcatalogdocs['echoInterfaceId'];

        $applicationInput = new DataObjectType();
        $applicationInput->key = "Input_to_Echo";
        $applicationInput->value = "Hello World";
        $experimentInputs = array($applicationInput);

        /**
         *  NOTE: For convenience, all the computational hosts are provided.
         *        Comment/Uncomment appropriately to schedule on alternative resources
         *        If all hosts are uncommented, the last one will be picked.
         */

        $scheduling = new ComputationalResourceScheduling();

        /* IU BigRed II Cluster */
        $scheduling->resourceHostId = $appcatalogdocs['bigredResourceId'];

        /* TACC Stampede Cluster */
        //$scheduling->resourceHostId = $appcatalogdocs['stampedeResourceId'];
        //$scheduling->ComputationalProjectAccount = "TG-STA110014S";

        /* SDSC Trestles Cluster */
        //$scheduling->resourceHostId = $appcatalogdocs['trestlesResourceId'];
        //$scheduling->ComputationalProjectAccount = "sds128";

        /* Job dimensions and resource queue */
        $scheduling->totalCPUCount = 1;
        $scheduling->nodeCount = 1;
        $scheduling->wallTimeLimit = 15;
        $scheduling->queueName = "normal";

        $userConfigurationData = new UserConfigurationData();
        $userConfigurationData->airavataAutoSchedule = 0;
        $userConfigurationData->overrideManualScheduledParams = 0;
        $userConfigurationData->computationalResourceScheduling = $scheduling;

        /**
         * An experiment is created within Airavata and all the provided inputs and configurations are persisted
         *  within the Airavata Registry.
         *
         * NOTE: Airavata uses a 2 step launch process. The creation just creates the experiment. The launch step
         *   executes the created experiment.
         */

        /* Simple workflow test. */
        $user = $argv[1];
        
        /* Create Project */
        $project = new Project();
	    $project->owner = $user;
	    $project->name = "LoadTesterProject";
  	    $projId = $airavataclient->createProject($project);
	    echo "$user created project $projId. \n";

        /* Create Experiment */
        $experiment = new Experiment();
        $experiment->projectID = $projId;
        $experiment->userName = $user;
        $experiment->name = "LoadTesterExperiment_".time();
        $experiment->applicationId = $applicationId;
        $experiment->userConfigurationData = $userConfigurationData;
        $experiment->experimentInputs = $exInputs;
        $expId = $airavataclient->createExperiment($experiment);
	echo "$user created experiment $expId. \n";
        //var_dump($experiment);

        /* Get whole project */
	$uproj = $airavataclient->getProject($projId);
 	echo "$user $projId detail follows: \n";
	var_dump($uproj);

        /* Update Project */
	$uproj->description = "Updated project description: ".time();
	$airavataclient->updateProject($projId, $uproj);
	echo "$user updated project $projId. \n";

        /* Get whole experiment */
	$uexp = $airavataclient->getExperiment($expId);
        echo "$user experiment $expId detail follows: \n";
        var_dump($uexp);

	/* Update Experiment */
	$uexp->description = "Updated experiment description: ".time();
	$airavataclient->updateExperiment($expId, $uexp);
	echo "$user updated experiment $expId. \n";

	/* Clone Experiment */
	$clone_expId = $airavataclient->cloneExperiment($expId, "CloneLoadTesterExperiment_".time());
	echo "$user cloned experiment $expId as $clone_expId. \n";

	/* Update Experiment Configuration */
        $update_userConfigurationData = new UserConfigurationData();
        $update_userConfigurationData->airavataAutoSchedule = 0;
        $update_userConfigurationData->overrideManualScheduledParams = 0;
        $update_userConfigurationData->computationalResourceScheduling = $scheduling;
	$airavataclient->updateExperimentConfiguration($expId, $update_userConfigurationData);
	echo "$user updated user configuration data for experiment $expId. \n";

	/* Update Resource Scheduleing */
	//$airavataclient->updateResourceScheduleing($expId, $cmRST);
	//echo "$user updated resource scheduleing for experiment $expId. \n";

	/* Validate experiment */
	//$valid = $airavataclient->validateExperiment($expId);
	//echo "$user experiment $expId validation is $valid. \n";

        /* Launch Experiment */
	//$airavataclient->launchExperiment($expId, 'airavataToken');
	//echo "$user experiment $expId is launched.";

	/* Get experiment status */ 
	$experimentStatus = $airavataclient->getExperimentStatus($expId);
        $experimentStatusString =  ExperimentState::$__names[$experimentStatus->experimentState];
	echo "$user experiment $expId status is $experimentStatusString. \n";

        /* Get additional information */
        //$version = $airavataclient->GetAPIVersion();
        //echo "$user Airavata Server Version is $version. \n"; 

	$userProjects = $airavataclient->getAllUserProjects($user);
        echo "$user total number of projects is " . sizeof($userProjects) . ". \n";

	$userExperiments = $airavataclient->getAllUserExperiments($user);
        echo "$user total number of experiments is " . sizeof($userExperiments) . ". \n";

        //echo $projId;
        $projectExperiments = $airavataclient->getAllExperimentsInProject($projId);
        echo "$user number of experiments in $projId is " . sizeof($projectExperiments) . ". \n";	
    }

}
catch (InvalidRequestException $ire)
{
    print 'InvalidRequestException: ' . $ire->getMessage()."\n";
}
catch (AiravataClientException $ace)
{
    print 'Airavata System Exception: ' . $ace->getMessage()."\n";
}
catch (AiravataSystemException $ase)
{
    print 'Airavata System Exception: ' . $ase->getMessage()."\n";
}
catch (ExperimentNotFoundException $enf)
{
    print 'Experiment Not Found Exception: ' . $enf->getMessage()."\n";
}



$transport->close();

?>

