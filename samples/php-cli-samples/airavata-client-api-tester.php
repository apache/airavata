<?php
namespace Airavata\Client\Samples;

$GLOBALS['THRIFT_ROOT'] = '../lib/Thrift/';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TTransport.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TBufferedTransport.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TSocket.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TBinaryProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TApplicationException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TProtocolException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TTransportException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Base/TBase.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Type/TType.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Type/TMessageType.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Factory/TStringFuncFactory.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'StringFunc/TStringFunc.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'StringFunc/Core.php';

$GLOBALS['AIRAVATA_ROOT'] = '../lib/Airavata/';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Airavata.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/Workspace/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/Workspace/Experiment/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Error/Types.php';

require_once '../lib/AiravataClientFactory.php';

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

$airavataconfig = parse_ini_file("airavata-client-properties.ini");

/* this is the same as the factory */
/* - Temporarity overriding to connect to test server.
$transport = new TSocket($airavataconfig['AIRAVATA_SERVER'], $airavataconfig['AIRAVATA_PORT']);
$transport->setRecvTimeout($airavataconfig['AIRAVATA_TIMEOUT']);
*/
$transport = new TSocket('gw127.iu.xsede.org', 8930);
$transport->setRecvTimeout(20000);
$protocol = new TBinaryProtocol($transport);
$transport->open();
$airavataclient = new AiravataClient($protocol);

try
{
    if ($argc != 2)
    {
        echo 'php airavata-client-api-tester.php <username>';
    }
    else
    {

        /* ComputationalResourceScheduling data for Trestles*/
        $cmRST = new ComputationalResourceScheduling();
        $cmRST->resourceHostId = "trestles.sdsc.edu";
        $cmRST->ComputationalProjectAccount = "sds128";
        $cmRST->totalCPUCount = 1;
        $cmRST->nodeCount = 1;
        $cmRST->numberOfThreads = 0;
        $cmRST->queueName = "normal";
        $cmRST->wallTimeLimit = 15;
        $cmRST->jobStartTime = 0;
        $cmRST->totalPhysicalMemory = 0;

        /* ComputationalResourceScheduling data for Stampede */
        $cmRSS = new ComputationalResourceScheduling();
        $cmRSS->resourceHostId = "stampede.tacc.xsede.org";
        $cmRSS->ComputationalProjectAccount = "TG-STA110014S";
        $cmRSS->totalCPUCount = 1;
        $cmRSS->nodeCount = 1;
        $cmRSS->numberOfThreads = 0;
        $cmRSS->queueName = "normal";
        $cmRSS->wallTimeLimit = 15;
        $cmRSS->jobStartTime = 0;
        $cmRSS->totalPhysicalMemory = 0;

        /* UserConfigurationData using either Trestles or Stampede*/
        //$cmRS = $cmRSS;
        $cmRS = $cmRST;
        $userConfigurationData = new UserConfigurationData();
        $userConfigurationData->airavataAutoSchedule = 0;
        $userConfigurationData->overrideManualScheduledParams = 0;
        $userConfigurationData->computationalResourceScheduling = $cmRS;
        //var_dump($cmRS);
        //var_dump($userConfigurationData);

        /*Application ID for Trestles or Stamepede */
        $appId_trestles = "SimpleEcho2";
        $appId_stampede = "SimpleEcho3";
        //$appId = $appId_stampede;
        $appId = $appId_trestles;

        /* Experiment input and output data. */
        $input = new DataObjectType();
        $input->key = "echo_input";
        $input->value = "echo_output=Hello World";
        $input->type = DataType::STRING;
        $exInputs = array($input);

        $output = new DataObjectType();
        $output->key = "echo_output";
        $output->value = "";
        $output->type = DataType::STRING;
        $exOutputs = array($output);

	
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
        $experiment->applicationId = $appId;
        $experiment->userConfigurationData = $userConfigurationData;
        $experiment->experimentInputs = $exInputs;
        $experiment->experimentOutputs = $exOutputs;
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
        $update_userConfigurationData->computationalResourceScheduling = $cmRSS; 
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

