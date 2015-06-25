package org.apache.airavata.secure.sample;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class OAuthTokenRetrievalClient {
    /**
     * Retrieve the OAuth Access token via the specified grant type.
     * @param consumerId
     * @param consumerSecret
     * @param userName
     * @param password
     * @param grantType
     * @return
     * @throws SecurityException
     */
    public String retrieveAccessToken(String consumerId, String consumerSecret, String userName, String password, int grantType)
            throws SecurityException {

        HttpPost postMethod = null;
        try {
            //TODO:handle SSL handshake with WSO2 IS properly.
            org.apache.http.conn.ssl.SSLSocketFactory sf = new org.apache.http.conn.ssl.SSLSocketFactory(
                    SSLContext.getDefault());
            sf.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme httpsScheme = new Scheme("https", sf, Properties.authzServerPort);
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(httpsScheme);

            postMethod = new HttpPost(Properties.oauthTokenEndPointURL);
            //build the HTTP request with relevant params for resource owner credential grant type
            String authInfo = consumerId + ":" + consumerSecret;
            String authHeader = new String(Base64.encodeBase64(authInfo.getBytes()));

            postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");
            postMethod.setHeader("Authorization", "Basic " + authHeader);

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

            if (grantType == 1) {
                urlParameters.add(new BasicNameValuePair("grant_type", "password"));
                urlParameters.add(new BasicNameValuePair("username", userName));
                urlParameters.add(new BasicNameValuePair("password", password));

            } else if (grantType == 2) {
                urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
            }

            postMethod.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = httpClient.execute(postMethod);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result.toString());
            return (String) jsonObject.get("access_token");
        } catch (ClientProtocolException e) {
            throw new SecurityException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new SecurityException(e.getMessage());
        } catch (IOException e) {
            throw new SecurityException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage());
        } catch (ParseException e) {
            throw new SecurityException(e.getMessage());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }
}