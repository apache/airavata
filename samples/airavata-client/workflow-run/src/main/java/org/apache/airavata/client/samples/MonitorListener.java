package org.apache.airavata.client.samples;

import org.apache.airavata.ws.monitor.EventData;
import org.apache.airavata.ws.monitor.EventDataListener;
import org.apache.airavata.ws.monitor.EventDataRepository;
import org.apache.airavata.ws.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorListener implements EventDataListener {

	private static final Logger log = LoggerFactory.getLogger(MonitorListener.class);

	@Override
	public void notify(EventDataRepository eventDataRepo, EventData eventData) {
		log.info("ExperimentID: " + eventData.getExperimentID());
		log.info("Message: " + eventData.getMessage());
	}

	@Override
	public void setExperimentMonitor(Monitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitoringPreStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitoringPostStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitoringPreStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitoringPostStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFail(EventData failNotification) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCompletion(EventData completionNotification) {
		// TODO Auto-generated method stub

	}

}
