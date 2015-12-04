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

package org.apache.airavata.gfac.ssh.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To handle outputs of different data types
 * 
 */
public class HandleOutputs {
	private static final Logger log = LoggerFactory.getLogger(HandleOutputs.class);

	public static List<OutputDataObjectType> handleOutputs(JobExecutionContext jobExecutionContext, Cluster cluster) throws GFacHandlerException {
		List<OutputDataObjectType> outputArray = new ArrayList<OutputDataObjectType>();
		try {
            String outputDataDir = ServerSettings.getSetting(Constants.OUTPUT_DATA_DIR);
            if (outputDataDir == null || outputDataDir.equals("")){
                outputDataDir = File.separator + "tmp";
            }

            outputDataDir += File.separator + jobExecutionContext.getExperimentID();
			(new File(outputDataDir)).mkdirs();

			List<OutputDataObjectType> outputs = jobExecutionContext.getTaskData().getApplicationOutputs();
			List<String> outputList = cluster.listDirectory(jobExecutionContext.getWorkingDir(),true);
			boolean missingOutput = false;

			for (OutputDataObjectType output : outputs) {
				// FIXME: Validation of outputs based on required and optional
				// and search based on REGEX provided in search.

				if (DataType.URI == output.getType()) {
					// for failed jobs outputs are not generated. So we should
					// not download outputs
					if (GFacUtils.isFailedJob(jobExecutionContext)) {
						continue;
					}
					String outputFile = output.getValue();
					String fileName = outputFile.substring(outputFile.lastIndexOf(File.separatorChar) + 1, outputFile.length());
					if (output.getLocation() == null && !outputList.contains(fileName) && output.isIsRequired()) {
						missingOutput = true;
					}else {
					// if user value has any pattern char.
					Pattern p = Pattern.compile("[*:<>?\\|]", Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(outputFile);
					// boolean b = m.matches();
					boolean b = m.find();
					if (b) {
						if(fileName.isEmpty() && !output.getSearchQuery().isEmpty()){
							fileName = output.getSearchQuery();
						}
						if(fileName.isEmpty()){
							throw new GFacHandlerException("Define a output value or search query");
						}
						String fileregex = wildcardToRegex(fileName);
						log.info("Regex for: "+ fileName+ " is: " + fileregex);
						cluster.scpFrom(outputFile, outputDataDir);
						ListIterator<String> li = outputList.listIterator();
						while (li.hasNext()) {
							String next = li.next();
							if (Pattern.matches(fileregex, next)) {
								String localFile = outputDataDir + File.separator + next;
								jobExecutionContext.addOutputFile(localFile);
								output.setValue(localFile);
								outputArray.add(output);
								break;
							}
						}
					} else {
							cluster.scpFrom(outputFile, outputDataDir);
							String localFile = outputDataDir + File.separator + fileName;
							jobExecutionContext.addOutputFile(localFile);
							output.setValue(localFile);
							outputArray.add(output);
						}
					}
				} else if (DataType.STDOUT == output.getType()) {
					String downloadFile = jobExecutionContext.getStandardOutput();
					String fileName = downloadFile.substring(downloadFile.lastIndexOf(File.separatorChar) + 1, downloadFile.length());
					cluster.scpFrom(downloadFile, outputDataDir);
					String localFile = outputDataDir + File.separator + fileName;
					jobExecutionContext.addOutputFile(localFile);
					jobExecutionContext.setStandardOutput(localFile);
					output.setValue(localFile);
					outputArray.add(output);

				} else if (DataType.STDERR == output.getType()) {
					String downloadFile = jobExecutionContext.getStandardError();
					String fileName = downloadFile.substring(downloadFile.lastIndexOf(File.separatorChar) + 1, downloadFile.length());
					cluster.scpFrom(downloadFile, outputDataDir);
					String localFile = outputDataDir + File.separator + fileName;
					jobExecutionContext.addOutputFile(localFile);
					jobExecutionContext.setStandardError(localFile);
					output.setValue(localFile);
					outputArray.add(output);

				}
			}
			if (outputArray == null || outputArray.isEmpty()) {
				log.error("Empty Output returned from the Application, Double check the application and ApplicationDescriptor output Parameter Names");
				if (jobExecutionContext.getTaskData().getAdvancedOutputDataHandling() == null) {
					throw new GFacHandlerException("Empty Output returned from the Application, Double check the application"
							+ "and ApplicationDescriptor output Parameter Names");
				}
			}

			if (missingOutput) {
				String arrayString = Arrays.deepToString(outputArray.toArray());
				log.error(arrayString);
				throw new GFacHandlerException("Required output is missing");
			}
		} catch (Exception e) {
			throw new GFacHandlerException(e);
		}
		jobExecutionContext.getTaskData().setApplicationOutputs(outputArray);
		return outputArray;
	}
	public static String wildcardToRegex(String wildcard){
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                    // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }
}
