<?php 
class Component{
	function __construct($url,$id){
		$this->url=$url;
		$this->id=$id;
	}
	
	function getURL(){
		return $this->url;
	}
	
	function getId(){
		return $this->id;
	}
}
?>