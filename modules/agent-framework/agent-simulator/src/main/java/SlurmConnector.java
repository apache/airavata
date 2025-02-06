import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.*;

public class SlurmConnector {

    private String hostName;
    private String loginUserName;
    private String privateKeyPath;
    final SSHClient ssh = new SSHClient();

    public SlurmConnector(String hostName, String loginUserName, String privateKeyPath) {
        this.hostName = hostName;
        this.loginUserName = loginUserName;
        this.privateKeyPath = privateKeyPath;
    }

    private static final Console con = System.console();

    public void connect() throws Exception {
        ssh.loadKnownHosts();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(hostName, 22);
        ssh.authPublickey(loginUserName, privateKeyPath);
    }

    public String executeCommand(String command) throws Exception {
        Session session = ssh.startSession();
        final Session.Command cmd = session.exec(command);
        String output = new String(cmd.getInputStream().readAllBytes());
        session.close();
        return output;
    }

    public int submitAgentJob(String agentId) throws Exception {
        executeCommand("mkdir -p /home/ubuntu/jobs/" + agentId);
        executeCommand("cp /home/ubuntu/example.slurm /home/ubuntu/jobs/" +agentId + "/job.slurm");
        String commandOut = executeCommand("/opt/slurm/bin/sbatch /home/ubuntu/jobs/" + agentId + "/job.slurm");
        System.out.println(commandOut);
        return commandOut.startsWith("Submitted batch job ") ? Integer.parseInt(commandOut.trim().split(" ")[3]) : -1;
    }

    public static void main(String[] args) throws Exception {
        SlurmConnector slurmConnector = new SlurmConnector("54.184.248.109", "ubuntu",
                "/Users/dwannipurage3/.ssh/cs_rsa_pkce");
        slurmConnector.connect();
        int jobId = slurmConnector.submitAgentJob("agent1");
        System.out.println("Job id " + jobId);
    }
}
