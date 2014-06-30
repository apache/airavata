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
use Airavata\Model\AppCatalog\ComputeResourceDescription;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;
use Airavata\API\AppCatalog\ApplicationCatalogAPIClient;
use Airavata\Model\Workspace\Experiment\Experiment;
use Airavata\Model\AppCatalog\JobSubmissionProtocol;
use Airavata\Model\AppCatalog\DataMovementProtocol;
use Airavata\Model\AppCatalog\GSISSHJobSubmission;
use Airavata\Model\AppCatalog\ResourceJobManager;

$airavataconfig = parse_ini_file("airavata-client-properties.ini");

$transport = new TSocket($airavataconfig['APP_CATALOG_SERVER'], $airavataconfig['APP_CATALOG_PORT']);
$transport->setRecvTimeout($airavataconfig['AIRAVATA_TIMEOUT']);

$protocol = new TBinaryProtocol($transport);

$airavataclient = new ApplicationCatalogAPIClient($protocol);
$transport->open();

echo "Airavata Server Version is: " . $airavataclient->getAPIVersion() . "\n";

echo "Add Compute Resources.... "."\n";

$id_list = $airavataclient->listComputeResourceDescriptions();
$compute_resource = new ComputeResourceDescription();
$compute_resource->hostName="test-stampede-host"."-".time();
$compute_resource->hostAliases=array("stampede");
$compute_resource->ipAddresses=array("stampede.tacc.xsede.org");
$compute_resource->isEmpty=false;
$compute_resource->scratchLocation="/home1/01437/ogce";
$compute_resource->jobSubmissionProtocols=array();
$compute_resource->dataMovementProtocols=array();

echo "Adding ".$compute_resource->hostName."...";
$compute_resource_id=$airavataclient->addComputeResourceDescription($compute_resource);
echo "done [saved in the catalog as ".$compute_resource_id."]\n";

$gsissh_protoco_data=new GSISSHJobSubmission();
$gsissh_protoco_data->resourceJobManager=ResourceJobManager::SLURM;
$gsissh_protoco_data->installedPath="/usr/bin/";
$gsissh_protoco_data->sshPort=2222;
$gsissh_protoco_data->monitorMode="push";

echo "Adding GSISSH protocol data to ".$compute_resource_id."...";
$airavataclient->addGSISSHJobSubmissionProtocol($compute_resource_id, $gsissh_protoco_data);
echo "done\n";
$transport->close();
?>
