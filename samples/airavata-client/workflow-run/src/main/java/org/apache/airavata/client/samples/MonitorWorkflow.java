package org.apache.airavata.client.samples;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.rest.client.PasswordCallbackImpl;
import org.apache.airavata.ws.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorWorkflow {
	private static final Logger log = LoggerFactory.getLogger(MonitorWorkflow.class);

	public static void monitor(final String experimentId) throws AiravataAPIInvocationException, URISyntaxException {
		PasswordCallback passwordCallback = new PasswordCallbackImpl(RunWorkflow.getUserName(),
				RunWorkflow.getPassword());
		AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(RunWorkflow.getRegistryURL()),
				RunWorkflow.getGatewayName(), RunWorkflow.getUserName(), passwordCallback);
		MonitorListener monitorListener = new MonitorListener();
		Monitor experimentMonitor = airavataAPI.getExecutionManager().getExperimentMonitor(experimentId,
				monitorListener);
		log.info("Started the Workflow monitor");
		experimentMonitor.startMonitoring();
	}
}
