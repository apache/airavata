package org.apache.airavata.xbaya.registrybrowser.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.registry.api.Registry;

public class GFacURLs {
	private Registry registry;
	
	public GFacURLs(Registry registry){
		setRegistry(registry);
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}
	
	public List<GFacURL> getURLS(){
		List<GFacURL> urls=new ArrayList<GFacURL>();
		List<String> gfacDescriptorList = getRegistry().getGFacDescriptorList();
		for (String urlString : gfacDescriptorList) {
			try {
				urls.add(new GFacURL(getRegistry(),new URL(urlString)));
			} catch (MalformedURLException e) {
				//practically speaking this exception should not be possible. just in case,
				e.printStackTrace();
			}
		}
		return urls;
	}
}
