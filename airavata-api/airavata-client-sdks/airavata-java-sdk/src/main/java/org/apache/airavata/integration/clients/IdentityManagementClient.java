package org.apache.airavata.integration.clients;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.integration.utils.Constants;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.util.TrustStoreManager;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Identity related operations
 */
public class IdentityManagementClient extends Connector {

    private static Logger LOGGER = LoggerFactory.getLogger(AiravataAPIClient.class);

    private String iamServerURL = null;
    private String iamClientId = null;
    private String iamClientSec = null;
    private String redirectURI = null;
    private String trustStorePath = null;
    private String trustStorePassword = null;

    public IdentityManagementClient(String fileName) throws IOException, AiravataSecurityException {
        super(fileName);
        this.iamServerURL = getProperties().getProperty(Constants.IAM_SERVER_URL);
        this.iamClientId = getProperties().getProperty(Constants.IAM_CLIENT_ID);
        this.iamClientSec = getProperties().getProperty(Constants.IAM_CLIENT_SECRET);
        this.redirectURI = getProperties().getProperty(Constants.REDIRECT_URI);
        this.trustStorePath = getProperties().getProperty(Constants.TRUST_STORE_PATH);
        this.trustStorePassword = getProperties().getProperty(Constants.TRUST_STORE_PASSWORD);

        TrustStoreManager trustStoreManager = new TrustStoreManager();
        trustStoreManager.initializeTrustStoreManager(trustStorePath,
                trustStorePassword);
    }


    private String getOpenIDConfigurationUrl(String realm) throws ApplicationSettingsException {
        return this.iamServerURL + "/realms/" + realm + "/.well-known/openid-configuration";
    }

    private String getFromUrl(String urlToRead, String token) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        if (token != null) {
            String bearerAuth = "Bearer " + token;
            conn.setRequestProperty("Authorization", bearerAuth);
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    private String getTokenEndpoint(String gatewayId) throws Exception {
        String openIdConnectUrl = getOpenIDConfigurationUrl(gatewayId);
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        return openIdConnectConfig.getString("token_endpoint");
    }

    private String getUsername(String token) throws Exception {

        String openIdConnectUrl = getOpenIDConfigurationUrl(getProperties().getProperty(Constants.IAM_REALM_ID));
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        String userInfoEndPoint = openIdConnectConfig.getString("userinfo_endpoint");
        JSONObject userInfo = new JSONObject(getFromUrl(userInfoEndPoint, token));

        return userInfo.getString("preferred_username");
    }

    public JSONObject getAccessToken(String code, String redirectURI) throws Exception {

        String openIdConnectUrl = getOpenIDConfigurationUrl(getProperties().getProperty(Constants.IAM_REALM_ID));
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        String urlString = openIdConnectConfig.getString("token_endpoint");

        CloseableHttpClient httpClient = HttpClients.createSystem();
        HttpPost httpPost = new HttpPost(urlString);
        String encoded = Base64.getEncoder().encodeToString((this.iamClientId + ":" + this.iamClientSec).getBytes(StandardCharsets.UTF_8));
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        formParams.add(new BasicNameValuePair("code", code));
        formParams.add(new BasicNameValuePair("redirect_uri", redirectURI));
        formParams.add(new BasicNameValuePair("client_id", this.iamClientId));
        formParams.add(new BasicNameValuePair("client_secret", this.iamClientSec));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPost.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject tokenInfo = new JSONObject(responseBody);
                return tokenInfo;
            } finally {
                response.close();
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public JSONObject getTokenFromRefreshToken(String refreshToken) throws Exception {

        String openIdConnectUrl = getOpenIDConfigurationUrl(getProperties().getProperty(Constants.IAM_REALM_ID));
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        String urlString = openIdConnectConfig.getString("token_endpoint");
        CloseableHttpClient httpClient = HttpClients.createSystem();

        HttpPost httpPost = new HttpPost(urlString);
        String encoded = Base64.getEncoder().encodeToString((this.iamClientId + ":" + this.iamClientSec).getBytes(StandardCharsets.UTF_8));
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
        formParams.add(new BasicNameValuePair("refresh_token", refreshToken));
        formParams.add(new BasicNameValuePair("client_id", this.iamClientId));
        formParams.add(new BasicNameValuePair("client_secret", this.iamClientSec));
        formParams.add(new BasicNameValuePair("scope", "openid"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPost.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject tokenInfo = new JSONObject(responseBody);
                return tokenInfo;
            } finally {
                response.close();
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public JSONObject getTokenFromPasswordGrantType(String username, String password) throws Exception {

        String openIdConnectUrl = getOpenIDConfigurationUrl(getProperties().getProperty(Constants.IAM_REALM_ID));
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        String urlString = openIdConnectConfig.getString("token_endpoint");
        CloseableHttpClient httpClient = HttpClients.createSystem();

        HttpPost httpPost = new HttpPost(urlString);
        String encoded = Base64.getEncoder().encodeToString((this.iamClientId + ":" + this.iamClientSec).getBytes(StandardCharsets.UTF_8));
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("grant_type", "password"));
        formParams.add(new BasicNameValuePair("username", username));
        formParams.add(new BasicNameValuePair("client_id", this.iamClientId));
        formParams.add(new BasicNameValuePair("client_secret", this.iamClientSec));
        formParams.add(new BasicNameValuePair("password", "password"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPost.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject tokenInfo = new JSONObject(responseBody);
                return tokenInfo;
            } finally {
                response.close();
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void logout(String refreshToken) throws Exception {
        String openIdConnectUrl = getOpenIDConfigurationUrl(getProperties().getProperty(Constants.IAM_REALM_ID));
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        String urlString = openIdConnectConfig.getString("end_session_endpoint");

        CloseableHttpClient httpClient = HttpClients.createSystem();

        HttpPost httpPost = new HttpPost(urlString);
        String encoded = Base64.getEncoder().encodeToString((this.iamClientId + ":" + this.iamClientSec).getBytes(StandardCharsets.UTF_8));
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("refresh_token", refreshToken));
        formParams.add(new BasicNameValuePair("client_id", this.iamClientId));
        formParams.add(new BasicNameValuePair("client_secret", this.iamClientSec));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPost.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            response.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public AuthzToken getAuthToken(String accessToken) throws Exception {
        String gatewayId = getProperties().getProperty(Constants.GATEWAY_ID);
        String username = getUsername(accessToken);
        AuthzToken token = new AuthzToken();
        token.setAccessToken(accessToken);
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.USER_NAME, username);
        claimsMap.put(Constants.GATEWAY_ID, gatewayId);
        token.setClaimsMap(claimsMap);
        return token;
    }

}
