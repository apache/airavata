package org.apache.airavata.cloud.util;

/**
 * Enum for various cloud types.
 * @author Mangirish Wagle
 *
 */
public enum CloudRef {

	JETSTREAM("jetstream"),
	AMAZON("amazon"),
	COMET("comet");

	String cloudType;

	private CloudRef(String type) {
		this.cloudType = type;
	}

	@Override
	public String toString() {
		return this.cloudType;
	}
}
