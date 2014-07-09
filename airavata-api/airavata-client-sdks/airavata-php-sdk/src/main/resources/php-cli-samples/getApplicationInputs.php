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
use Airavata\Model\AppCatalog\AppInterface\DataType;

try
{

    if ($argc < 1)
    {
        echo 'php getApplicationInputs.php <appInterfaceId>';
    }
    else {

        $appInterfaceId = $argv[1];

        $appInputs = $airavataclient->getApplicationInputs($appInterfaceId);

        if ($appInputs) {
            foreach ($appInputs as $appInput) {
                var_dump($appInput);
                $inputType = DataType::$__names[$appInput->type];
                echo "\n Application Input Name: $appInput->name \t Input Type $inputType \n";
            }
        } else {
            echo "\n Failed to fetch application interface Inputs. \n";
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

