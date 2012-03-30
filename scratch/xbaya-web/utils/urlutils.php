<?php

function startsWith($haystack, $needle)
{
	$length = strlen($needle);
	return (substr($haystack, 0, $length) === $needle);
}

function endsWith($haystack, $needle)
{
	$length = strlen($needle);
	$start  = $length * -1; //negative
	return (substr($haystack, $start) === $needle);
}
function removeFileName($url){
	$filename = basename($url);
	return substr($url,0,strlen($url)-strlen($filename));
}

function attachToURLInPath($url, $filename){
	return (endsWith($url,"/"))? $url+$filename: removeFileName($url).$filename;
}

function attachToCurrentURLInPath($filename){
	$url = getCurrentURL();
	return (endsWith($url,"/"))? $url+$filename: removeFileName($url).$filename;
}

function getCurrentURL(){
	return (!empty($_SERVER['HTTPS'])) ? "https://".$_SERVER['SERVER_NAME'].$_SERVER['REQUEST_URI'] : "http://".$_SERVER['SERVER_NAME'].$_SERVER['REQUEST_URI'];
}
?>