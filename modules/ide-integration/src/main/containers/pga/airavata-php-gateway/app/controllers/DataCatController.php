<?php

class DataCatController extends BaseController
{

    public function select()
    {
        //FIXME
        $results = json_decode(file_get_contents('http://localhost:8000/query-api/select?q=sddslfnlsdf'), true);
        if(!isset($results) || empty($results)){
            $results = array();
        }
        return View::make('datacat/select', array("results" => $results));
    }

    public function summary()
    {
        //FIXME
        $result = json_decode(file_get_contents('http://localhost:8000/query-api/select?q=sddslfnlsdf'), true);
        return View::make('datacat/summary', array("result" => $result[1]));
    }

}
