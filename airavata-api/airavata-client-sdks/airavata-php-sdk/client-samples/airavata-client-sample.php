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

//Create a Experiment
$experiment = new Experiment();
$experiment->name = "Test Experiment";
$experiment->userName = "TestUser";
$experiment->projectID = "TestProject";

$experimentInputs = new DataObjectType;
$experimentInputs->key = "echo_input";
$experimentInputs->value = "echo_output=Hello World";

$experiment->applicationId = "SimpleEcho2";
$experiment->experimentInputs = $experimentInputs;

$scheduling = new ComputationalResourceScheduling;
$scheduling->resourceHostId = "gsissh-trestles";

$userConfigData = new UserConfigurationData;
$userConfigData->computationalResourceScheduling = $scheduling;
$experiment->userConfigurationData = $userConfigData;


try {

    echo "Experiment Id created is: " . $airavataclient->createExperiment($experiment);

} catch (TException $texp) {
    print 'Exception: ' . $texp->getMessage()."\n";
} catch (AiravataSystemException $ase) {
    print 'Airavata System Exception: ' . $ase->getMessage()."\n";
}
$transport->close();

?>

