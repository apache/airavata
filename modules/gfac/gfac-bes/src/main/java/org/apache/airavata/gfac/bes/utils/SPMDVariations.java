/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.gfac.bes.utils;

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


