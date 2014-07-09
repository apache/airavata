<?php
/**
 * Bundle all thrift and Airavata stubs into a include file. This is simple but not so elegant way.
 *  Contributions welcome to improve writing PHP Client Samples.
 *
 */
include 'getAiravataClient.php';
global $airavataclient;
global $transport;

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

