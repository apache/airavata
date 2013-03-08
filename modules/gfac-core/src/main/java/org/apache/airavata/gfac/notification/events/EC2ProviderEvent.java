package org.apache.airavata.gfac.notification.events;

public class EC2ProviderEvent extends GFacEvent {
    String statusMessage;

    public EC2ProviderEvent(String message){
        this.eventType = EC2ProviderEvent.class.getSimpleName();
        statusMessage = message;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
