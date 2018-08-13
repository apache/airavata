package org.apache.airavata.helix.agent.dav;

public class DavConfig {
    private String userName;
    private String password;
    private String serverName;
    private boolean useHTTPS;
    private int port;
    private String basepath;

    DavConfig(String serverName, boolean useHTTPS, int port, String userName, String password, String basepath) {
        this.userName = userName;
        this.password = password;
        this.serverName = serverName;
        this.useHTTPS = useHTTPS;
        this.port = port;
        this.basepath = basepath;
    }

    /**
     * @return the userName
     */
    public String getUserName() {

        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {

        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverName the serverName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @return the useHTTPS
     */
    public boolean isUseHTTPS() {
        return useHTTPS;
    }

    /**
     * @param useHTTPS the useHTTPS to set
     */
    public void setUseHTTPS(boolean useHTTPS) {
        this.useHTTPS = useHTTPS;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     *
     * @param basepath
     */
    public void setBasepath(String basepath) {
        this.basepath = basepath;
    }

    /**
     *
     * @return basepath
     */
    public String getBasepath() {
        return basepath;
    }
}
