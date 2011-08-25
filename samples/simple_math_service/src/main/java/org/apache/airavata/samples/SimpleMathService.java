package org.apache.airavata.samples;

public class SimpleMathService {
	/**
	 * 
	 * @param x
	 * @param y
	 * @return x plus y
	 */
    public int add(int x, int y) {
    	return x + y;
    }

    /**
     * 
     * @param x
     * @param y
     * @return x minus y
     */
    public int subtract(int x, int y) {
    	return x + y;
    }
    
    /**
     * 
     * @param x
     * @param y
     * @return x multiply y
     */
    public int multiply(int x, int y) {
    	return x * y;
    }
    
    /**
     * 
     * @param x
     * @param size
     * @return a string array with size contains x
     */
    public String[] stringArrayGenerate(String x, int size) {
    	String[] result = new String[size];
    	for (int i = 0; i < result.length; i++) {
			result[i] = x;
		}
    	return result;
    }
    
    /**
     * 
     * @param x
     * @param size
     * @return an integer array with size contains x
     */
    public int[] intArrayGenerate(int x, int size) {
    	int[] result = new int[size];
    	for (int i = 0; i < result.length; i++) {
			result[i] = x;
		}
    	return result;
    }
}
