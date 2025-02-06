import java.util.HashMap;
import java.util.Map;

public class ResourceScheduler {

    private String clusters[] = {"54.184.248.109"};
    private String loginUserName = "ubuntu";
    private String privateKey = "/Users/dwannipurage3/.ssh/cs_rsa_pkce";

    Map<String, SlurmConnector> slurmConnectors = new HashMap<>();

    public ResourceScheduler() {
        for (String cluster : clusters) {
            slurmConnectors.put(cluster, new SlurmConnector(cluster, loginUserName, privateKey));
        }
    }

    public void createBurst(int jobsPerCluster) throws Exception {
        for (int cluster = 0; cluster < clusters.length; ++cluster) {
            for (int i = 0; i < jobsPerCluster; i++) {
                String agentId = "cluster-" + cluster + "-Jobid" + i;
                SlurmConnector slurmConnector = slurmConnectors.get(clusters[cluster]);
                slurmConnector.submitAgentJob(agentId);
            }
        }
    }


}
