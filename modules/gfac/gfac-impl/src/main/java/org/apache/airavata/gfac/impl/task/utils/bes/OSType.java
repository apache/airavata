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
package org.apache.airavata.gfac.impl.task.utils.bes;

public enum OSType {

	unknown("Unknown"), //$NON-NLS-1$
	linux("LINUX"), //$NON-NLS-1$
	mac_os("MACOS"), //$NON-NLS-1$
	win95("WIN95"), //$NON-NLS-1$
	win98("WIN98"), //$NON-NLS-1$
	windows_R_Me("Windows_R_Me"), //$NON-NLS-1$
	winNT("WINNT"), //$NON-NLS-1$
	windows_2000("Windows_2000"), //$NON-NLS-1$
	windows_XP("Windows_XP"), //$NON-NLS-1$
	msdos("MSDOS"), //$NON-NLS-1$
	solaris("Solaris"), //$NON-NLS-1$
	sunOS("SunOS"), //$NON-NLS-1$
	freeBSD("FreeBSD"), //$NON-NLS-1$
	netBSD("NetBSD"), //$NON-NLS-1$
	openBSD("OpenBSD"), //$NON-NLS-1$
	bsdunix("BSDUNIX"), //$NON-NLS-1$
	aix("AIX"), //$NON-NLS-1$
	z_OS("z_OS"), //$NON-NLS-1$
	os_2("OS_2"), //$NON-NLS-1$
	os9("OS9"), //$NON-NLS-1$
	netWare("NetWare"), //$NON-NLS-1$
	tru64_unix("Tru64_UNIX"), //$NON-NLS-1$
	irix("IRIX"), //$NON-NLS-1$
	osf("OSF"), //$NON-NLS-1$

	mvs("MVS"), //$NON-NLS-1$
	os400("OS400"), //$NON-NLS-1$
	javaVM("JavaVM"), //$NON-NLS-1$
	win3x("WIN3x"), //$NON-NLS-1$
	winCE("WINCE"), //$NON-NLS-1$
	NCR3000("NCR3000"), //$NON-NLS-1$
	dc_os("DC_OS"), //$NON-NLS-1$
	reliant_unix("Reliant_UNIX"), //$NON-NLS-1$
	sco_unixWare("SCO_UnixWare"), //$NON-NLS-1$
	sco_openServer("SCO_OpenServer"), //$NON-NLS-1$
	sequent("Sequent"), //$NON-NLS-1$
	u6000("U6000"), //$NON-NLS-1$
	aseries("ASERIES"), //$NON-NLS-1$
	tandemNSK("TandemNSK"), //$NON-NLS-1$
	tandemNT("TandemNT"), //$NON-NLS-1$
	bs2000("BS2000"), //$NON-NLS-1$
	lynx("Lynx"), //$NON-NLS-1$
	xenix("XENIX"), //$NON-NLS-1$
	vm("VM"), //$NON-NLS-1$
	interactive_unix("Interactive_UNIX"), //$NON-NLS-1$
	gnu_hurd("GNU_Hurd"), //$NON-NLS-1$
	mach_kernel("MACH_Kernel"), //$NON-NLS-1$
	inferno("Inferno"), //$NON-NLS-1$
	qnx("QNX"), //$NON-NLS-1$
	epoc("EPOC"), //$NON-NLS-1$
	ixWorks("IxWorks"), //$NON-NLS-1$
	vxWorks("VxWorks"), //$NON-NLS-1$
	mint("MiNT"), //$NON-NLS-1$
	beOS("BeOS"), //$NON-NLS-1$
	hp_mpe("HP_MPE"), //$NON-NLS-1$
	nextStep("NextStep"), //$NON-NLS-1$
	palmPilot("PalmPilot"), //$NON-NLS-1$
	rhapsody("Rhapsody"), //$NON-NLS-1$
	dedicated("Dedicated"), //$NON-NLS-1$
	os_390("OS_390"), //$NON-NLS-1$
	vse("VSE"), //$NON-NLS-1$
	tpf("TPF"), //$NON-NLS-1$
	caldera_open_unix("Caldera_Open_UNIX"), //$NON-NLS-1$
	attunix("ATTUNIX"), //$NON-NLS-1$
	dgux("DGUX"), //$NON-NLS-1$
	decnt("DECNT"), //$NON-NLS-1$
	openVMS("OpenVMS"), //$NON-NLS-1$
	hpux("HPUX"), //$NON-NLS-1$
	other("other"); //$NON-NLS-1$


	private OSType(String value) { 
		this.value = value;
	}

	private final String value;

	public String getValue() { 
		return value;
	}

	public static OSType fromString(String value)
	{
		for(OSType type : values())
		{
			if(type.value.equals(value))
			{
				return type;
			}
		}
		return null;
	}
	
	public String toString()
	{
		return value;
	}

}
