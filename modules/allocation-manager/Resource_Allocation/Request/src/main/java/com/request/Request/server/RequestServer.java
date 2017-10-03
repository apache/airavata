package com.request.Request.server;

import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;

public class RequestServer {

 public static void StartsimpleServer(RequestResourceService.Processor<RequestResourceServiceHandler> processor) {
  try {
   TServerTransport serverTransport = new TServerSocket(9000);
   TServer server = new TSimpleServer(
     new Args(serverTransport).processor(processor));

   // Use this for a multithreaded server
   // TServer server = new TThreadPoolServer(new
   // TThreadPoolServer.Args(serverTransport).processor(processor));

   System.out.println("Starting the Resource request server...");
   server.serve();
  } catch (Exception e) {
   e.printStackTrace();
  }
 }
 
 public static void main(String[] args) {
  StartsimpleServer(new RequestResourceService.Processor<RequestResourceServiceHandler>(new RequestResourceServiceHandler()));
 }

}