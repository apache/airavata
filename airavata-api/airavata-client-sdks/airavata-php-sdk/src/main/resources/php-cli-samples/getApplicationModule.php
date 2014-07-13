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

use Airavata\Model\AppCatalog\AppDeployment\ApplicationModule;


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

    try {
        return $airavataclient->getApplicationModule($appModuleId);
    } catch (InvalidRequestException $ire) {
        echo 'InvalidRequestException!<br><br>' . $ire->getMessage();
    } catch (AiravataClientException $ace) {
        echo 'AiravataClientException!<br><br>' . $ace->getMessage();
    } catch (AiravataSystemException $ase) {
        echo 'AiravataSystemException!<br><br>' . $ase->getMessage();
    } catch (TTransportException $tte) {
        echo 'TTransportException!<br><br>' . $tte->getMessage();
    } catch (\Exception $e) {
        echo 'Exception!<br><br>' . $e->getMessage();
    }

}

?>

