/*
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
 *
*/
package org.apache.airavata.xbaya.interpretor;

import org.apache.airavata.common.utils.XMLUtil;
import org.xmlpull.infoset.XmlElement;

import java.io.File;
import java.io.IOException;

public class ProvenanceFileWrite {


	private String file;
	private String folder;

	public ProvenanceFileWrite(String folder, String file){
		this.file = file;
		this.folder = folder;
	}

	/**
	 * @param elem
	 */
	public void write(XmlElement elem) {

		try {
			System.out.println(xsul5.XmlConstants.BUILDER.serializeToString(elem));
			XMLUtil.saveXML(elem, new File(new File(this.folder), this.file));
		} catch (IOException e) {
			//failing to write
		}
	}
}
