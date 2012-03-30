<?php
// phpinfo();
	class WSDLParser{
		function __construct($wsdlurl){
			$client=new SoapClient($wsdlurl);
			$functions=$client->__getFunctions();
			$typeStrings=$client->__getTypes();
			$types=array();
			foreach($typeStrings as $t){
				$types=$types+$this->extractParameterInfo($t);
			}
			$this->types=$types;
			$operations=array();
			foreach($functions as $f){
				$operation=$this->extractFunctionInfo($types,$f);
				$key=$operation->toString();
				if (!array_key_exists($key,$operations)){
					$operations[$key]=$operation;
				}
			}
			$this->operations=$operations;
// 			var_dump($client->);
// 			echo "<br />";
		}
		
		function extractParameterInfo($paramString){
			$parts1=explode("{", $paramString);
			$parts2=explode(" ",$parts1[0]);
			$function_name=$parts2[1];
			$parts3=explode("}",$parts1[1]);
			$parts3=explode(";",$parts3[0]);
			$parameters=array();
			foreach($parts3 as $p){
				$para=explode(" ",$p);
				$para=remove_array_spaces($para);
				if (count($para)>1){
					$parameters[$para[1]]=$para[0];
				}
			}
			return array($function_name=>$parameters);
		}
		
		function extractFunctionInfo($types,$functionString){
			$parts1=explode("(", $functionString);
			$parts2=explode(" ",$parts1[0]);
			$function_name=$parts2[1];
			$function_return_type=$parts2[0];
			$parts3=explode(")",$parts1[1]);
			$parts3=explode(";",$parts3[0]);
			$parameters=array();
			foreach($parts3 as $p){
				$para=explode(" ",$p);
				$para=remove_array_spaces($para);
				if (count($para)>1){
					$parameters[$para[1]]=$para[0];
				}
			}
			return new WSOperation($function_name,$parameters, $function_return_type);
		}
		
		function getTypes(){
			return $this->types;
		}
		
		function getOperations(){
			return $this->operations;
		}
		
		function getServiceName(){
			return "Calculator";
		}

		function getPrimitiveTypes($typeNames){
			if (is_array($typeNames)){
				$primitives=array();
				foreach($typeNames as $key=>$value){
					$baseTypes=$this->getPrimitiveTypesForType($value);
					if ($baseTypes==null){
						$primitives[$key]=$value;
					}else{
						foreach($baseTypes as $baseKey=>$baseValue){
							$primitives[$key.".".$baseKey]=$baseValue;
						}
					}	
				}
				return $primitives;
			}else{
				return $this->getPrimitiveTypesForType($typeNames);
			}
		}
		private function getPrimitiveTypesForType($typeName){
			$types=$this->getTypes();
			if (array_key_exists($typeName, $types)){
				$primitives=array();
				$baseTypes=$types[$typeName];
// 				var_dump($baseTypes);echo "<br />";
				foreach($baseTypes as $basekey=>$baseType){
					$rawPrimitives=$this->getPrimitiveTypes($baseType);
					if ($rawPrimitives==null){
						$primitives[$typeName.".".$basekey]=$baseType;
					}else{
						foreach($rawPrimitives as $key=>$value){
							$primitives[$typeName.".".$key]=$value;
						}
					}
				}
				return $primitives;
			}else{
				return null;
			}
		}
		
	}
	class WSOperation{
		function __construct($function_name,$parameters, $return_type){
			$this->function_name=$function_name;
			$this->parameters=$parameters;
			$this->return_type=$return_type;
		}
		
		public function toString(){
			$result=$this->function_name."$";
			foreach($this->parameters as $p){
				$result=$result.$p."_";
			}
			return $result;
		}
		
		function getFunctionName(){
			return $this->function_name;
		}
		
		function getParameters(){
			return $this->parameters;
		}
		
		function getReturnType(){
			return $this->return_type;
		}
	}
	function remove_array_spaces($arr){
		$i=count($arr)-1;
		while($i>-1){
			if (trim($arr[$i])==""){
				unset($arr[$i]);
			}
			$i-=1;
		}
		return array_values($arr);
	}

// 	$parser=new WSDLParser("http://localhost/poc/test/Calculator.wsdl");
// // 	foreach($parser->getTypes() as $type){
// // 		var_dump($type);
// // 		echo "<br />";
// // 	}
// 	foreach($parser->getOperations() as $operation){
// // 		var_dump($operation);
// // 		echo "<br />";
// 		foreach($operation->getParameters() as $key=>$value){
// 			var_dump($parser->getPrimitiveTypes($value));
// 			echo "<br />";
// 		}
// 	}
?>