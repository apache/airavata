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
use Airavata\API\Error\InvalidRequestException;
use Thrift\Exception\TTransportException;

try
{

    if ($argc < 1)
    {
        echo 'php getApplicationOutputs.php <appInterfaceId>';
    }
    else {

        $appInterfaceId = $argv[1];

        $appOutputs = $airavataclient->getApplicationOutputs($appInterfaceId);

        if ($appOutputs) {
            var_dump($appOutputs);
        } else {
            echo "\n Failed to fetch application interface Outputs. \n";
        }
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
catch (TTransportException $tte)
{
    echo 'TTransportException!<br><br>' . $tte->getMessage();
}
catch (\Exception $e)
{
    echo 'Exception!<br><br>' . $e->getMessage();
}

$transport->close();

?>

