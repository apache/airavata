<?php 

class Category{
	function __construct($id, $name){
		$this->id=$id;
		$this->name=$name;
	}
	
	function getId(){
		return $this->id; 
	}
	
	function getName(){
		return $this->name;
	}
}
?>