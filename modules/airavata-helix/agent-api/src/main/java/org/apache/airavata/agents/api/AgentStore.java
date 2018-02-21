package org.apache.airavata.agents.api;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AgentStore {
    public AgentAdaptor fetchAdaptor(String computeResource, String Protocol, String authToken) throws AgentException {

        AgentData agentData = getAgentDataForComputeResource(computeResource);

        try {
            URL[] urls = new URL[1];
            urls[0] = new URL(agentData.getLibraryLocation());
            URLClassLoader classLoader = new URLClassLoader(urls, AgentAdaptor.class.getClassLoader());

            Class<?> clazz = classLoader.loadClass(agentData.getAdaptorClass());
            AgentAdaptor agentAdaptor = (AgentAdaptor) clazz.newInstance();

            Class<?> paramClazz = classLoader.loadClass(agentData.paramClass);
            AdaptorParams adaptorParams = (AdaptorParams) paramClazz.newInstance();

            Object paramsInit = adaptorParams.loadFromFile(new File(agentData.paramDataFile));

            //agentAdaptor.init(paramsInit);
            System.out.println("Done");

            return agentAdaptor;

        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException | IOException e) {
            e.printStackTrace();
            throw new AgentException("Failed to fetch agent adaptor for compute resource " + computeResource, e);
        }
    }

    public static void main(String args[]) throws InstantiationException, IOException, AgentException {
        AgentStore store = new AgentStore();

        AgentAdaptor agentAdaptor = store.fetchAdaptor("localhost", null, null);
        System.out.println("Agent loaded");
    }

    private AgentData getAgentDataForComputeResource(String computeResource) {
        if ("localhost".equals(computeResource)) {
            return new AgentData().setLibraryLocation("file:///Users/dimuthu/code/fork/airavata-sandbox/airavata-helix/modules/agent-impl/ssh-agent/target/ssh-agent-1.0-SNAPSHOT-jar-with-dependencies.jar")
                    .setAdaptorClass("org.apache.airavata.helix.agent.ssh.SshAgentAdaptor")
                    .setParamClass("org.apache.airavata.helix.agent.ssh.SshAdaptorParams")
                    .setParamDataFile("/tmp/ssh-param.json");
        }

        return null;
    }

    public static class AgentData {

        private String libraryLocation;
        private String adaptorClass;
        private String paramClass;
        private String paramDataFile;

        public String getLibraryLocation() {
            return libraryLocation;
        }

        public AgentData setLibraryLocation(String libraryLocation) {
            this.libraryLocation = libraryLocation;
            return this;
        }

        public String getAdaptorClass() {
            return adaptorClass;
        }

        public AgentData setAdaptorClass(String adaptorClass) {
            this.adaptorClass = adaptorClass;
            return this;
        }

        public String getParamClass() {
            return paramClass;
        }

        public AgentData setParamClass(String paramClass) {
            this.paramClass = paramClass;
            return this;
        }

        public String getParamDataFile() {
            return paramDataFile;
        }

        public AgentData setParamDataFile(String paramDataFile) {
            this.paramDataFile = paramDataFile;
            return this;
        }
    }
}
