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

import de.fzj.unicore.uas.client.FileTransferClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.UFTPConstants;
import de.fzj.unicore.uas.client.UFTPFileTransferClient;
import de.fzj.unicore.uas.fts.FiletransferOptions.IMonitorable;
import de.fzj.unicore.uas.fts.FiletransferOptions.SupportsPartialRead;
import org.unigrids.services.atomic.types.GridFileType;
import org.unigrids.services.atomic.types.ProtocolType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * helper that exports remote files from a UNICORE Storage 
 * to the local client machine.<br/>
 * Simple wildcards ("*" and "?") and download of 
 * directories are supported.
 * 
 * TODO this should be refactored so the single-file download logic 
 * is separated from the wildcard/directory/provided outputStream logic
 * 
 * @author schuller
 */
public class FileDownloader extends FileTransferBase{

	private boolean showProgress=true;

	private boolean forceFileOnly=false;
	
	private OutputStream targetStream=null;
	
	public FileDownloader(String from, String to, Mode mode){
		this(from,to,mode,true);
	}
	
	public FileDownloader(String from, String to, Mode mode, boolean failOnError){
		this.to=to;
		this.from=from;
		this.mode=mode;
		this.failOnError=failOnError;
	}
	
	public void perform(StorageClient sms)throws Exception{
		boolean isWildcard=hasWildCards(from);
		boolean isDirectory=false;
		GridFileType gridSource=null;
		if(isWildcard){
			performWildCardExport(sms);
		}
		else {
			//check if source is a directory
			gridSource=sms.listProperties(from);
			isDirectory=gridSource.getIsDirectory();
			if(isDirectory){
				if(forceFileOnly){
					throw new IOException("Source is a directory");
				}
				performDirectoryExport(gridSource, new File(to), sms);
			}
			else{
				download(gridSource,new File(to),sms);
			}
		}	
	}
	
	protected void performDirectoryExport(GridFileType directory, File targetDirectory, StorageClient sms)throws Exception{
		if(!targetDirectory.exists()|| !targetDirectory.canWrite()){
			throw new IOException("Target directory <"+to+"> does not exist or is not writable!");
		}
		if(!targetDirectory.isDirectory()){
			throw new IOException("Target <"+to+"> is not a directory!");
		}
		GridFileType[]gridFiles=sms.listDirectory(directory.getPath());
		for(GridFileType file: gridFiles){
			if(file.getIsDirectory()){
				if(!recurse) {
					System.out.println("Skipping directory "+file.getPath());
					continue;
				}
				else{
					File newTargetDirectory=new File(targetDirectory,getName(file.getPath()));
					boolean success=newTargetDirectory.mkdirs();
					if(!success)throw new IOException("Can create directory: "+newTargetDirectory.getAbsolutePath());
					performDirectoryExport(file, newTargetDirectory, sms);
					continue;
				}
			}
			download(file, new File(targetDirectory,getName(file.getPath())), sms);
		}
	}
	
	protected void performWildCardExport(StorageClient sms)throws Exception{
		String dir=getDir(from);
		if(dir==null)dir="/";
		GridFileType[] files=sms.find(dir, false, from, false, null, null);
		File targetDir=targetStream==null?new File(to):null;
		if(targetStream==null){
			if(!targetDir.isDirectory())throw new IOException("Target is not a directory.");
		}
		for(GridFileType f: files){
			download(f, targetDir, sms);
		}
	}	
	
	private String getDir(String path){
		return new File(path).getParent();
	}
	
	private String getName(String path){
		return new File(path).getName();
	}
	
	/**
	 * download a single regular file
	 * 
	 * @param source - grid file descriptor
	 * @param localFile - local file or directory to write to
	 * @param sms
	 * @throws Exception
	 */
	private void download(GridFileType source, File localFile, StorageClient sms)throws Exception{
		if(source==null || source.getIsDirectory()){
			throw new IllegalStateException("Source="+source); 
		}
		
		OutputStream os=targetStream!=null?targetStream:null;
		FileTransferClient ftc=null;
		try{
			String path=source.getPath();
			if(targetStream==null){
				if(localFile.isDirectory()){
					localFile=new File(localFile,getName(source.getPath()));
				}
				if(mode.equals(Mode.nooverwrite) && localFile.exists()){
					System.out.println("File exists and creation mode was set to 'nooverwrite'.");
					return; 
				}
				System.out.println("Downloading remote file '"+sms.getUrl()+"#/"+path+"' -> "+localFile.getAbsolutePath());
				os=new FileOutputStream(localFile.getAbsolutePath(), mode.equals(Mode.append));
			}
			
			chosenProtocol=sms.findSupportedProtocol(preferredProtocols.toArray(new ProtocolType.Enum[preferredProtocols.size()]));
			Map<String,String>extraParameters=makeExtraParameters(chosenProtocol);
			ftc=sms.getExport(path,extraParameters,chosenProtocol);
			configure(ftc, extraParameters);
			System.out.println("DEB:File transfer URL : "+ftc.getUrl());
//			ProgressBar p=null;
			if(ftc instanceof IMonitorable  && showProgress){
				long size=ftc.getSourceFileSize();
				if(isRange()){
					size=getRangeSize();
				}
//				p=new ProgressBar(localFile.getName(),size,msg);
//				((IMonitorable) ftc).setProgressListener(p);
			}
			long startTime=System.currentTimeMillis();
			if(isRange()){
				if(!(ftc instanceof SupportsPartialRead)){
					throw new Exception("Byte range is defined but protocol does not allow " +
							"partial read! Please choose a different protocol!");
				}
				System.out.println("Byte range: "+startByte+" - "+(getRangeSize()>0?endByte:""));
				SupportsPartialRead pReader=(SupportsPartialRead)ftc;
				pReader.readPartial(startByte, endByte-startByte+1, os);
			}
			else{
				ftc.readAllData(os);
			}
//			if(p!=null){
//				p.finish();
//			}
			if(timing){
				long duration=System.currentTimeMillis()-startTime;
				double rate=(double)localFile.length()/(double)duration;
				System.out.println("Rate: " +rate+ " kB/sec.");
			}
			if(targetStream==null)copyProperties(source, localFile);
		}
		finally{
			try{ 
				if(targetStream==null && os!=null){
					os.close();
				}
			}catch(Exception ignored){}
			if(ftc!=null){
				try{
					ftc.destroy();
				}catch(Exception e1){
//					System.out.println("Could not destroy the filetransfer client",e1);
				}
			}
		}
	}

	/**
	 * if possible, copy the remote executable flag to the local file
	 * @throws Exception
	 */
	private void copyProperties(GridFileType source, File localFile)throws Exception{
		try{
			localFile.setExecutable(source.getPermissions().getExecutable());
		}
		catch(Exception ex){
			//TODO: logging
//			("Can't set 'executable' flag for "+localFile.getName(), ex);
		}
	}
	
	private void configure(FileTransferClient ftc, Map<String,String>params){
		if(ftc instanceof UFTPFileTransferClient){
			UFTPFileTransferClient u=(UFTPFileTransferClient)ftc;
			String secret=params.get(UFTPConstants.PARAM_SECRET);
			u.setSecret(secret);
		}
	}

	public void setShowProgress(boolean showProgress) {
		this.showProgress = showProgress;
	}

	public void setForceFileOnly(boolean forceFileOnly) {
		this.forceFileOnly = forceFileOnly;
	}

	public void setTargetStream(OutputStream targetStream) {
		this.targetStream = targetStream;
	}
	
}
