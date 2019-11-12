package org.apache.airavata.tools.load;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.commons.cli.*;
import org.keycloak.authorization.client.AuthzClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;

public class LoadClient {

    private String privateKeyFile = System.getProperty("user.home") + "/.ssh/id_rsa";
    private String publicKeyFile = System.getProperty("user.home") + "/.ssh/id_rsa.pub";
    private String passPhrase = null;
    private String configFile;

    private SecurityManager securityManager = new SecurityManager();
    private Map<String, StorageResourceManager> storageResourceManagerStore = new HashMap<>();
    private Configurations configurations;

    public void init() throws Exception {

        if (configFile == null) {
            try (InputStream in = LoadClient.class.getResourceAsStream("/conf/load-config.yml")) {
                Yaml yaml = new Yaml();
                configurations = yaml.loadAs(in, Configurations.class);
            }
        } else {
            try (InputStream in = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                configurations = yaml.loadAs(in, Configurations.class);
            }
        }

        // Making sure that all authzkeys are loaded
        for (Configuration cfg : configurations.getConfigurations()) {
            cfg.getAuthzToken();
        }

        securityManager.loadCertificate(configurations.getApiHost(), configurations.getApiPort());
        createStorageResourceManagers(configurations);
    }

    public void start() throws Exception {
        for (Configuration configuration : configurations.getConfigurations()) {

            UnitLoad unitLoad = new UnitLoad(configurations.getApiHost(), configurations.getApiPort(),
                    securityManager.getTrustStorePath(), securityManager.getTrustStorePassword(),
                    storageResourceManagerStore.get(configuration.getStorageResourceId()), configuration.getAuthzToken());

            StatusMonitor statusMonitor = new StatusMonitor(configurations.getApiHost(), configurations.getApiPort(),
                    securityManager.getTrustStorePath(), securityManager.getTrustStorePassword(), configuration.getAuthzToken());

            CompletionService<List<String>> completion = unitLoad.execute(configuration);

            List<String> allExperiments = new ArrayList<>();

            for (int i = 0; i < configuration.getConcurrentUsers(); i++) {
                Future<List<String>> experimentsPerUser = completion.take();
                allExperiments.addAll(experimentsPerUser.get());
            }
            System.out.println("All experiments " );
            System.out.println(allExperiments);
            statusMonitor.monitorExperiments(allExperiments);
        }
        destroyStorageResourceManagers();
        System.out.println("Finished load ");
        System.exit(0);
    }

    private void createStorageResourceManagers(Configurations configurations) throws Exception {


        Airavata.Client airavataClient = AiravataClientFactory.createAiravataSecureClient(configurations.getApiHost(), configurations.getApiPort(),
                securityManager.getTrustStorePath(), securityManager.getTrustStorePassword(), 100000);

        for (Configuration configuration : configurations.getConfigurations()) {
            String storageResourceId = configuration.getStorageResourceId();

            if (!storageResourceManagerStore.containsKey(storageResourceId)) {
                StorageResourceDescription storageResource = airavataClient
                        .getStorageResource(configuration.getAuthzToken(), storageResourceId);

                StoragePreference gatewayStoragePreference = airavataClient
                        .getGatewayStoragePreference(configuration.getAuthzToken(),
                                configuration.getGatewayId(), storageResourceId);

                StorageResourceManager storageResourceManager = new StorageResourceManager(gatewayStoragePreference,
                        storageResource, privateKeyFile, publicKeyFile, passPhrase);
                storageResourceManager.init();

                storageResourceManagerStore.put(storageResourceId, storageResourceManager);
            }
        }
    }

    private void destroyStorageResourceManagers() {
        storageResourceManagerStore.values().forEach(StorageResourceManager::destroy);
    }

    public static void main(String args[]) throws Exception {

        Options options = new Options();
        options.addOption("config", true, "Load configuration file in yaml format");
        options.addOption("apiHost", true, "API Server host name");
        options.addOption("apiPort", true, "API Server port");
        options.addOption("privateKeyPath", true, "SSH private key path to communicate with storage resources (Defaults to user private key in ~/.ssh/id_rsa)");
        options.addOption("publicKeyPath", true, "SSH public key path to communicate with storage resources (Defaults to user public key in ~/.ssh/id_rsa.pub)");
        options.addOption("passPhrase", true, "SSH private key pass phrase (if any)");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse( options, args);

        LoadClient loadClient = new LoadClient();

        if (cmd.hasOption("config")) {
            loadClient.configFile = cmd.getOptionValue("config");
        } else {
            System.out.println("Error : Load config file should be specified");
            System.exit(0);
        }

        if (cmd.hasOption("privateKeyPath")) {
            loadClient.privateKeyFile = cmd.getOptionValue("privateKeyPath");
        } else {
            System.out.println("Using default private key file " + loadClient.privateKeyFile);
        }

        if (cmd.hasOption("publicKeyPath")) {
            loadClient.publicKeyFile = cmd.getOptionValue("publicKeyPath");
        } else {
            System.out.println("Using default public key file " + loadClient.publicKeyFile);
        }

        if (cmd.hasOption("passPhrase")) {
            loadClient.passPhrase = cmd.getOptionValue("passPhrase");
        }

        loadClient.init();
        loadClient.start();
    }
}
