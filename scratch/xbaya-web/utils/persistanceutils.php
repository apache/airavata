<?php

function getProfiles(){
	return unserialize(file_get_contents("profiles.db"));
}

function getComponents(){
	return unserialize(file_get_contents("static_components.db"));
}


?>