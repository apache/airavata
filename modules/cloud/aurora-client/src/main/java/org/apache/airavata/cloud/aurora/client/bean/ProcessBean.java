package org.apache.airavata.cloud.aurora.client.bean;

/**
 * The Class ProcessBean.
 */
public class ProcessBean {

	/** The name. */
	private String name;
	
	/** The cmd line. */
	private String cmdLine;
	
	/** The is daemon. */
	private boolean isDaemon;
	
	/** The is ephemeral. */
	private boolean isEphemeral;
	
	/** The is final. */
	private boolean isFinal;
	
	/** The max failures. */
	private int max_failures;
	
	/** The min duration. */
	private int min_duration;

	/**
	 * Instantiates a new process bean.
	 *
	 * @param name the name
	 * @param cmdLine the cmd line
	 * @param isDaemon the is daemon
	 */
	public ProcessBean(String name, String cmdLine, boolean isDaemon) {
		this.name = name;
		this.cmdLine = cmdLine;
		this.isDaemon = isDaemon;
		
		// set defaults
		this.isEphemeral = false;
		this.isFinal = false;
		this.max_failures = 1;
		this.min_duration = 5;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the cmd line.
	 *
	 * @return the cmd line
	 */
	public String getCmdLine() {
		return cmdLine;
	}

	/**
	 * Sets the cmd line.
	 *
	 * @param cmdLine the new cmd line
	 */
	public void setCmdLine(String cmdLine) {
		this.cmdLine = cmdLine;
	}

	/**
	 * Checks if is daemon.
	 *
	 * @return true, if is daemon
	 */
	public boolean isDaemon() {
		return isDaemon;
	}

	/**
	 * Sets the daemon.
	 *
	 * @param isDaemon the new daemon
	 */
	public void setDaemon(boolean isDaemon) {
		this.isDaemon = isDaemon;
	}

	/**
	 * Checks if is ephemeral.
	 *
	 * @return true, if is ephemeral
	 */
	public boolean isEphemeral() {
		return isEphemeral;
	}

	/**
	 * Sets the ephemeral.
	 *
	 * @param isEphemeral the new ephemeral
	 */
	public void setEphemeral(boolean isEphemeral) {
		this.isEphemeral = isEphemeral;
	}

	/**
	 * Checks if is final.
	 *
	 * @return true, if is final
	 */
	public boolean isFinal() {
		return isFinal;
	}

	/**
	 * Sets the final.
	 *
	 * @param isFinal the new final
	 */
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	/**
	 * Gets the max failures.
	 *
	 * @return the max failures
	 */
	public int getMax_failures() {
		return max_failures;
	}

	/**
	 * Sets the max failures.
	 *
	 * @param max_failures the new max failures
	 */
	public void setMax_failures(int max_failures) {
		this.max_failures = max_failures;
	}

	/**
	 * Gets the min duration.
	 *
	 * @return the min duration
	 */
	public int getMin_duration() {
		return min_duration;
	}

	/**
	 * Sets the min duration.
	 *
	 * @param min_duration the new min duration
	 */
	public void setMin_duration(int min_duration) {
		this.min_duration = min_duration;
	}
	
}
