<?php

class FilemanagerController extends BaseController
{
	public function __construct()
    {
        $this->beforeFilter('verifylogin');
        $this->beforeFilter('verifyauthorizeduser');
        Session::put("nav-active", "storage");
    }

    public function browse(){
		//FIXME check for no ../ parts in the path
		if(Input::has("path") && (0 == strpos(Input::get("path"), Session::get('username'))
				|| 0 == strpos(Input::get("path"), "/" . Session::get('username')))){
			$path = Input::get("path");
			if(0 === strpos($path, '/')){
				$path = substr($path, 1);
			}
		}else{
			$path = Session::get("username");
		}
		return View::make("files/browse",array("path"=>$path));
    }

	public function get(){
		if(Session::has('username') ){

			$path = Input::get('path');
			/*
			if( $path == null || (0 !== strpos($path, Session::get('username']))){
			    header('HTTP/1.0 403 Forbidden');
			}
			*/
			$DATA_ROOT = Config::get("pga_config.airavata")["experiment-data-absolute-path"];
			$data_path = $DATA_ROOT . "/" . $path;
			if (!file_exists( $data_path))
				echo FileManager::msg(False, "$path does not exist");

			if (is_dir( $data_path))
			    echo FileManager::get_content( $DATA_ROOT .  "/", $path);
			else
			    echo file_get_contents($data_path);
		}
	}
}
