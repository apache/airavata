<?php

function getExecutablePath(){
   return realpath("sample_scripts");
}


function getModulesNames(){
	$modules = array();
	array_push($modules,"add.sh");
	array_push($modules,"echo.sh");
	array_push($modules,"multiply.sh");
	array_push($modules,"subtract.sh");
	return $modules;
}
?>