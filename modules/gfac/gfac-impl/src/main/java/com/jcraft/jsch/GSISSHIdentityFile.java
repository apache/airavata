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

/**
 * NOTE : This is class is directly created using com.jcraft.jsch.IdentityFile
 * IdentityFile has private access. Therefore to suit our requirements we modify IdentityFile
 * with public access.
 */
public class GSISSHIdentityFile implements Identity {
    private JSch jsch;
    private KeyPair kpair;
    private String identity;

    public static GSISSHIdentityFile newInstance(String prvfile, String pubfile, JSch jsch) throws JSchException{
        KeyPair kpair = KeyPair.load(jsch, prvfile, pubfile);
        return new GSISSHIdentityFile(jsch, prvfile, kpair);
    }

    public static GSISSHIdentityFile newInstance(String name, byte[] prvkey, byte[] pubkey, JSch jsch) throws JSchException{
        KeyPair kpair = KeyPair.load(jsch, prvkey, pubkey);
        return new GSISSHIdentityFile(jsch, name, kpair);
    }

    private GSISSHIdentityFile(JSch jsch, String name, KeyPair kpair) throws JSchException{
        this.jsch = jsch;
        this.identity = name;
        this.kpair = kpair;
    }

    /**
     * Decrypts this identity with the specified pass-phrase.
     * @param passphrase the pass-phrase for this identity.
     * @return <tt>true</tt> if the decryption is succeeded
     * or this identity is not cyphered.
     */
    public boolean setPassphrase(byte[] passphrase) throws JSchException{
        return kpair.decrypt(passphrase);
    }

    /**
     * Returns the public-key blob.
     * @return the public-key blob
     */
    public byte[] getPublicKeyBlob(){
        return kpair.getPublicKeyBlob();
    }

    /**
     * Signs on data with this identity, and returns the result.
     * @param data data to be signed
     * @return the signature
     */
    public byte[] getSignature(byte[] data){
        return kpair.getSignature(data);
    }

    /**
     * @deprecated This method should not be invoked.
     * @see #setPassphrase(byte[] passphrase)
     */
    public boolean decrypt(){
        throw new RuntimeException("not implemented");
    }

    /**
     * Returns the name of the key algorithm.
     * @return "ssh-rsa" or "ssh-dss"
     */
    public String getAlgName(){
        return new String(kpair.getKeyTypeName());
    }

    /**
     * Returns the name of this identity.
     * It will be useful to identify this object in the {@link IdentityRepository}.
     */
    public String getName(){
        return identity;
    }

    /**
     * Returns <tt>true</tt> if this identity is cyphered.
     * @return <tt>true</tt> if this identity is cyphered.
     */
    public boolean isEncrypted(){
        return kpair.isEncrypted();
    }

    /**
     * Disposes internally allocated data, like byte array for the private key.
     */
    public void clear(){
        kpair.dispose();
        kpair = null;
    }

    /**
     * Returns an instance of {@link KeyPair} used in this {@link Identity}.
     * @return an instance of {@link KeyPair} used in this {@link Identity}.
     */
    public KeyPair getKeyPair(){
        return kpair;
    }
}

