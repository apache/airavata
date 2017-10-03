package com.request.Request.client;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.request.Request.server.RequestResourceService;

public class RequestResourceClient {

 public static void main(String[] args) {

  try {
   TTransport transport;

   transport = new TSocket("localhost", 9000);
   transport.open();

   TProtocol protocol = new TBinaryProtocol(transport);
   RequestResourceService.Client client = new RequestResourceService.Client(protocol);

   com.request.Request.server.Resource obj = new com.request.Request.server.Resource(1,"Resource needed","Madrina","for research");
   System.out.println(client.request(obj));
   
   com.request.Request.server.Resource obj2 = new com.request.Request.server.Resource(1,"Resource requested","John Doe","for hobby project");
   System.out.println(client.request(obj2));

   transport.close();
  } catch (TTransportException e) {
   e.printStackTrace();
  } catch (TException x) {
   x.printStackTrace();
  }
 }

}