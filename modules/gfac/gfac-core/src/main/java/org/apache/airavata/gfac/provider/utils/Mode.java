package org.apache.airavata.gfac.provider.utils;

/**
 * file creation modes 
 */
public enum Mode {

	/**
	 * overwrite any existing file
	 */
	overwrite,
	
	/**
	 * append to an existing file
	 */
	append,
	
	/**
	 * do NOT overwrite and fail if the file exists
	 */
	nooverwrite
	
	
}