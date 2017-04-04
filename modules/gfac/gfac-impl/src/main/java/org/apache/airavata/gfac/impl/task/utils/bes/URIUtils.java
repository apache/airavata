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


import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import java.net.URISyntaxException;

public class URIUtils {

	public static String encodeAll(String uri) throws URIException
	{
		String result = encodeAuthority(uri);
		result = encodePath(uri);
		result = encodeQuery(result );
		result  = encodeFragment(result );
		return result;
	}
	
	public static String encodeAuthority(String uri) throws URIException
	{
		int start = uri.indexOf("//");
		if(start == -1) return uri;
		start++;
		int end = uri.indexOf("/",start+1);
		if(end == -1) end = uri.indexOf("?",start+1);
		if(end == -1) end = uri.indexOf("#",start+1);
		if(end == -1) end = uri.length();
		String before = uri.substring(0, start+1);
		String authority= uri.substring(start+1,end);
		String after = uri.substring(end);
		authority = URIUtil.encode(authority, URI.allowed_authority);
	
		return before+authority+after;
	}
	
	public static String encodePath(String uri) throws URIException
	{
		int doubleSlashIndex = uri.indexOf("//");
		boolean hasAuthority =  doubleSlashIndex >= 0;
		int start = -1;
		if(hasAuthority)
		{
			start = uri.indexOf("/",doubleSlashIndex+2);
		}
		else
		{
			start = uri.indexOf(":");
		}
		if(start == -1) return uri;
		
		int end = uri.indexOf("?",start+1);
		if(end == -1) end = uri.indexOf("#",start+1);
		if(end == -1) end = uri.length();
		String before = uri.substring(0, start+1);
		String path= uri.substring(start+1,end);
		String after = uri.substring(end);
		path = URIUtil.encode(path, URI.allowed_abs_path);
		return before+path+after;
	}
	
	
	public static String encodeQuery(String uri) throws URIException
	{
		int queryStart = uri.indexOf("?");
		if(queryStart == -1) return uri;
		int queryEnd = uri.indexOf("#");
		if(queryEnd == -1) queryEnd = uri.length();
		
		String beforeQuery = uri.substring(0, queryStart+1);
		String query = uri.substring(queryStart+1,queryEnd);
		String afterQuery = uri.substring(queryEnd);
		query = URIUtil.encode(query, URI.allowed_query);
		return beforeQuery+query+afterQuery;
	}
	
	
	public static String encodeFragment(String uri) throws URIException
	{
		int fragmentStart = uri.indexOf("#");
		if(fragmentStart == -1) return uri;

		String beforeFragment = uri.substring(0, fragmentStart+1);
		String fragment = uri.substring(fragmentStart+1);
		fragment = URIUtil.encode(fragment, URI.allowed_fragment);
		return beforeFragment+fragment;
	}
    
	public static java.net.URI createGsiftpURI(String host, String localPath) throws URISyntaxException {
        StringBuffer buf = new StringBuffer();
        if (!host.startsWith("gsiftp://"))
            buf.append("gsiftp://");
        buf.append(host);
        if (!host.endsWith("/"))
            buf.append("/");
        buf.append(localPath);
        return new java.net.URI(buf.toString());
    }
}