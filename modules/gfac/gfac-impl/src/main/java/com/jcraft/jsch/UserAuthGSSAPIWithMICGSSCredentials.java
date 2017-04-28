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
package com.jcraft.jsch;

import org.apache.airavata.gfac.core.authentication.GSIAuthenticationInfo;
import org.globus.gsi.gssapi.GSSConstants;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
 * Copyright(c)2004,2005,2006 ymnk, JCraft,Inc. All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The names of the authors may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission. THIS SOFTWARE IS PROVIDED ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT, INC. OR ANY CONTRIBUTORS TO THIS
 * SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION)HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE)ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * This class now supports two mappings to the gssapi-with-mic method: x509
 * (preferred) and krb5.
 *
 * @author Al Rossi
 * @author Jeff Overbey
 */
public class UserAuthGSSAPIWithMICGSSCredentials extends UserAuth {

    private static final int SSH_MSG_USERAUTH_GSSAPI_RESPONSE = 60;
    private static final int SSH_MSG_USERAUTH_GSSAPI_TOKEN = 61;
    // private static final int SSH_MSG_USERAUTH_GSSAPI_EXCHANGE_COMPLETE = 63;
    private static final int SSH_MSG_USERAUTH_GSSAPI_ERROR = 64;
    private static final int SSH_MSG_USERAUTH_GSSAPI_ERRTOK = 65;
    private static final int SSH_MSG_USERAUTH_GSSAPI_MIC = 66;

    // this is the preferred order
    private static String[] supportedMethods = { "gssapi-with-mic.x509",
            "gssapi-with-mic.krb5" };
    private static byte[][] supportedOids;

    static {
        try {
            supportedOids = new byte[][] {
                    GSSConstants.MECH_OID.getDER(),
                    new Oid("1.2.840.113554.1.2.2").getDER() };
        } catch (GSSException gsse) {
            gsse.printStackTrace();
        }
    }

    @Override
    public boolean start(Session session) throws Exception {

        // this.userinfo = userinfo;
        Packet packet = session.packet;
        Buffer buf = session.buf;
        final String username = session.username;
        byte[] _username = Util.str2byte(username);

        // checkForSupportedOIDs
        List methods = new ArrayList();
        boolean found = false;
        for (int i = 0; i < supportedOids.length; i++) {
            found = found
                    || checkForSupportedOIDs(methods, packet, buf, i,
                    _username, session);
        }

        if (!found)
            return false;

        // logger.debug( "supported methods " + methods );

        boolean success = false;
        for (Iterator it = methods.iterator(); it.hasNext();) {
            String method = (String) it.next();
            success = tryMethod(username, _username, method, session, packet,
                    buf);
            if (success)
                break;
        }
        return success;

    }

    private boolean checkForSupportedOIDs(List methods, Packet packet,
                                          Buffer buf, int index, byte[] _username, Session session)
            throws Exception {
        packet.reset();

        // byte SSH_MSG_USERAUTH_REQUEST(50)
        // string user name(in ISO-10646 UTF-8 encoding)
        // string service name(in US-ASCII)
        // string "gssapi"(US-ASCII)
        // uint32 n, the number of OIDs client supports
        // string[n] mechanism OIDS
        buf.putByte((byte) SSH_MSG_USERAUTH_REQUEST);
        buf.putString(_username);
        buf.putString("ssh-connection".getBytes());
        buf.putString("gssapi-with-mic".getBytes());
        buf.putInt(1);
        buf.putString(supportedOids[index]);
        session.write(packet);

        while (true) {
            buf = session.read(buf);

            if (buf.buffer[5] == SSH_MSG_USERAUTH_FAILURE) {
                return false;
            }

            if (buf.buffer[5] == SSH_MSG_USERAUTH_GSSAPI_RESPONSE) {
                buf.getInt();
                buf.getByte();
                buf.getByte();
                byte[] message = buf.getString();
                // logger.debug( "OID " + supportedOids[index] );
                if (Util.array_equals(message, supportedOids[index])) {
                    methods.add(supportedMethods[index]);
                    // logger.debug( "OID MATCH, method is " + methods );
                    return true;
                }
            }

            if (buf.buffer[5] == SSH_MSG_USERAUTH_BANNER) {
                buf.getInt();
                buf.getByte();
                buf.getByte();
                byte[] _message = buf.getString();
                buf.getString();
                String message = Util.byte2str(_message);
                if (userinfo != null) {
                    userinfo.showMessage(message);
                }
                continue;
            }
            return false;
        }
    }

