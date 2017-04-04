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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.unigrids.services.atomic.types.ProtocolType;

import de.fzj.unicore.uas.client.FileTransferClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.UFTPConstants;
import de.fzj.unicore.uas.client.UFTPFileTransferClient;
import de.fzj.unicore.uas.fts.FiletransferOptions.IMonitorable;

/**
 * upload local file(s) to a remote location
 *
 * @author schuller
 */
public class FileUploader extends FileTransferBase{

	public FileUploader(String from, String to, Mode mode)throws FileNotFoundException{
		this(from,to,mode,true);
	}

	public FileUploader(String from, String to, Mode mode, boolean failOnError)throws FileNotFoundException{
		this.to=to;
		this.from=from;
		this.mode=mode;
		this.failOnError=failOnError;
		checkOK();
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}


	public void perform(StorageClient sms)throws Exception{
		File fileSpec=new File(from);
		boolean hasWildCards=false;
		boolean isDirectory=fileSpec.isDirectory();
		File[] fileset=null;
		
		if(!isDirectory){
			hasWildCards=hasWildCards(fileSpec);
		}
		
		chosenProtocol=sms.findSupportedProtocol(preferredProtocols.toArray(new ProtocolType.Enum[preferredProtocols.size()]));
		Map<String,String>extraParameters=makeExtraParameters(chosenProtocol);

		if(!hasWildCards && !isDirectory){
			//single regular file
			uploadFile(fileSpec,to,sms,chosenProtocol,extraParameters);
			return;
		}
		
		//handle wildcards or directory
		if(hasWildCards){
			fileset=resolveWildCards(fileSpec);
		}
		else{
			fileset=fileSpec.listFiles();
		}
		
		if(!isValidDirectory(to, sms)){
			throw new IOException("The specified remote target '"+to+"' is not a directory");
		}
		if(to==null)to="/";
		String target=isDirectory?to+"/"+fileSpec.getName():to;
		sms.createDirectory(target);
		uploadFiles(fileset,target,sms,chosenProtocol,extraParameters);
	}

	/**
	 * upload a set of files to a remote directory (which must exist)
	 * 
	 * @param files
	 * @param remoteDirectory
	 * @param sms
	 * @param protocol
	 * @param extraParameters
	 * @param msg
	 * @throws Exception
	 */
	private void uploadFiles(File[]files, String remoteDirectory, StorageClient sms, ProtocolType.Enum protocol, 
			Map<String,String>extraParameters)throws Exception{
		for(File localFile: files){
			String target=remoteDirectory+"/"+localFile.getName();
			if(localFile.isDirectory()){
				if(!recurse){
					System.out.println("Skipping directory "+localFile.getAbsolutePath());
				}else{
					File[] fileset=localFile.listFiles();
					sms.createDirectory(target);
					uploadFiles(fileset,target,sms,protocol,extraParameters);
				}
			}else{
				uploadFile(localFile,target,sms,protocol,extraParameters);
			}
		}
	}

	/**
	 * uploads a single regular file
	 * 
	 * @param localFile
	 * @param remotePath
	 * @param sms
	 * @param protocol
	 * @param extraParameters
	 * @param msg
	 * @throws Exception
	 */
	private void uploadFile(File localFile, String remotePath, StorageClient sms, ProtocolType.Enum protocol, 
			Map<String,String>extraParameters) throws Exception{
		long startTime=System.currentTimeMillis();
		FileInputStream is=null;
		FileTransferClient ftc=null;
		try{
			if(remotePath==null){
				remotePath="/"+localFile.getName();
			}
			else if(remotePath.endsWith("/")){
				remotePath+=localFile.getName();
			}
			System.out.println("Uploading local file '"+localFile.getAbsolutePath()+"' -> '"+sms.getUrl()+"#"+remotePath+"'");
			is=new FileInputStream(localFile.getAbsolutePath());
			boolean append=Mode.append.equals(mode);
			ftc=sms.getImport(remotePath, append, extraParameters, protocol);
			configure(ftc, extraParameters);
			if(append)ftc.setAppend(true);
			String url=ftc.getUrl();
			System.out.println("File transfer URL : "+url);
//			ProgressBar p=null;
			if(ftc instanceof IMonitorable){
				long size=localFile.length();
				if(isRange()){
					size=getRangeSize();
				}
//				p=new ProgressBar(localFile.getName(),size,msg);
//				((IMonitorable) ftc).setProgressListener(p);
			}
			if(isRange()){
				System.out.println("Byte range: "+startByte+" - "+(getRangeSize()>0?endByte:""));
				long skipped=0;
				while(skipped<startByte){
					skipped+=is.skip(startByte);
				}
				ftc.writeAllData(is, endByte-startByte+1);
				
			}else{
				ftc.writeAllData(is);
			}
			copyProperties(localFile, sms, remotePath);
			
//			if(ftc instanceof IMonitorable){
//				p.finish();
//			}
			
		}finally{
			if(ftc!=null){
				try{
					ftc.destroy();
				}catch(Exception e1){
//					msg.error("Could not clean-up the filetransfer at <"+ftc.getUrl()+">",e1);
				}
			}
			try{ if(is!=null)is.close(); }catch(Exception ignored){}
		}
		if(timing){
			long duration=System.currentTimeMillis()-startTime;
			double rate=(double)localFile.length()/(double)duration;
			System.out.println("Rate: "+rate+ " kB/sec.");
		}
	}

	/**
	 * if possible, copy the local executable flag to the remote file
	 * @param sourceFile - local file
	 * @throws Exception
	 */
	private void copyProperties(File sourceFile, StorageClient sms, String target)throws Exception{
		boolean x=sourceFile.canExecute();
		try{
			if(x){
				sms.changePermissions(target, true, true, x);
			}
		}catch(Exception ex){
//			System.out.println("Can't set exectuable flag on remote file.",ex);
		}
	}

	private void checkOK()throws FileNotFoundException{
		if(!failOnError){
			return;
		}
		File orig=new File(from);
		if(!orig.isAbsolute()){
			orig=new File(System.getProperty("user.dir"),from);
		}
		File[] files=resolveWildCards(orig);
		if(files==null){
			throw new FileNotFoundException("Local import '"+from+"' does not exist.");
		}
		for(File f: files){
			if(!f.exists())throw new FileNotFoundException("Local import '"+from+"' does not exist.");
		}
	}
	
	private void configure(FileTransferClient ftc, Map<String,String>params){
		if(ftc instanceof UFTPFileTransferClient){
			UFTPFileTransferClient u=(UFTPFileTransferClient)ftc;
			String secret=params.get(UFTPConstants.PARAM_SECRET);
			u.setSecret(secret);
		}
	}
}
