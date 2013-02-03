package org.apache.airavata.core.gfac.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactInfo {
    public static final Logger log = LoggerFactory.getLogger(ContactInfo.class);
    public String hostName;
    public int port;
    public static final int GSI_FTP_PORT = 2811;

    public ContactInfo(String hostName, int port) {
        if (port <= 0 || port == 80) {
            log.info(hostName + "port recived " + port + " setting it to " + GSI_FTP_PORT);
            port = GSI_FTP_PORT;
        }
        this.hostName = hostName;
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContactInfo) {
            return hostName.equals(((ContactInfo) obj).hostName) && port == ((ContactInfo) obj).port;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hostName.hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(hostName).append(":").append(port);
        return buf.toString();
    }
}

