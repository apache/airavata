<?php

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\ExperimentNotFoundException;
use Airavata\API\Error\InvalidRequestException;
use Thrift\Exception\TTransportException;

include 'getAiravataClient.php';
global $airavataclient;
global $transport;

if ($argc != 2)
{
    exit("php getExperimentOutputs.php <experiment_id> \n");
}

$expId = $argv[1];

$outputs = get_experiment_outputs($expId);

foreach ($outputs as $output)
{
    echo "$output->type: $output->value      <br><br>";
}

var_dump($outputs);


$transport->close();


/**
 * Get the experiment with the given ID
 * @param $expId
 * @return null
 */
function get_experiment_outputs($expId)
{
    global $airavataclient;

    try
    {
        return $airavataclient->getExperimentOutputs($expId);
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

