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
/* buffered transport
$socket = new TSocket('gw111.iu.xsede.org', 8930);
$transport = new TBufferedTransport($socket, 1024, 1024);
$protocol = new TBinaryProtocol($transport);
$airavataclient = new AiravataClient($protocol);

$transport->open();
*/

/* client factory
$airavataClientFactory = new AiravataClientFactory(array('airavataServerHost' => "gw111.iu.xsede.org", 'airavataServerPort' => "8930"));
$airavataclient = $airavataClientFactory->getAiravataClient();
*/

/* this is the same as the factory */
$transport = new TSocket('gw111.iu.xsede.org', 8930);
$transport->setRecvTimeout(5000);

$protocol = new TBinaryProtocol($transport);
$transport->open();
$airavataclient = new AiravataClient($protocol);


try
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

   /* ComputationalResourceScheduling data for Stampede
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
   */

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

   /* Create Experiment: needs to update using unique project ID. */
    $proj = "default";
    $user = "admin";
    $exp_name = "Test Experiment";

    $experiment = new Experiment();
    $experiment->projectID = $proj;
    $experiment->userName = $user;
    $experiment->name = $exp_name;
    $experiment->applicationId = $appId;
    $experiment->userConfigurationData = $userConfigurationData;
    $experiment->experimentInputs = $exInputs;
    $experiment->experimentOutputs = $exOutputs;
 
    $expId = $airavataclient->createExperiment($experiment);

    if ($expId) {
        echo "Experiment $expId created!";
    } else {
        echo "Failed to create experiment.";
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





$transport->close();

?>

