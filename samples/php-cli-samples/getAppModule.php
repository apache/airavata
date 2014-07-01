<?php
namespace Airavata\Client\Samples;

$airavataconfig = parse_ini_file("airavata-client-properties.ini");

$GLOBALS['THRIFT_ROOT'] = $airavataconfig['THRIFT_LIB_DIR'];
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

$GLOBALS['AIRAVATA_ROOT'] = $airavataconfig['AIRAVATA_PHP_STUBS_DIR'];
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Airavata.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Error/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/AppCatalog/AppDeployment/Types.php';

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\ExperimentNotFoundException;
use Airavata\API\Error\InvalidRequestException;
use Airavata\Client\AiravataClientFactory;
use Airavata\Model\Workspace\Experiment\ExperimentState;
use Thrift\Exception\TTransportException;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TBufferedTransport;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;

$transport = new TSocket($airavataconfig['AIRAVATA_SERVER'], $airavataconfig['AIRAVATA_PORT']);
$transport->setRecvTimeout($airavataconfig['AIRAVATA_TIMEOUT']);

$protocol = new TBinaryProtocol($transport);
$transport->open();
$airavataclient = new AiravataClient($protocol);


if (count($argv) < 2) {
    exit("Please provide an appModuleID. \n");
}

$appModuleId = $argv[1];

$applicationModule = get_appModule($appModuleId);

var_dump($applicationModule);

$transport->close();

/**
 * Get the appModule with the given ID
 * @param $appModuleId
 * @return null
 */
function get_appModule($appModuleId)
{
    global $airavataclient;

    try
    {
        return $airavataclient->getAppicationModule($appModuleId);
    }
    catch (InvalidRequestException $ire)
    {
        echo 'InvalidRequestException!<br><br>' . $ire->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        echo 'AiravataClientException!<br><br>' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        echo 'AiravataSystemException!<br><br>' . $ase->getMessage();
    }
    catch (TTransportException $tte)
    {
        echo 'TTransportException!<br><br>' . $tte->getMessage();
    }
    catch (\Exception $e)
    {
        echo 'Exception!<br><br>' . $e->getMessage();
    }

}

?>

