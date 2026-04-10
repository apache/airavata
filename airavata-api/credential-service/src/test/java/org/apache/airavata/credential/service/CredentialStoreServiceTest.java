/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.credential.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.airavata.credential.model.CredentialEntity;
import org.apache.airavata.credential.model.CredentialPK;
import org.apache.airavata.credential.repository.CredentialRepository;
import org.apache.airavata.credential.repository.CredentialStoreException;
import org.apache.airavata.credential.util.CredentialEncryptionUtil;
import org.apache.airavata.interfaces.CommunityUserProvider;
import org.apache.airavata.model.credential.store.proto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CredentialStoreServiceTest {

    @Mock
    CredentialRepository credentialRepository;

    @Mock
    CommunityUserProvider communityUserProvider;

    @Mock
    CredentialEncryptionUtil encryptionUtil;

    CredentialStoreService handler;

    @BeforeEach
    void setUp() throws Exception {
        handler = new CredentialStoreService();
        setField(handler, "credentialRepository", credentialRepository);
        setField(handler, "communityUserProvider", communityUserProvider);
        setField(handler, "encryptionUtil", encryptionUtil);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private CredentialEntity makeEntity(StoredCredential stored, String gatewayId, String tokenId)
            throws CredentialStoreException {
        CredentialEntity entity = new CredentialEntity();
        entity.setGatewayId(gatewayId);
        entity.setTokenId(tokenId);
        entity.setCredential(stored.toByteArray());
        entity.setPortalUserId(CredentialEncryptionUtil.getPortalUserName(stored));
        entity.setTimePersisted(new Timestamp(stored.getSshCredential().getPersistedTime()));
        entity.setDescription(CredentialEncryptionUtil.getDescription(stored));
        return entity;
    }

    @Test
    void getSSHCredential_returnsCredentialWhenFound() throws Exception {
        StoredCredential stored = StoredCredential.newBuilder()
                .setSshCredential(SSHCredential.newBuilder()
                        .setUsername("testUser")
                        .setGatewayId("gw1")
                        .setPublicKey("ssh-rsa AAAA")
                        .setPrivateKey("-----BEGIN RSA PRIVATE KEY-----")
                        .setPassphrase("passphrase123")
                        .setToken("token-1")
                        .setPersistedTime(1000000)
                        .setDescription("test key"))
                .build();

        CredentialEntity entity = makeEntity(stored, "gw1", "token-1");

        when(credentialRepository.findById(new CredentialPK("gw1", "token-1"))).thenReturn(Optional.of(entity));
        when(encryptionUtil.convertByteArrayToCredential(any())).thenReturn(stored);

        SSHCredential result = handler.getSSHCredential("token-1", "gw1");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("gw1", result.getGatewayId());
        assertEquals("ssh-rsa AAAA", result.getPublicKey());
        assertEquals("passphrase123", result.getPassphrase());
        assertEquals("token-1", result.getToken());
        assertEquals(1000000, result.getPersistedTime());
        assertEquals("test key", result.getDescription());
    }

    @Test
    void getSSHCredential_returnsNullWhenNotSSHCredential() throws Exception {
        StoredCredential stored = StoredCredential.newBuilder()
                .setPasswordCredential(PasswordCredential.newBuilder().setToken("token-1"))
                .build();

        CredentialEntity entity = new CredentialEntity();
        entity.setGatewayId("gw1");
        entity.setTokenId("token-1");
        entity.setCredential(stored.toByteArray());
        entity.setPortalUserId("");
        entity.setTimePersisted(new Timestamp(0));

        when(credentialRepository.findById(new CredentialPK("gw1", "token-1"))).thenReturn(Optional.of(entity));
        when(encryptionUtil.convertByteArrayToCredential(any())).thenReturn(stored);

        SSHCredential result = handler.getSSHCredential("token-1", "gw1");

        assertNull(result);
    }

    @Test
    void getSSHCredential_throwsOnError() throws Exception {
        when(credentialRepository.findById(any())).thenThrow(new RuntimeException("DB error"));

        assertThrows(CredentialStoreException.class, () -> handler.getSSHCredential("token-1", "gw1"));
    }

    @Test
    void getPasswordCredential_returnsCredentialWhenFound() throws Exception {
        StoredCredential stored = StoredCredential.newBuilder()
                .setPasswordCredential(PasswordCredential.newBuilder()
                        .setGatewayId("gw1")
                        .setPortalUserName("portalUser")
                        .setLoginUserName("loginUser")
                        .setPassword("secret")
                        .setToken("pwd-token-1")
                        .setPersistedTime(2000000)
                        .setDescription("pwd cred"))
                .build();

        CredentialEntity entity = new CredentialEntity();
        entity.setGatewayId("gw1");
        entity.setTokenId("pwd-token-1");
        entity.setCredential(stored.toByteArray());
        entity.setPortalUserId("portalUser");
        entity.setTimePersisted(new Timestamp(2000000));
        entity.setDescription("pwd cred");

        when(credentialRepository.findById(new CredentialPK("gw1", "pwd-token-1")))
                .thenReturn(Optional.of(entity));
        when(encryptionUtil.convertByteArrayToCredential(any())).thenReturn(stored);

        PasswordCredential result = handler.getPasswordCredential("pwd-token-1", "gw1");

        assertNotNull(result);
        assertEquals("gw1", result.getGatewayId());
        assertEquals("portalUser", result.getPortalUserName());
        assertEquals("loginUser", result.getLoginUserName());
        assertEquals("secret", result.getPassword());
        assertEquals("pwd-token-1", result.getToken());
    }

    @Test
    void getPasswordCredential_returnsNullWhenNotPasswordCredential() throws Exception {
        StoredCredential stored = StoredCredential.newBuilder()
                .setSshCredential(SSHCredential.newBuilder().setToken("token-1"))
                .build();

        CredentialEntity entity = new CredentialEntity();
        entity.setGatewayId("gw1");
        entity.setTokenId("token-1");
        entity.setCredential(stored.toByteArray());
        entity.setPortalUserId("");
        entity.setTimePersisted(new Timestamp(0));

        when(credentialRepository.findById(new CredentialPK("gw1", "token-1"))).thenReturn(Optional.of(entity));
        when(encryptionUtil.convertByteArrayToCredential(any())).thenReturn(stored);

        PasswordCredential result = handler.getPasswordCredential("token-1", "gw1");

        assertNull(result);
    }

    @Test
    void addSSHCredential_savesAndReturnsToken() throws Exception {
        SSHCredential sshCredential = SSHCredential.newBuilder()
                .setGatewayId("gw1")
                .setUsername("testUser")
                .setPublicKey("ssh-rsa AAAA")
                .setPrivateKey("-----BEGIN RSA-----")
                .setDescription("my key")
                .build();

        when(encryptionUtil.convertCredentialToByteArray(any())).thenReturn(new byte[] {1, 2, 3});

        String token = handler.addSSHCredential(sshCredential);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(credentialRepository).save(any(CredentialEntity.class));
    }

    @Test
    void addPasswordCredential_savesAndReturnsToken() throws Exception {
        PasswordCredential pwdCredential = PasswordCredential.newBuilder()
                .setGatewayId("gw1")
                .setPortalUserName("portalUser")
                .setLoginUserName("loginUser")
                .setPassword("secret")
                .setDescription("pwd cred")
                .build();

        when(encryptionUtil.convertCredentialToByteArray(any())).thenReturn(new byte[] {1, 2, 3});

        String token = handler.addPasswordCredential(pwdCredential);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(credentialRepository).save(any(CredentialEntity.class));
    }

    @Test
    void getAllCredentialSummaries_sshType_filtersCorrectly() throws Exception {
        StoredCredential sshStored = StoredCredential.newBuilder()
                .setSshCredential(SSHCredential.newBuilder()
                        .setUsername("user1")
                        .setGatewayId("gw1")
                        .setPublicKey("ssh-rsa key")
                        .setToken("ssh-token-1")
                        .setPersistedTime(1000000))
                .build();

        StoredCredential pwdStored = StoredCredential.newBuilder()
                .setPasswordCredential(PasswordCredential.newBuilder()
                        .setPortalUserName("user2")
                        .setGatewayId("gw1")
                        .setToken("pwd-token-1")
                        .setPersistedTime(2000000))
                .build();

        CredentialEntity sshEntity = new CredentialEntity();
        sshEntity.setGatewayId("gw1");
        sshEntity.setTokenId("ssh-token-1");
        sshEntity.setCredential(sshStored.toByteArray());
        sshEntity.setPortalUserId("user1");
        sshEntity.setTimePersisted(new Timestamp(1000000));

        CredentialEntity pwdEntity = new CredentialEntity();
        pwdEntity.setGatewayId("gw1");
        pwdEntity.setTokenId("pwd-token-1");
        pwdEntity.setCredential(pwdStored.toByteArray());
        pwdEntity.setPortalUserId("user2");
        pwdEntity.setTimePersisted(new Timestamp(2000000));

        List<String> tokens = List.of("ssh-token-1", "pwd-token-1");
        when(credentialRepository.findByGatewayIdAndTokenIdIn("gw1", tokens)).thenReturn(List.of(sshEntity, pwdEntity));
        when(encryptionUtil.convertByteArrayToCredential(sshEntity.getCredential()))
                .thenReturn(sshStored);
        when(encryptionUtil.convertByteArrayToCredential(pwdEntity.getCredential()))
                .thenReturn(pwdStored);

        List<CredentialSummary> result = handler.getAllCredentialSummaries(SummaryType.SSH, tokens, "gw1");

        assertEquals(1, result.size());
        assertEquals(SummaryType.SSH, result.get(0).getType());
        assertEquals("ssh-token-1", result.get(0).getToken());
    }

    @Test
    void getAllCredentialSummaries_passwdType_filtersCorrectly() throws Exception {
        StoredCredential pwdStored = StoredCredential.newBuilder()
                .setPasswordCredential(PasswordCredential.newBuilder()
                        .setPortalUserName("user1")
                        .setGatewayId("gw1")
                        .setToken("pwd-token-1")
                        .setPersistedTime(1000000))
                .build();

        CredentialEntity entity = new CredentialEntity();
        entity.setGatewayId("gw1");
        entity.setTokenId("pwd-token-1");
        entity.setCredential(pwdStored.toByteArray());
        entity.setPortalUserId("user1");
        entity.setTimePersisted(new Timestamp(1000000));

        List<String> tokens = List.of("pwd-token-1");
        when(credentialRepository.findByGatewayIdAndTokenIdIn("gw1", tokens)).thenReturn(List.of(entity));
        when(encryptionUtil.convertByteArrayToCredential(any())).thenReturn(pwdStored);

        List<CredentialSummary> result = handler.getAllCredentialSummaries(SummaryType.PASSWD, tokens, "gw1");

        assertEquals(1, result.size());
        assertEquals(SummaryType.PASSWD, result.get(0).getType());
    }

    @Test
    void deleteSSHCredential_delegatesToRepository() throws Exception {
        handler.deleteSSHCredential("token-1", "gw1");

        verify(credentialRepository).deleteById(new CredentialPK("gw1", "token-1"));
    }

    @Test
    void deleteSSHCredential_throwsOnError() throws Exception {
        doThrow(new RuntimeException("DB error"))
                .when(credentialRepository)
                .deleteById(new CredentialPK("gw1", "token-1"));

        assertThrows(CredentialStoreException.class, () -> handler.deleteSSHCredential("token-1", "gw1"));
    }

    @Test
    void getAllCredentialSummaryForGateway_sshType_returnsSSHOnly() throws Exception {
        StoredCredential sshStored = StoredCredential.newBuilder()
                .setSshCredential(SSHCredential.newBuilder()
                        .setUsername("user1")
                        .setGatewayId("gw1")
                        .setPublicKey("ssh-rsa key")
                        .setToken("ssh-token-1")
                        .setPersistedTime(1000000))
                .build();

        CredentialEntity entity = new CredentialEntity();
        entity.setGatewayId("gw1");
        entity.setTokenId("ssh-token-1");
        entity.setCredential(sshStored.toByteArray());
        entity.setPortalUserId("user1");
        entity.setTimePersisted(new Timestamp(1000000));

        when(credentialRepository.findByGatewayId("gw1")).thenReturn(List.of(entity));
        when(encryptionUtil.convertByteArrayToCredential(any())).thenReturn(sshStored);

        List<CredentialSummary> result = handler.getAllCredentialSummaryForGateway(SummaryType.SSH, "gw1");

        assertEquals(1, result.size());
        assertEquals("ssh-token-1", result.get(0).getToken());
    }

    @Test
    void getAllCredentialSummaryForGateway_nonSSHType_returnsEmpty() throws Exception {
        List<CredentialSummary> result = handler.getAllCredentialSummaryForGateway(SummaryType.PASSWD, "gw1");

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllPWDCredentialsForGateway_returnsPwdTokensAndDescriptions() throws Exception {
        StoredCredential pwdStored = StoredCredential.newBuilder()
                .setPasswordCredential(PasswordCredential.newBuilder()
                        .setToken("pwd-token-1")
                        .setDescription("my password")
                        .setGatewayId("gw1")
                        .setPortalUserName("user1")
                        .setPersistedTime(System.currentTimeMillis()))
                .build();

        CredentialEntity entity = new CredentialEntity();
        entity.setGatewayId("gw1");
        entity.setTokenId("pwd-token-1");
        entity.setCredential(pwdStored.toByteArray());
        entity.setPortalUserId("user1");
        entity.setTimePersisted(new Timestamp(System.currentTimeMillis()));
        entity.setDescription("my password");

        when(credentialRepository.findByGatewayId("gw1")).thenReturn(List.of(entity));
        when(encryptionUtil.convertByteArrayToCredential(any())).thenReturn(pwdStored);

        Map<String, String> result = handler.getAllPWDCredentialsForGateway("gw1");

        assertEquals(1, result.size());
        assertEquals("my password", result.get("pwd-token-1"));
    }
}
