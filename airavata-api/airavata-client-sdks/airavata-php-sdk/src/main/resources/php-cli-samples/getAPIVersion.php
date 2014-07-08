<?php

/**
 * Bundle all thrift and Airavata stubs into a include file. This is simple but not so elegant way.
 *  Contributions welcome to improve writing PHP Client Samples.
 *
 */
include 'getAiravataClient.php';
global $airavataclient;
global $transport;

try
{
	$version = $airavataclient->getAPIVersion();
}
catch (TException $texp)
{
    print 'Exception: ' . $texp->getMessage()."\n";
}


echo 'Airavata server version is ' . $version;


$transport->close();

?>

