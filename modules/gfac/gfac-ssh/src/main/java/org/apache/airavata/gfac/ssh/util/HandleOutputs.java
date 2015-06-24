package org.apache.airavata.gfac.ssh.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
			List<String> outputList = cluster.listDirectory(jobExecutionContext.getWorkingDir());
			boolean missingOutput = false;

			for (OutputDataObjectType output : outputs) {
				// FIXME: Validation of outputs based on required and optional and search based on REGEX provided in search.

				if (DataType.URI == output.getType()) {
                    // for failed jobs outputs are not generated. So we should not download outputs
                    if (GFacUtils.isFailedJob(jobExecutionContext)){
                       continue;
                    }
					String outputFile = output.getValue();
					String fileName = outputFile.substring(outputFile.lastIndexOf(File.separatorChar) + 1, outputFile.length());

					if (output.getLocation() == null && !outputList.contains(fileName) && output.isIsRequired()) {
						missingOutput = true;
					} else {
						cluster.scpFrom(outputFile, outputDataDir);
						String localFile = outputDataDir + File.separator + fileName;
						jobExecutionContext.addOutputFile(localFile);
						output.setValue(localFile);
						outputArray.add(output);
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
}
