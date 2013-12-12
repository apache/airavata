package org.apache.airavata.interpreter.service.handler;

import org.apache.airavata.experiment.execution.InterpreterService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class SimpleThirftServer {
    public static void StartsimpleServer(InterpreterService.Processor<InterpreterServiceHandler> processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(9090);
            TServer server = new TSimpleServer(
                    new TServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the simple server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        InterpreterService.Processor<InterpreterServiceHandler> processor = new InterpreterService.Processor<InterpreterServiceHandler>(new InterpreterServiceHandler());
        StartsimpleServer(processor);
    }
}
