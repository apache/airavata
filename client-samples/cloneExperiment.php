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


$airavataconfig = parse_ini_file("airavata-client-properties.ini");

$transport = new TSocket($airavataconfig['AIRAVATA_SERVER'], $airavataconfig['AIRAVATA_PORT']);
$transport->setRecvTimeout($airavataconfig['AIRAVATA_TIMEOUT']);

$protocol = new TBinaryProtocol($transport);
$transport->open();
$airavataclient = new AiravataClient($protocol);


//$expId = 'experiment2_8f9c0ccc-6392-47b3-bcde-f4e82557bc32';


if ($argc != 2)
{
    echo 'php cloneExperiment.php <experiment_id>';
}
else
{
    $cloneId = clone_experiment($argv[1]);

    $clone = get_experiment($cloneId);

    var_dump($clone);

    echo "Created clone with ID $clone->experimentID named $clone->name \n\n";
}




//var_dump($experiment);


$transport->close();


/**
 * Get the experiment with the given ID
 * @param $expId
 * @return null
 */
function get_experiment($expId)
{
    global $airavataclient;

    try
    {
        return $airavataclient->getExperiment($expId);
    }
    catch (InvalidRequestException $ire)
    {
        echo 'InvalidRequestException!<br><br>' . $ire->getMessage();
    }
    catch (ExperimentNotFoundException $enf)
    {
        echo 'ExperimentNotFoundException!<br><br>' . $enf->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        echo 'AiravataClientException!<br><br>' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        echo 'AiravataSystemException during get!<br><br>' . $ase->getMessage();
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

/**
 * Clone the experiment with the given ID
 * @param $expId
 * @return null
 */
function clone_experiment($expId)
{
    global $airavataclient;

    try
    {
        $experiment = $airavataclient->getExperiment($expId);

        return $airavataclient->cloneExperiment($expId, $experiment->name .= time());
    }
    catch (InvalidRequestException $ire)
    {
        echo 'InvalidRequestException!<br><br>' . $ire->getMessage();
    }
    catch (ExperimentNotFoundException $enf)
    {
        echo 'ExperimentNotFoundException!<br><br>' . $enf->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        echo 'AiravataClientException!<br><br>' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        echo 'AiravataSystemException during clone!<br><br>' . $ase->getMessage();
    }
    catch (TTransportException $tte)
    {
        echo 'TTransportException!<br><br>' . $tte->getMessage();
    }
}

?>

