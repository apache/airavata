<?php
function getSettings($filename){
	$content = file_get_contents($filename, true);
	$lines=explode("\n", $content);
	$elements=array();
	foreach ($lines as &$line) {
		$s=trim($line);
		if ($s!=""){
			$data=explode(",",$s);
			$data_elements=array();
			foreach($data as $d){
				$d2=explode(":",$d);
				$data_elements[$d2[0]]=$d2[1];
			}
			array_push($elements,$data_elements);
		}
	}
	return $elements;
}
?>