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
    echo 'php updateExperiment.php <experiment_id>';
}
else
{
    update_experiment($argv[1]);
}

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
 * Update the experiment with the given ID
 * @param $expId
 * @return null
 */
function update_experiment($expId)
{
    global $airavataclient;

    try
    {
        //create new experiment to receive the clone
        $experiment = $airavataclient->getExperiment($expId);
        $experiment->name .= time();

        $airavataclient->updateExperiment($expId, $experiment);

        $updatedExperiment = $airavataclient->getExperiment($expId);

        echo "Experiment $experiment->name updated:\n\n";
        var_dump($updatedExperiment);
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
        echo 'AiravataSystemException during update!<br><br>' . $ase->getMessage();
    }
    catch (TTransportException $tte)
    {
        echo 'TTransportException!<br><br>' . $tte->getMessage();
    }
}

?>

