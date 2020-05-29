package org.apache.airavata.ide.integration;

import org.apache.airavata.monitor.email.EmailBasedMonitor;
import org.apache.airavata.monitor.realtime.RealtimeMonitor;

public class JobMonitorStarter {
    public static void main(String args[]) throws Exception {
        System.out.println("Starting Email Monitor .......");

        EmailBasedMonitor emailBasedMonitor = new EmailBasedMonitor();
        emailBasedMonitor.startServer();

        System.out.println("Starting Realtime Monitor .......");
        RealtimeMonitor realtimeMonitor = new RealtimeMonitor();
        realtimeMonitor.startServer();

    }
}