    private boolean tryMethod(String username, byte[] _username, String method,
                              Session session, Packet packet, Buffer buf) throws Exception {
        GSSContext context = null;
        try {
            Class c = Class.forName(session.getConfig(method));
            context = (GSSContext) (c.newInstance());

        } catch (Exception e) {
            // logger.error( "could not instantiate GSSContext", e );
            return false;
        }

        // Get the credentials and set them
        // Not a good way, but we dont have any choice
        if (session instanceof ExtendedSession) {
            GSIAuthenticationInfo authenticationInfo = ((ExtendedSession) session).getAuthenticationInfo();

            if (context instanceof GSSContextX509) {
                ((GSSContextX509) context).setCredential(authenticationInfo.getCredentials());
            }
        }

        // logger.debug( "GOT CONTEXT: " + context );


        // FIXME
        // if ( userinfo instanceof IX509UserInfo ) {
        // if ( context instanceof GSSContextX509 ) {
        // GSSCredential credential = ( ( IX509UserInfo )userinfo
        // ).getCredential();
        // logger.debug( "user info credential = " + credential );
        // ( ( GSSContextX509 )context ).setCredential( credential );
        // }
        // }

        try {
            context.create(username, session.host);
        } catch (JSchException e) {
            // logger.error( "context creation failed", e );
            return false;
        }

        byte[] token = new byte[0];

        while (!context.isEstablished()) {
            try {
                token = context.init(token, 0, token.length);
            } catch (JSchException e) {
                // logger.error( "context initialization failed", e );
                // TODO
                // ERRTOK should be sent?
                // byte SSH_MSG_USERAUTH_GSSAPI_ERRTOK
                // string error token
                return false;
            }

            if (token != null) {
                packet.reset();
                buf.putByte((byte) SSH_MSG_USERAUTH_GSSAPI_TOKEN);
                buf.putString(token);
                session.write(packet);
            }

            if (!context.isEstablished()) {
                buf = session.read(buf);

                if (buf.buffer[5] == SSH_MSG_USERAUTH_GSSAPI_ERROR) {
                    // uint32 major_status
                    // uint32 minor_status
                    // string message
                    // string language tag
                    buf = session.read(buf);
                } else if (buf.buffer[5] == SSH_MSG_USERAUTH_GSSAPI_ERRTOK) {
                    buf = session.read(buf);
                }

                if (buf.buffer[5] == SSH_MSG_USERAUTH_FAILURE) {
                    return false;
                }

                buf.getInt();
                buf.getByte();
                buf.getByte();
                token = buf.getString();
            }
        }

        Buffer mbuf = new Buffer();
        // string session identifier
        // byte SSH_MSG_USERAUTH_REQUEST
        // string user name
        // string service
        // string "gssapi-with-mic"
        mbuf.putString(session.getSessionId());
        mbuf.putByte((byte) SSH_MSG_USERAUTH_REQUEST);
        mbuf.putString(_username);
        mbuf.putString("ssh-connection".getBytes());
        mbuf.putString("gssapi-with-mic".getBytes());

        byte[] mic = context.getMIC(mbuf.buffer, 0, mbuf.getLength());

        if (mic == null) { // there was an error in the getMIC call
            return false;
        }

        packet.reset();
        buf.putByte((byte) SSH_MSG_USERAUTH_GSSAPI_MIC);
        buf.putString(mic);
        session.write(packet);

        context.dispose();

        buf = session.read(buf);
        if (buf.buffer[5] == SSH_MSG_USERAUTH_SUCCESS) {
            return true;
        }
        if (buf.buffer[5] == SSH_MSG_USERAUTH_FAILURE) {
            buf.getInt();
            buf.getByte();
            buf.getByte();
            byte[] foo = buf.getString();
            int partial_success = buf.getByte();
            if (partial_success != 0) {
                throw new JSchPartialAuthException(new String(foo));
            }
        }
        return false;
    }
}

