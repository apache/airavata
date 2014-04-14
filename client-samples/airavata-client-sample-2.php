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
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/Workspace/Experiment/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Error/Types.php';

require_once '../lib/AiravataClientFactory.php';

use Airavata\Model\Workspace\Experiment\ComputationalResourceScheduling;
use Airavata\Model\Workspace\Experiment\DataObjectType;
use Airavata\Model\Workspace\Experiment\UserConfigurationData;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TSocket;
use Thrift\Transport\TBufferedTransport;
use Airavata\API\AiravataClient;
use Airavata\Model\Workspace\Experiment\Experiment;
use Airavata\Model\Workspace\Experiment\JobState;
use Airavata\Client\AiravataClientFactory;

/* buffered transport
$socket = new TSocket('gw111.iu.xsede.org', 8930);
$transport = new TBufferedTransport($socket);
$protocol = new TBinaryProtocol($transport);

$airavataclient = new AiravataClient($protocol);

$transport->open();
*/

/* client factory
$clientfactory = new AiravataClientFactory('gw111.iu.xsede.org', 8930);
$airavataclient = $clientfactory->getAiravataClient();
*/

/* this is the same as the factory */
$transport = new TSocket('gw111.iu.xsede.org', 8930);
$protocol = new TBinaryProtocol($transport);
$transport->open();
$airavataclient = new AiravataClient($protocol);




try {
    echo "Airavata Server Version is: " . $airavataclient->GetAPIVersion() . "<br>";


    $userExperiments = $airavataclient->getAllUserExperiments("admin");
    echo "# of user experiments = " . sizeof($userExperiments) . "<br>";

    $projectExperiments = $airavataclient->getAllExperimentsInProject("project1");
    echo "# of project experiments = " . sizeof($projectExperiments) . "<br>";

    // TTransportException here!
    //$projects = $airavataclient->getAllUserProjects("admin");


    //$expId = "PHPTest_8ad02879-b2e5-497c-b9ee-056803fde8d7";


    /*echo "<br><br>\n\n"."Getting experiment.... "."<br>\n";
    $experiment = $airavataclient->getExperiment($expId);
    echo "Experiment Name: ". $experiment->name ."<br>\n";*/


    /*echo "<br><br>\n\n" . "Getting experiment output.... "."<br>\n";
    $output = $airavataclient->getExperimentOutputs($expId);
    echo "output length: " . count($output) . "<br>\n";
    echo "output type: " . $output[0]->type . "<br>\n";*/



    /*echo "<br><br>\n\n"."Checking Job Status.... "."<br>\n";
    $jobStatus = $airavataclient->getJobStatuses($expId);
    
    echo "Job Status: "."<br>\n";
    echo "State: " . JobState::$__names[$jobStatus[array_keys($jobStatus)[0]]->jobState]. "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\t".
        "Time of last state change:" . $jobStatus[array_keys($jobStatus)[0]]->timeOfStateChange;*/



} catch (TException $texp) {
    print 'Exception: ' . $texp->getMessage()."\n";
} catch (AiravataSystemException $ase) {
    print 'Airavata System Exception: ' . $ase->getMessage()."\n";
}
//$transport->close();

?>

