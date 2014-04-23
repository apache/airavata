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

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\InvalidRequestException;
use Airavata\Client\AiravataClientFactory;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TBufferedTransport;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;

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
$protocol = new TBinaryProtocol($transport);
$transport->open();
$airavataclient = new AiravataClient($protocol);

echo "Airavata Server Version is: " . $airavataclient->GetAPIVersion();

echo "<br><br>"."Creating New Experiment.... "."<br>";

$version = 'DEFAULT';

try
{
    $version = $airavataclient->GetAPIVersion();
}
catch (TException $texp)
{
    print 'Exception: ' . $texp->getMessage()."\n";
}


echo 'Airavata server version is ' . $version;


$transport->close();

?>

