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
package org.apache.airavata.xbaya.provenance;

import org.apache.airavata.common.utils.Pair;
import org.apache.airavata.common.utils.XMLUtil;
import org.xmlpull.infoset.XmlElement;

import java.io.File;
import java.io.FileFilter;

public class ProvenanceReader {

    public String DEFAULT_LIBRARY_FOLDER_NAME = "provenance";

	public ProvenanceReader() {

	}

	public Object read(final String nodeName, Pair<String, String>[] inputs)
			throws Exception {

		File directory = new File(DEFAULT_LIBRARY_FOLDER_NAME);
		if (!directory.isDirectory()) {
			return null;
		}
		File[] componentMatchFiles = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return false;
				} else {
					String fileName = pathname.getName();
					return -1 != fileName.indexOf(nodeName);
				}
			}
		});

		for (File file : componentMatchFiles) {
			if (!file.isDirectory()) {
				XmlElement xml = XMLUtil.loadXML(file);
				XmlElement root = xml;
				XmlElement wsnode;

				XmlElement foreach;
				if (null != (wsnode = root.element("wsnode"))) {
					XmlElement inputElems = root.element("inputs");
					Iterable inputValElems = inputElems.children();
					for (Object object : inputValElems) {
						if (object instanceof XmlElement) {
							XmlElement inputElem = (XmlElement)object;
							for (Pair<String, String> pair : inputs) {
								String inputName = pair.getLeft();
								//issue "x".equals("CreateCoordinatePortType_createCoordinate_in_0")
								if(inputName.equals(inputElem.getName())){
									//found the input now check whether values are the same
									if(XMLUtil.isEqual(inputElem, XMLUtil.stringToXmlElement(pair.getRight()))){
										//match found return output
										XmlElement output = root.element("output");
										return output.children().iterator().next();

									}
								}

							}
						}
					}

				} else if (null != (foreach = root.element("foreach"))) {

				}

			}
		}

		return null;
	}
}
