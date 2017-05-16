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
package org.apache.airavata.workflow.model.xsd;

public class LeadCrosscutXSD {

    public static final String XSD = "<schema\n"
            + "    targetNamespace='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'\n"
            + "    xmlns:lead='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'\n"
            + "    xmlns='http://www.w3.org/2001/XMLSchema'\n" + "    xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n"
            + "    attributeFormDefault='qualified'\n" + "    elementFormDefault='qualified'>\n"
            + "    <complexType name='LeadCrosscutParameters'>\n" + "      <annotation><documentation xml:lang='en'>\n"
            + "        List of elements from crosscut namespace (and others)\n"
            + "        Note: boundary box is define by by four points of forecast grid\n"
            + "        (ctrlon-nx*dx,ctrlat-ny*dy,ctrlon+nx*dx,ctrlat+ny*dy)\n"
            + "        or simple corners of rectangle\n" + "        (westbc,southbc,eastbc,northbc)\n"
            + "        and both are always the same values i.e.\n" + "        westbc := ctrlon-nx*dx\n"
            + "      </documentation></annotation>\n" + "        <sequence>\n"
            + "          <any namespace='##any' processContents='lax' minOccurs='0' maxOccurs='unbounded' />\n"
            + "        </sequence>\n" + "    </complexType>\n" + "     <element name='nx' type='int'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "         Number of steps east or west.\n"
            + "      </documentation></annotation>\n" + "    </element>\n" + "    <element name='ny' type='int'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "         Number of steps north or south.\n"
            + "      </documentation></annotation>\n" + "    </element>\n" + "    <element name='nz' type='int'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "         Number of steps going up.\n"
            + "      </documentation></annotation>\n" + "    </element>\n" + "    <element name='dx' type='int'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "        Size of step (in meters).\n"
            + "      </documentation></annotation>\n" + "    </element>\n" + "    <element name='dy' type='int'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "        Size of step (in meters).\n"
            + "      </documentation></annotation>\n" + "    </element>\n" + "    <element name='dz' type='int'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "        Size of step (in meters).\n"
            + "      </documentation></annotation>\n" + "    </element>\n"
            + "    <element name='ctrlon' type='float'>\n" + "      <annotation><documentation xml:lang='en'>\n"
            + "        Center Longitude (-180 ... 180)\n" + "      </documentation></annotation>\n"
            + "    </element>\n" + "    <element name='ctrlat' type='float'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "        Center Latitude (-90 ... +90)\n"
            + "      </documentation></annotation>\n" + "    </element>\n"
            + "    <element name='westbc' type='float'>\n" + "      <annotation><documentation xml:lang='en'>\n"
            + "        Longitude of left side of bounding box (-180..180).  This is also the 'westbc'\n"
            + "        element in the LEAD Metadata Schema.\n" + "      </documentation></annotation>\n"
            + "    </element>\n" + "    <element name='eastbc' type='float'>\n"
            + "      <annotation><documentation xml:lang='en'>\n"
            + "        Longitude of right side of bounding box (-180..180).  This is also the 'eastbc'\n"
            + "        element in the LEAD Metadata Schema.\n" + "      </documentation></annotation>\n"
            + "    </element>\n" + "    <element name='southbc' type='float'>\n"
            + "      <annotation><documentation xml:lang='en'>\n"
            + "        Latitude of bottom side of bounding box (-90..90).  This is also the 'southbc'\n"
            + "        element in the LEAD Metadata Schema.\n" + "      </documentation></annotation>\n"
            + "    </element>\n" + "    <element name='northbc' type='float'>\n"
            + "      <annotation><documentation xml:lang='en'>\n"
            + "        Latitude of top side of bounding box (-90..90).  This is also the 'northbc'\n"
            + "        element in the LEAD Metadata Schema.\n" + "      </documentation></annotation>\n"
            + "    </element>\n" + "    <element name='fcst_time' type='float'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "       Duration of the forecast in hours\n"
            + "      </documentation></annotation>\n" + "    </element>\n"
            + "    <element name='mapproj' type='int'>\n" + "      <annotation><documentation xml:lang='en'>\n" + "\n"
            + "      </documentation></annotation>\n" + "    </element>\n"
            + "    <element name='trulat1' type='float'>\n" + "      <annotation><documentation xml:lang='en'>\n"
            + "        ???\n" + "      </documentation></annotation>\n" + "    </element>\n"
            + "    <element name='trulat2' type='float'>\n" + "      <annotation><documentation xml:lang='en'>\n"
            + "        ???\n" + "      </documentation></annotation>\n" + "    </element>\n"
            + "    <element name='sclfct' type='float'>\n" + "      <annotation><documentation xml:lang='en'>\n"
            + "        sclfct is a map projection scale factor, its not needed as of now\n"
            + "      </documentation></annotation>\n" + "    </element>\n"
            + "    <element name='use_latest' type='boolean'>\n" + "      <annotation><documentation xml:lang='en'>\n"
            + "        Set to true if latest data should be used, otherwise consult the start_date\n"
            + "        and start_time fields to determine which dataset(s) to use.\n"
            + "      </documentation></annotation>\n" + "    </element>\n"
            + "   <element name='start_date' type='string'>\n" + "      <annotation><documentation xml:lang='en'>\n"
            + "        Start date is in YYYY/MM/DD format.\n" + "      </documentation></annotation>\n"
            + "    </element>\n" + "    <element name='start_hour' type='int'>\n"
            + "      <annotation><documentation xml:lang='en'>\n" + "        Hour on which to start forecast.\n"
            + "      </documentation></annotation>\n" + "    </element>\n"
            + "    <element name='user_modified_namelists' type='boolean'></element>\n"
            + "   <element name='mp_physics'>\n" + "    	<simpleType>\n" + "    		<restriction base='int'>\n"
            + "    			<enumeration value='0'></enumeration>\n" + "    			<enumeration value='1'></enumeration>\n"
            + "    			<enumeration value='2'></enumeration>\n" + "    			<enumeration value='3'></enumeration>\n"
            + "    			<enumeration value='4'></enumeration>\n" + "    			<enumeration value='5'></enumeration>\n"
            + "    			<enumeration value='6'></enumeration>\n" + "    			<enumeration value='8'></enumeration>\n"
            + "    			<enumeration value='9'></enumeration>\n" + "    			<enumeration value='98'></enumeration>\n"
            + "    			<enumeration value='99'></enumeration>\n" + "    		</restriction>\n" + "    	</simpleType>\n"
            + "    </element>\n" + "     <element name='ra_lw_physics'>\n" + "    	<simpleType>\n"
            + "    		<restriction base='int'>\n" + "    			<enumeration value='0'></enumeration>\n"
            + "    			<enumeration value='1'></enumeration>\n" + "    			<enumeration value='3'></enumeration>\n"
            + "    			<enumeration value='99'></enumeration>\n" + "    		</restriction>\n" + "    	</simpleType>\n"
            + "    </element>\n" + "    <element name='ra_sw_physics'>\n" + "    	<simpleType>\n"
            + "    		<restriction base='int'>\n" + "    			<enumeration value='0'></enumeration>\n"
            + "    			<enumeration value='1'></enumeration>\n" + "    			<enumeration value='2'></enumeration>\n"
            + "    			<enumeration value='3'></enumeration>\n" + "    			<enumeration value='99'></enumeration>\n"
            + "    		</restriction>\n" + "    	</simpleType>\n" + "    </element>\n"
            + "    <element name='radt' type='float'></element>\n" + "    <element name='sf_sfclay_physics'>\n"
            + "    	<simpleType>\n" + "    		<restriction base='int'>\n"
            + "    			<enumeration value='0'></enumeration>\n" + "    			<enumeration value='1'></enumeration>\n"
            + "    			<enumeration value='2'></enumeration>\n" + "    			<enumeration value='3'></enumeration>\n"
            + "    		</restriction>\n" + "    	</simpleType>\n" + "    </element>\n"
            + "    <element name='sf_surface_physics'>\n" + "    	<simpleType>\n"
            + "    		<restriction base='string'>\n" + "    			<enumeration value='0'></enumeration>\n"
            + "    			<enumeration value='1'></enumeration>\n" + "    			<enumeration value='2'></enumeration>\n"
            + "    			<enumeration value='3'></enumeration>\n" + "    		</restriction>\n" + "    	</simpleType>\n"
            + "    </element>\n" + "    <element name='cu_physics'>\n" + "    	<simpleType>\n"
            + "    		<restriction base='int'>\n" + "    			<enumeration value='0'></enumeration>\n"
            + "    			<enumeration value='1'></enumeration>\n" + "    			<enumeration value='2'></enumeration>\n"
            + "    			<enumeration value='3'></enumeration>\n" + "    			<enumeration value='4'></enumeration>\n"
            + "    			<enumeration value='99'></enumeration>\n" + "    		</restriction>\n" + "    	</simpleType>\n"
            + "    </element>\n" + "    <element name='cudt' type='float'></element>\n"
            + "    <element name='ifsnow'>\n" + "    	<simpleType>\n" + "    		<restriction base='int'>\n"
            + "    			<enumeration value='0'></enumeration>\n" + "    			<enumeration value='1'></enumeration>\n"
            + "    		</restriction>\n" + "    	</simpleType>\n" + "    </element>\n"
            + "    <element name='w_damping'>\n" + "    	<simpleType>\n" + "    		<restriction base='int'>\n"
            + "    			<enumeration value='0'></enumeration>\n" + "    			<enumeration value='1'></enumeration>\n"
            + "    		</restriction>\n" + "    	</simpleType>\n" + "    </element>\n"
            + "    <element name='diff_opt'>\n" + "    	<simpleType>\n" + "    		<restriction base='int'>\n"
            + "    			<enumeration value='0'></enumeration>\n" + "    			<enumeration value='1'></enumeration>\n"
            + "    			<enumeration value='2'></enumeration>\n" + "    		</restriction>\n" + "    	</simpleType>\n"
            + "    </element>\n" + "    <element name='km_opt'>\n" + "    	<simpleType>\n"
            + "    		<restriction base='int'>\n" + "    			<enumeration value='1'></enumeration>\n"
            + "    			<enumeration value='2'></enumeration>\n" + "    			<enumeration value='3'></enumeration>\n"
            + "    			<enumeration value='4'></enumeration>\n" + "    		</restriction>\n" + "    	</simpleType>\n"
            + "    </element>\n" + "    <element name='bl_pbl_physics'>\n" + "    	<simpleType>\n"
            + "    		<restriction base='int'>\n" + "    			<enumeration value='1'></enumeration>\n"
            + "    			<enumeration value='2'></enumeration>\n" + "    			<enumeration value='3'></enumeration>\n"
            + "    			<enumeration value='99'></enumeration>\n" + "    		</restriction>\n" + "    	</simpleType>\n"
            + "    </element>\n" + "    <element name='base_temp' type='float'></element>\n"
            + "    <element name='khdif' type='float'></element>\n"
            + "    <element name='kvdif' type='float'></element>\n" + "</schema>\n";

    /**
     * @return
     */
    public static String getXml() {
        return XSD;
    }

}