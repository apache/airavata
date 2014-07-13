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
use Thrift\Exception\TTransportException;

try {
    if ($argc != 2) {
        exit("Please specify a Experiment Id. Usage: php getExperimentOutputs.php <experiment_id> \n");
    } else {

        $experimentId = $argv[1];
        $experimentOutputs = $airavataclient->getExperimentOutputs($experimentId);

        var_dump($experimentOutputs);

        foreach ($experimentOutputs as $output) {
            echo "\n$output->key: $output->value \n";
        }
    }
} catch (InvalidRequestException $ire) {
    echo 'InvalidRequestException!<br><br>' . $ire->getMessage();
} catch (ExperimentNotFoundException $enf) {
    echo 'ExperimentNotFoundException!<br><br>' . $enf->getMessage();
} catch (AiravataClientException $ace) {
    echo 'AiravataClientException!<br><br>' . $ace->getMessage();
} catch (AiravataSystemException $ase) {
    echo 'AiravataSystemException!<br><br>' . $ase->getMessage();
} catch (TTransportException $tte) {
    echo 'TTransportException!<br><br>' . $tte->getMessage();
} catch (\Exception $e) {
    echo 'Exception!<br><br>' . $e->getMessage();
}

$transport->close();

?>

