package org.apache.airavata.gfac.provider.utils;

public enum SPMDVariations {

	MPI ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPI"), 
	GridMPI ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/GridMPI"),
	IntelMPI ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/IntelMPI"),
	LAMMPI ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/LAM-MPI"), 
	MPICH1 ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPICH1"),
	MPICH2 ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPICH2"),
	MPICHGM ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPICH-GM"),
	MPICHMX ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPICH-MX"),
	MVAPICH ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MVAPICH"),
	MVAPICH2 ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MVAPICH2"),
	OpenMPI ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/OpenMPI"),
	POE ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/POE"),
	PVM ("http://www.ogf.org/jsdl/2007/02/jsdl-spmd/PVM");
	
	private final String variation;
	
	private SPMDVariations(String variation) {
		this.variation = variation;
	}
	
	public String value(){
		return variation;
	}
	
}


