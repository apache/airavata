<?php 
	class Test{
		function __construct($name, $age){
			$this->name=$name;
			$this->age=$age;
		}
	}
	$test=array(5,6,7,8);
	$str=serialize($test);
	echo $str;
	echo "<br />";
	var_dump(unserialize($str));
	echo "<br />";
	$test=new Test("Saminda", array("age"=>18,"school"=>"iu"));
	var_dump($test);
	echo "<br />";
	echo serialize($test);
	echo "<br />";
	var_dump(unserialize(serialize($test)));
	echo "<br />";
	echo serialize(array());
?>