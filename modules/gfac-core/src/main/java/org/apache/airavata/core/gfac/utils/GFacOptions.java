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

package org.apache.airavata.core.gfac.utils;

public interface GFacOptions {

    public static final String NOTIFIER_CLASS_NAME = "notifierClass";

    public static final String OPTION_INSTALLATION = "installation";

    public static final String OPTION_INSTALLATION_LOCAL = "local";

    public static final String REGISTRY_URL_NAME = "registryURL";

    public static final String FACTORY_URL_NAME = "factoryUrl";

    /** My Proxy Parameters */
    public static final String MYPROXY_USERNAME = "myproxyUserName";

    public static final String MYPROXY_PASSWD = "myproxyPasswd";

    public static final String MYPROXY_SERVER = "myproxyServer";

    public static final String MYPROXY_DEFAULT_SERVER = "rainier.extreme.indiana.edu";

    public static final String MYPROXY_LIFETIME = "myproxyLifetime";

    public static final String MYPROXY_DEFAULT_LIFETIME = "2"; // hours

    public static final String WS_GRAM_PREFERED = "wsgramPrefered";

    /** file transfer service * */
    public static final String FILE_TRANSFER_SERVICE = "filetransferService";

    public static enum FileTransferServiceType {
        DAMN, GridFTP, SSH, SharedFs
    };

    public static final String DAMN_WSDL_URL = "damn.wsdlUrl";

    /** Service will renew the proxy if it is less than this value * */
    public static final String MIN_PROXY_LIFETIME_PER_REQUEST_MINUTES = "minProxyLifetimeNeed4RequestMinutes";

    public static final String HOST_SCHEDULER_URL = "hostscheduler.wsdlurl";

    /* Ssh Options */
    public static final String SSH_USERNAME = "gfac.ssh.username";
    public static final String SSH_PASSWD = "gfac.ssh.password";
    public static final String SSH_KEYFILE = "gfac.ssh.keyFileName";
    public static final String SSH_KNOWN_HOSTFILE = "gfac.ssh.knownHostsFileName";

    public static final String PREFFERED_PROVIDER = "gfac.prefferedProvider";

    public static final String CUSTOM_MESSAGE_INTERCEPTER = "customMessageIntercepter";

    public static final String NOTIFICATION_BROKER_URL_NAME = "notificationBrokerURL";
    public static final String MYLEAD_AGENT_URL_NAME = "myLEADAgentURL";

    public static enum CurrentProviders {
        Local, Gram, WSGram, SSH, ResourceBroker, Sigiri, InteractiveLocal, DRMAA
    };

    public static final String SCMS_LOCATION = "scms.location";

    public static final String SSL_TRUSTED_CERTS_FILE = "ssl.trustedCertsFile";

    public static final String SSL_HOSTCERTS_KEY_FILE = "ssl.hostcertsKeyFile";

    public static final String SERVICE_QNAME = "serviceQName";

    public static final String HOST_NAME = "hostName";

    /** Comma (,) separated list of app descs **/
    public static final String APP_DESC_LISR = "appDescList";

    public static final String DEPLOYMENT_HOSTS = "gfac.deploymentHosts";

    public static final String SIGIRI_LOCATION = "gfac.sigiriLocation";

    public static final String GFAC_TRANSIENT_SERVICE_PROFILE = "gfac.transientServiceProfile";
    public static final String GFAC_PERSISTANT_SERVICE_PROFILE = "gfac.persistantServiceProfile";

    public static final String GFAC_RETRYONJOBERRORCODES = "gfac.retryonJobErrorCodes";

}
