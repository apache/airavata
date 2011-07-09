/**
 * StreamServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.1  Built on : Oct 19, 2009 (10:59:00 EDT)
 */

package org.apache.airavata.xbaya.streaming;

/**
 * StreamServiceCallbackHandler Callback class, Users can extend this class and implement their own receiveResult and
 * receiveError methods.
 */
public abstract class StreamServiceCallbackHandler {

    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking Web service call is finished and
     * appropriate method of this CallBack is called.
     * 
     * @param clientData
     *            Object mechanism by which the user can pass in user data that will be avilable at the time this
     *            callback is called.
     */
    public StreamServiceCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public StreamServiceCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */

    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for registerEPLWithInsert method override this method for handling normal
     * response from registerEPLWithInsert operation
     */
    public void receiveResultregisterEPLWithInsert(java.lang.String result) {
    }

    /**
     * auto generated Axis2 Error handler override this method for handling error response from registerEPLWithInsert
     * operation
     */
    public void receiveErrorregisterEPLWithInsert(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getQueueLength method override this method for handling normal response
     * from getQueueLength operation
     */
    public void receiveResultgetQueueLength(org.apache.airavata.xbaya.streaming.StreamServiceStub.QueueLength[] result) {
    }

    /**
     * auto generated Axis2 Error handler override this method for handling error response from getQueueLength operation
     */
    public void receiveErrorgetQueueLength(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for publishToStream method override this method for handling normal
     * response from publishToStream operation
     */
    public void receiveResultpublishToStream(java.lang.String result) {
    }

    /**
     * auto generated Axis2 Error handler override this method for handling error response from publishToStream
     * operation
     */
    public void receiveErrorpublishToStream(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for publish method override this method for handling normal response from
     * publish operation
     */
    public void receiveResultpublish(java.lang.String result) {
    }

    /**
     * auto generated Axis2 Error handler override this method for handling error response from publish operation
     */
    public void receiveErrorpublish(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getStreams method override this method for handling normal response
     * from getStreams operation
     */
    public void receiveResultgetStreams(org.apache.airavata.xbaya.streaming.StreamServiceStub.StreamDescription[] result) {
    }

    /**
     * auto generated Axis2 Error handler override this method for handling error response from getStreams operation
     */
    public void receiveErrorgetStreams(java.lang.Exception e) {
    }

}
