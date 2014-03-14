<?php
namespace Airavata\Client\Samples;

$GLOBALS['THRIFT_ROOT'] = '../lib/Thrift/';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TTransport.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TSocket.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TBinaryProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TApplicationException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TProtocolException.php';
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

use Airavata\Model\Workspace\Experiment\ComputationalResourceScheduling;
use Airavata\Model\Workspace\Experiment\DataObjectType;
use Airavata\Model\Workspace\Experiment\UserConfigurationData;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;
use Airavata\Model\Workspace\Experiment\Experiment;

$transport = new TSocket('gw111.iu.xsede.org', 8930);
$protocol = new TBinaryProtocol($transport);

$airavataclient = new AiravataClient($protocol);
$transport->open();

echo "Airavata Server Version is: " . $airavataclient->GetAPIVersion();

echo "<br><br>"."Creating New Experiment.... "."<br>";

//Create a Experiment
$experiment = new Experiment();
$experiment->name = "PHPTest";
$experiment->description = "Testingfromphp";
$experiment->userName = "admin";
$experiment->projectID = "project1";
$experiment->applicationId = "US3AppTrestles";

$experimentInputs = new DataObjectType();
$experimentInputs->key = "input";
$experimentInputs->value = "file:///home/airavata/input/hpcinput.tar";
$experiment->experimentInputs = array($experimentInputs);

$scheduling = new ComputationalResourceScheduling();
$scheduling->resourceHostId = "gsissh-trestles";

$userConfigData = new UserConfigurationData();
$userConfigData->computationalResourceScheduling = $scheduling;
$userConfigData->overrideManualScheduledParams = False;
$userConfigData->airavataAutoSchedule = False;
$experiment->userConfigurationData = $userConfigData;

try {

    $expId = $airavataclient->createExperiment($experiment);
    echo "Experiment Id created is: " . $expId;

    echo "<br><br>"."Launching Experiment.... "."<br>";
    $airavataclient->launchExperiment($expId, "airavataToken");
    echo "....Launched Experiment ".$expId."<br>";

    echo "<br><br>"."Checking Experiment Status.... "."<br>";
    $experimentStatus = $airavataclient->getExperimentStatus($expId);
    echo "Experiment Status: "."<br>";
    echo "State: ".$experimentStatus->ExperimentState ."&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;".
        "Time of last state change:". $experimentStatus->timeOfStateChange;

    echo "<br><br>"."Checking Job Status.... "."<br>";
    $jobStatus = $airavataclient->getJobStatuses($expId);
    echo "Job Status: "."<br>";
    echo "State: "."&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;".
        "Time of last state change:";

} catch (TException $texp) {
    print 'Exception: ' . $texp->getMessage()."\n";
} catch (AiravataSystemException $ase) {
    print 'Airavata System Exception: ' . $ase->getMessage()."\n";
}
$transport->close();

?>

