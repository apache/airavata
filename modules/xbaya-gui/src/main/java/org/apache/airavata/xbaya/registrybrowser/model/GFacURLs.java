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

package org.apache.airavata.xbaya.registrybrowser.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataRegistry;

public class GFacURLs {
    private AiravataRegistry registry;

    public GFacURLs(AiravataRegistry registry) {
        setRegistry(registry);
    }

    public AiravataRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry registry) {
        this.registry = registry;
    }

    public List<GFacURL> getURLS() {
        List<GFacURL> urls = new ArrayList<GFacURL>();
        try {
			List<String> gfacDescriptorList = getRegistry().getGFacDescriptorList();
			for (String urlString : gfacDescriptorList) {
			    try {
			        urls.add(new GFacURL(getRegistry(), new URL(urlString)));
			    } catch (MalformedURLException e) {
			        // practically speaking this exception should not be possible. just in case,
			        e.printStackTrace();
			    }
			}
		} catch (RegistryException e) {
			e.printStackTrace();
		}
        return urls;
    }
}
