package org.apache.airavata.common.registry.api.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.airavata.common.registry.api.Registry;
import org.apache.airavata.common.registry.api.user.UserManager;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class SlingRegistry implements Registry {	
	private URI repositoryURI;
	private String username;
	private String password;
	private UserManager userManager;

	public SlingRegistry(URI uri, String username, String password) {
		this.repositoryURI = uri;
		this.username = username;
		this.password = password;
	}

	private enum RequestMethod {
		POST, GET
	};

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Jackrabbit";
	}

	@Override
	public URI getRepositoryURI() {
		return repositoryURI;
	}

	protected JSONObject getNodeAsJson(String relPath) throws IOException,
			JSONException {
		HttpURLConnection connection = getConnection(relPath + ".json",
				RequestMethod.GET);
		String response = readFromResponse(connection);
		JSONObject json = new JSONObject(response);
		connection.disconnect();
		return json;
	}

	protected JSONObject getChildNodesAsJson(String relPath, String childLevels)
			throws IOException, JSONException {
		HttpURLConnection connection = getConnection(relPath + "."
				+ childLevels + ".json", RequestMethod.GET);
		String response = readFromResponse(connection);
		JSONObject json = new JSONObject(response);
		connection.disconnect();
		return json;
	}

	protected String createOrUpdateNode(String relPath, String data) throws Exception {
		String uuid = null;
		HttpURLConnection connection = getConnection(relPath,
				RequestMethod.POST);
		if (data != null) {
			writePropertiesToNode(connection, data);
		}
		connection.disconnect();
		uuid = getUuidOfNode(relPath);
		return uuid;
	}

	protected void deleteNode(String relPath) throws IOException {
		HttpURLConnection connection = getConnection(relPath,
				RequestMethod.POST);
		String data = ":operation=delete";
		try{
			writePropertiesToNode(connection, data);
		}catch(Exception ex){
			//node does not exist
		}
		connection.disconnect();
	}

	private String writePropertiesToNode(URLConnection connection, String data)
			throws IOException {
		connection.setDoOutput(true);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				connection.getOutputStream()));
		writer.write(data);
		writer.flush();
		String result = readFromResponse(connection); // calling this is required
		writer.close();
		return result;
	}
	
	protected String writePropertiesToNode(String relPath, String data)
			throws IOException {
		HttpURLConnection connection = getConnection(relPath, RequestMethod.POST);
		connection.setDoOutput(true);
		OutputStreamWriter writer = new OutputStreamWriter(
				connection.getOutputStream());
		writer.write(data);
		writer.flush();
		writer.close();
		String result = readFromResponse(connection); // calling this is required
		return result;
	}

	private String readFromResponse(URLConnection connection)
			throws IOException {
		StringBuffer response = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			response.append(line + "\n");
		}
		reader.close();
		return response.toString();
	}

	private HttpURLConnection getConnection(String relPath, RequestMethod method)
			throws IOException {
		URL url = new URL(repositoryURI.toString() + relPath);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		byte[] encoding = Base64.encodeBase64((username + ":" + password).getBytes());
		connection.setRequestProperty("Authorization", "Basic " + new String(encoding));
		connection.setRequestMethod(method.toString());
		return connection;
	}

	protected String getUuidOfNode(String relPath) throws IOException,
			JSONException {
		HttpURLConnection conn = getConnection(relPath, RequestMethod.POST);
		String data = "jcr:mixinTypes=mix:referenceable";
		writePropertiesToNode(conn, data);
		conn.disconnect();
		JSONObject json = getNodeAsJson(relPath);
		String uuid = json.getString("jcr:uuid");
		return uuid;
	}
	
	protected void removePropertyFromNode(String relPath, String property) throws IOException{
		HttpURLConnection connection = getConnection(relPath, RequestMethod.POST);
		writePropertiesToNode(connection, property + "@Delete");
		connection.disconnect();
	}
	
	protected boolean isPropertyAvailable(String relPath, String property){
		try{
			JSONObject children = getChildNodesAsJson(relPath, "0");
			String[] fields = JSONObject.getNames(children);
			for(String s: fields){
				if(s.equalsIgnoreCase(property)){
					return true;
				}
			}
			return false;
		}catch(Exception ex){
			return false;
		}
	}
	
	public UserManager getUserManager() {
		return userManager;
	}
	
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

}