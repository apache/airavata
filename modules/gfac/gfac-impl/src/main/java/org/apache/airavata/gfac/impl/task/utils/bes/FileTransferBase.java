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

import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.util.PropertyHelper;
import org.unigrids.services.atomic.types.GridFileType;
import org.unigrids.services.atomic.types.ProtocolType;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Pattern;

public class FileTransferBase {

	protected Properties extraParameterSource;

	protected boolean timing=false;

	protected boolean recurse=false;

	protected String from;

	protected String to;

	//index of first byte to download
	protected Long startByte;
	
	//index of last byte to download
	protected Long endByte;
	
	/**
	 * the creation mode
	 */
	protected Mode mode;

	/**
	 * whether the job processing should fail if an error occurs
	 */
	protected boolean failOnError;

	protected List<ProtocolType.Enum> preferredProtocols=new ArrayList<ProtocolType.Enum>();

	public FileTransferBase(){
		preferredProtocols.add(ProtocolType.BFT);
	}

	protected Map<String,String>makeExtraParameters(ProtocolType.Enum protocol){
		Map<String, String> res;
		if(extraParameterSource==null){
			res=new HashMap<String, String>();
		}
		else{
			String p=String.valueOf(protocol);
			PropertyHelper ph=new PropertyHelper(extraParameterSource, new String[]{p,p.toLowerCase()});
			res= ph.getFilteredMap();
		}
		if(res.size()>0){
			// TODO: change it to logger 
			System.out.println("Have "+res.size()+" extra parameters for protocol "+protocol);
		}
		return res;
	}
	
	
	public String getTo() {
		return to;
	}

	public String getFrom() {
		return from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public Mode getMode() {
		return mode;
	}

	public boolean isFailOnError() {
		return failOnError;
	}

	public boolean isTiming() {
		return timing;
	}

	public void setTiming(boolean timing) {
		this.timing = timing;
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	public List<ProtocolType.Enum> getPreferredProtocols() {
		return preferredProtocols;
	}

	public void setPreferredProtocols(List<ProtocolType.Enum> preferredProtocols) {
		this.preferredProtocols = preferredProtocols;
	}

	public void setExtraParameterSource(Properties properties){
		this.extraParameterSource=properties;
	}

	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}
	/**
	 * check if the given path denotes a valid remote directory
	 * @param remotePath - the path
	 * @param sms - the storage
	 * @return <code>true</code> if the remote directory exists and is a directory
	 */
	protected boolean isValidDirectory(String remotePath, StorageClient sms){
		boolean result=false;
		if(! ("/".equals(remotePath) || ".".equals(remotePath)) ){
			try{
				GridFileType gft=sms.listProperties(remotePath);
				result=gft.getIsDirectory();
			}catch(Exception ex){
				result=false;
			}
		}
		else result=true;
		
		return result;
	}
	
	public File[] resolveWildCards(File original){
		final String name=original.getName();
		if(!hasWildCards(original))return new File[]{original};
		File parent=original.getParentFile();
		if(parent==null)parent=new File(".");
		FilenameFilter filter=new FilenameFilter(){
			Pattern p=createPattern(name);
			public boolean accept(File file, String name){
				return p.matcher(name).matches();
			}
		};
		return parent.listFiles(filter);
	}

	protected boolean hasWildCards(File file){
		return hasWildCards(file.getName());
	}

	public boolean hasWildCards(String name){
		return name.contains("*") || name.contains("?");
	}

	private Pattern createPattern(String nameWithWildcards){
		String regex=nameWithWildcards.replace("?",".").replace("*", ".*");
		return Pattern.compile(regex);
	}
	
	protected ProtocolType.Enum chosenProtocol=null;
	
	public ProtocolType.Enum getChosenProtocol(){
		return chosenProtocol;
	}

	public Long getStartByte() {
		return startByte;
	}

	public void setStartByte(Long startByte) {
		this.startByte = startByte;
	}

	public Long getEndByte() {
		return endByte;
	}

	public void setEndByte(Long endByte) {
		this.endByte = endByte;
	}
	
	/**
	 * checks if a byte range is defined
	 * @return <code>true</code> iff both startByte and endByte are defined
	 */
	protected boolean isRange(){
		return startByte!=null && endByte!=null;
	}
	
	/**
	 * get the number of bytes in the byte range, or "-1" if the range is open-ended
	 * @return
	 */
	protected long getRangeSize(){
		if(Long.MAX_VALUE==endByte)return -1;
		return endByte-startByte;
	}
}
