package org.apache.airavata.helix.adaptor.wrapper;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;

public class SSHClientWrapper extends SSHClient {

    public SSHClientWrapper() {
        super();
    }

    public SSHClientWrapper(Config config) {
        super(config);
    }

    private boolean errored = false;

    public boolean isErrored() {
        return errored;
    }

    public void setErrored(boolean errored) {
        this.errored = errored;
    }
}
