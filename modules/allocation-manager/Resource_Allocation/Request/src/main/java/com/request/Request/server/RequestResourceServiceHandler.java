package com.request.Request.server;


import org.apache.thrift.TException;

public class RequestResourceServiceHandler implements RequestResourceService.Iface {

 public String request(Resource res) throws TException {
  return res.content + " " + res.purpose + ". Requested by: " + res.requestBy;
 }

}
