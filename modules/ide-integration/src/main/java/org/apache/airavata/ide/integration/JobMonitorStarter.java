package org.apache.airavata.ide.integration;

import org.apache.airavata.monitor.email.EmailBasedMonitor;

public class JobMonitorStarter {
    public static void main(String args[]) throws Exception {
        EmailBasedMonitor emailBasedMonitor = new EmailBasedMonitor();
        emailBasedMonitor.startServer();
    }
}
