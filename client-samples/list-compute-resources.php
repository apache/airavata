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
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;
use Airavata\API\AppCatalog\ApplicationCatalogAPIClient;
use Airavata\Model\Workspace\Experiment\Experiment;
use Airavata\Model\AppCatalog\JobSubmissionProtocol;
use Airavata\Model\AppCatalog\DataMovementProtocol;
use Airavata\Model\AppCatalog\ResourceJobManager;

$airavataconfig = parse_ini_file("airavata-client-properties.ini");

$transport = new TSocket($airavataconfig['APP_CATALOG_SERVER'], $airavataconfig['APP_CATALOG_PORT']);
echo $airavataconfig['AIRAVATA_TIMEOUT']."\n";
$transport->setRecvTimeout($airavataconfig['AIRAVATA_TIMEOUT']);

$protocol = new TBinaryProtocol($transport);

$airavataclient = new ApplicationCatalogAPIClient($protocol);
$transport->open();

echo "Airavata Server Version is: " . $airavataclient->getAPIVersion() . "\n";

echo "Listing Compute Resources.... "."\n";

$id_list = $airavataclient->listComputeResourceDescriptions();

foreach($id_list as $id){
	echo "Compute Resource Id : ".$id."\n";
	$compute_resource = $airavataclient->getComputeResourceDescription($id);
	echo "\t"."Host name : " . $compute_resource->hostName ."\n";
	echo "\t"."Aliases : " . implode(",",array_keys($compute_resource->hostAliases)) ."\n";
	echo "\t"."Ip addresses : " . implode(",",array_keys($compute_resource->ipAddresses)) ."\n";
	echo "\t".count($compute_resource->jobSubmissionProtocols)." Job Submission Protocols Supported"."\n";
	foreach($compute_resource->jobSubmissionProtocols as $protocol_data_id => $protocol_type){
		echo "\t\t".$protocol_data_id."[".JobSubmissionProtocol::$__names[$protocol_type]. "]"."\n";
		switch ($protocol_type){
			case JobSubmissionProtocol::GRAM:
				$globus_data=$airavataclient->getGlobusJobSubmissionProtocol($protocol_data_id);
				echo "\t\t\tGate Keeper Endpoint(s) : ".implode(",",($globus_data->globusGateKeeperEndPoint))."\n";
				break;
			case JobSubmissionProtocol::GSISSH:
				$gsissh_data=$airavataclient->getGSISSHJobSubmissionProtocol($protocol_data_id);
				echo "\t\t\tResource Job Manager : ".ResourceJobManager::$__names[$gsissh_data->resourceJobManager]."\n";
				echo "\t\t\tInstalled Path : ".$gsissh_data->installedPath."\n";
				echo "\t\t\tSSH port : ".$gsissh_data->sshPort."\n";
				echo "\t\t\tMonitor Mode : ".$gsissh_data->monitorMode."\n";
				break;
		}
	}
	echo "\t".count($compute_resource->dataMovementProtocols)." Data Movement Protocols Supported"."\n";
	foreach($compute_resource->dataMovementProtocols as $protocol_data_id => $protocol_type){
		echo "\t\t".$protocol_data_id."[".DataMovementProtocol::$__names[$protocol_type] . "]"."\n";
			switch ($protocol_type){
			case DataMovementProtocol::GridFTP:
				$gridftp_data=$airavataclient->getGridFTPDataMovementProtocol($protocol_data_id);
				echo "\t\t\tGrid FTP Endpoint(s) : ".implode(",",($gridftp_data->gridFTPEndPoint))."\n";
				break;
		}
	}
}

//$compute_resource = new \ComputeResourceDescription();
//$compute_resource->hostName="localhost";
//$compute_resource->hostAliases=array("localhost");
//$compute_resource->ipAddresses=array("127.0.0.1");

//Create a Experiment
$transport->close();

?>


