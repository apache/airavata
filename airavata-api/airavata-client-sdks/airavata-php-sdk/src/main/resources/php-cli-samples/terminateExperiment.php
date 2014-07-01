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
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/Workspace/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/Workspace/Experiment/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Error/Types.php';

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


if ($argc != 2)
{
    echo 'php terminateExperiment.php <experiment_id>';
}
else
{
    terminate_experiment($argv[1]);

    echo 'If there are no exceptions, assume the experiment terminated successfully';
}


$transport->close();


/**
 * End the experiment with the given ID
 * @param $expId
 */
function terminate_experiment($expId)
{
    global $airavataclient;

    try
    {
        $airavataclient->terminateExperiment($expId);
    }
    catch (InvalidRequestException $ire)
    {
        echo 'InvalidRequestException!\n\n' . $ire->getMessage();
    }
    catch (ExperimentNotFoundException $enf)
    {
        echo 'ExperimentNotFoundException!\n\n' . $enf->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        echo 'AiravataClientException!\n\n' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        echo 'AiravataSystemException!\n\n' . $ase->getMessage();
    }
    catch (TTransportException $tte)
    {
        echo 'TTransportException!\n\n' . $tte->getMessage();
    }
    catch (\Exception $e)
    {
        echo 'Exception!\n\n' . $e->getMessage();
    }
}

?>

