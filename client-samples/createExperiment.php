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
   $exp_name = $_GET["name"];
   $user = $_GET["user"];
   $proj = $_GET["proj"];

   $experiment = new Experiment();
   $experiment->projectID = $proj;
   $experiment->userName = $user;
   $experiment->name = $exp_name;
 
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

