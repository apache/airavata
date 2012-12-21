package org.apache.airavata.client.api;

/**
 * If a saving descriptor already found in the system this exception will be thrown.
 */
public class DescriptorRecordAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 1231L;

    public DescriptorRecordAlreadyExistsException(Throwable e) {
        super(e);
    }
    public DescriptorRecordAlreadyExistsException(String message) {
        super(message, null);
    }

    public DescriptorRecordAlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }

}
