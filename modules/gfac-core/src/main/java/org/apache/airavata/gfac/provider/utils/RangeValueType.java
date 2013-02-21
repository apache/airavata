package org.apache.airavata.gfac.provider.utils;


/**
 * The representation of range requirement types in JSDL.
 * 
 * @author Alexei Baklushin
 * @version $Id: RangeValueType.java,v 1.4 2008/06/06 11:15:01 hohmannc Exp $
 * @version $Id: RangeValueType.java,v 1.3 2006/08/10 09:31:54 lukichev Exp $
 * 
 */
public class RangeValueType implements ResourceRequirement {
	

    private double exact = Double.NaN;
    private double lowerBound = Double.NEGATIVE_INFINITY;
    private double upperBound = Double.POSITIVE_INFINITY;
    
    private double epsilon = Double.NaN;
    private boolean includeLowerBound = true;
    private boolean includeUpperBound = true;
    
    private boolean enabled = false;
    
    
    public RangeValueType(double exact, double epsilon, double lowerBound, boolean includeLowerBound, double upperBound, boolean includeUpperBound, boolean enabled) {
        this.exact = exact;
    	this.epsilon = epsilon;
    	this.lowerBound = lowerBound;
        this.includeLowerBound = includeLowerBound;
        this.upperBound = upperBound;
        this.includeUpperBound = includeUpperBound;
        this.enabled = enabled;
    }
   
    
    
    /**
	 * Create the range requirements
	 * 
	 * @param exact -
	 *            the exact value
	 * @param lowerBound -
	 *            the lower bound
	 * @param upperBound -
	 *            the upper bound
	 * @param includelowerBound -
	 *            true, if lowerBound should be included in range
	 * @param includeUpperBound -
	 *            true, if upperBound should be included in range
	 * 
	 */
    public RangeValueType(double exact, double epsilon, double lowerBound, boolean includeLowerBound, double upperBound, boolean includeUpperBound) {
    	this(exact,epsilon,lowerBound,includeLowerBound,upperBound,includeUpperBound,false);
        
    }
    
    
    /**
	 * Create the range requirements
	 * 
	 * @param exact -
	 *            the exact value
	 * @param lowerBound -
	 *            the lower bound
	 * @param upperBound -
	 *            the upper bound
	 */
    public RangeValueType(double exact, double epsilon, double lowerBound, double upperBound) {
    	this(exact,epsilon,lowerBound,true,upperBound,true);
    }
    
    
    public RangeValueType(double exact, double lowerBound, double upperBound) {
    	this(exact,Double.NaN,lowerBound,true,upperBound,true);
    }

    /**
	 * Create the exact requirements
	 * 
	 * @param exact -
	 *            the exact value
	 * @param epsilon -
	 *            the epsilon arround exact
	 * 
	 */
    public RangeValueType(double exact, double epsilon) {
        this(exact,epsilon,Double.NaN,Double.NaN);
    }

    
    /**
	 * Create the exact requirements
	 * 
	 * @param exact -
	 *            the exact value
	 */
    public RangeValueType(double exact) {
        this(exact,Double.NaN);
    }

    public RangeValueType() {
    }

    /**
	 * Get exact requirements
	 * 
	 * @return the exact requirements
	 */
    public double getExact() {
        return exact;
    }

    /**
	 * Set exact requirements
	 * 
	 * @param exact -
	 *            the exact requirements
	 */
    public void setExact(double exact) {
        this.exact = exact;
    }
    
    /**
	 * Get epsilon
	 * 
	 * @return the epsilon
	 */
    public double getEpsilon() {
        return epsilon;
    }

    /**
	 * Set epsilon
	 * 
	 * @param epsilon -
	 *            epsilon belonging to to exact requirements
	 */
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
	 * Get lower bound
	 * 
	 * @return the lower bound
	 */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
	 * Set lower bound
	 * 
	 * @param lowerBound -
	 *            the lower bound
	 */
    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
	 * Get upper bound
	 * 
	 * @return the upper bound
	 */
    public double getUpperBound() {
        return upperBound;
    }

    /**
	 * Set upper bound
	 * 
	 * @param upperBound -
	 *            the upper bound
	 */
    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    /**
	 * Test if requirements are met
	 * 
	 * @param value -
	 *            the tested value
	 * @return <code>true</code> if value is in the range and not less than
	 *         the exact value
	 */
    public boolean lowerThanDouble(double value) {
        return (value >= exact && value >= lowerBound && value <= upperBound) ? true : false;
    }

    public String toString() {
        if (lowerBound == Double.NEGATIVE_INFINITY && upperBound == Double.POSITIVE_INFINITY) {
            return Double.toString(exact);
        }
        else {
            return "(e=" + Double.toString(exact) + ",l=" + Double.toString(lowerBound) + ",u=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + Double.toString(upperBound) + ")"; //$NON-NLS-1$
        }
    }


	public boolean isIncludeLowerBound() {
		return includeLowerBound;
	}


	public void setIncludeLowerBound(boolean includeLowerBound) {
		this.includeLowerBound = includeLowerBound;
	}


	public boolean isIncludeUpperBound() {
		return includeUpperBound;
	}


	public void setIncludeUpperBound(boolean includeUpperBound) {
		this.includeUpperBound = includeUpperBound;
	}
	
	public RangeValueType clone(){
		return new RangeValueType(this.exact, this.epsilon, this.lowerBound, this.includeLowerBound, this.upperBound, this.includeUpperBound,this.enabled);
	}



	public boolean isEnabled() {
		return enabled;
	}



	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	
	public boolean equals(Object o)
	{
		if(! (o instanceof RangeValueType)) return false;
		RangeValueType other = (RangeValueType) o;
		return doublesEqual(getExact(),other.getExact())
		&& doublesEqual(getEpsilon(), other.getEpsilon())
		&& doublesEqual(getLowerBound(), other.getLowerBound())
		&& doublesEqual(getUpperBound(), other.getUpperBound())
		&& isIncludeLowerBound() == other.isIncludeLowerBound()
		&& isIncludeUpperBound() == other.isIncludeUpperBound()
		&& isEnabled() == other.isEnabled();
	}

	
	private boolean doublesEqual(double a, double b)
	{
		Double A = new Double(a);
		Double B = new Double(b);
		return A.equals(B);
	}
}
