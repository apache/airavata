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
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/AppCatalog/ApplicationCatalogAPI.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/Workspace/Experiment/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/AppCatalog/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Error/Types.php';

use Airavata\Model\Workspace\Experiment\ComputationalResourceScheduling;
use Airavata\Model\Workspace\Experiment\DataObjectType;
use Airavata\Model\Workspace\Experiment\UserConfigurationData;
use Airavata\Model\ComputeResourceDescription;
use Airavata\Model\ApplicationInterface;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;
use Airavata\API\AppCatalog\ApplicationCatalogAPIClient;
use Airavata\Model\Workspace\Experiment\Experiment;
use Airavata\Model\AppCatalog\JobSubmissionProtocol;
use Airavata\Model\AppCatalog\DataMovementProtocol;

$airavataconfig = parse_ini_file("airavata-client-properties.ini");

$transport = new TSocket($airavataconfig['APP_CATALOG_SERVER'], $airavataconfig['APP_CATALOG_PORT']);
$transport->setRecvTimeout($airavataconfig['AIRAVATA_TIMEOUT']);

$protocol = new TBinaryProtocol($transport);

$airavataclient = new ApplicationCatalogAPIClient($protocol);
$transport->open();

echo "Airavata Server Version is: " . $airavataclient->getAPIVersion() . "\n";

echo "Listing Application Interfaces.... "."\n";

$id_list = $airavataclient->listApplicationInterfaceIds();

foreach($id_list as $id){
	echo "Application Interface Id : ".$id."\n";
	$app_interface = $airavataclient->getApplicationInterface($id);
	echo "\t"."Interface Data : " . $app_interface->applicationInterfaceData."\n";
	echo "\t".count($app_interface->applicationDeployments)." Deployments"."\n";
	foreach($app_interface->applicationDeployments as $deployment){
		echo "\t\t"."Compute Resource : ".$deployment->computeResourceDescription->hostName."\n";
		echo "\t\t\t"."Application Data".$deployment->applicationDescriptor->applicationDescriptorData."\n";
	}
}

//$compute_resource = new \ComputeResourceDescription();
//$compute_resource->hostName="localhost";
//$compute_resource->hostAliases=array("localhost");
//$compute_resource->ipAddresses=array("127.0.0.1");

//Create a Experiment
$transport->close();

?>


