import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.UUID;

public class RandomizedIntervalSimulator {

    private int simulatorDelays[] = {120};
    private String agentPath = "/Users/dwannipurage3/code/airavata/modules/agent-framework/airavata-agent/airavata-agent";
    private String agentServiceHost = "localhost:19900";
    private int totalJobs  = 20;

    private void runAgent(String agentId) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(agentPath, agentServiceHost, agentId);

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        int exitCode = process.waitFor();
        System.out.println("Process exited with code: " + exitCode + " for agent " + agentId);
    }

    private void runSimulator(int interval) throws Exception {

        Random random = new Random();
        int randomInterval = random.nextInt(interval + 1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Agent for interval " + interval + " starting in " + randomInterval);
                    Thread.sleep(randomInterval * 1000);
                    runAgent(interval + "-" + UUID.randomUUID().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        RandomizedIntervalSimulator simulator = new RandomizedIntervalSimulator();
        for (int simulatorDelay : simulator.simulatorDelays) {
            for (int i = 0; i < simulator.totalJobs; i ++) {
                System.out.println("Simulating job " + i + " of delay " + simulatorDelay);
                simulator.runSimulator(simulatorDelay);
            }
        }
    }



}
