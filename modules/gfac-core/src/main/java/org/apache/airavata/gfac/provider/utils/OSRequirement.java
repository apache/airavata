package org.apache.airavata.gfac.provider.utils;


/**
 * 
 * The requirements for target operating system.
 * 
 * @author Alexei Baklushin
 * @version $Id: OperatingSystemRequirementsType.java,v 1.7 2006/08/28 07:49:07 baklushin Exp $
 * 
 */
public class OSRequirement implements ResourceRequirement{
    private OSType osType;
    private String version;
    protected boolean enabled;
    
    
    public OSRequirement() {
    }

    /**
     * 
     * @param type -
     *            the type of the O/S
     * @param version -
     *            the version of the O/S
     */
    public OSRequirement(OSType osType, String osVersion) {
        setOSType(osType);
        setOSVersion(osVersion);
    }

    /**
     * Set the type of the O/S
     * 
     * @param type -
     *            the type of the O/S
     */
    public void setOSType(OSType osType) {
        this.osType = osType;
    }

    /**
     * Get the type of the O/S
     * 
     * @return the type of the O/S
     */
    public OSType getOSType() {
        return osType;
    }

    /**
     * Set the version of the O/S
     * 
     * @param version -
     *            the version of the O/S
     */
    public void setOSVersion(String version) {
        this.version = version;
    }

    /**
     * Get the version of the O/S
     * 
     * @return the version of the O/S
     */
    public String getOSVersion() {
        return version;
    }

    /**
     * 
     * equals this instance of class with another instance
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj==null || getClass() != obj.getClass()) return false;
        final OSRequirement other = (OSRequirement) obj;
        boolean typeEqual = osType == null ? other.osType == null : osType.equals(other.osType);
        boolean versionEqual = version == null ? other.version == null : version.equals(other.version);
        return typeEqual && versionEqual && isEnabled() == other.isEnabled();
    }



	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}