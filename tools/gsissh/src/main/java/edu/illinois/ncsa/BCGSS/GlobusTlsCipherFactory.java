///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package edu.illinois.ncsa.BCGSS;
//
//import edu.illinois.ncsa.bouncycastle.crypto.Digest;
//import edu.illinois.ncsa.bouncycastle.crypto.tls.AlertDescription;
//import edu.illinois.ncsa.bouncycastle.crypto.tls.DefaultTlsCipherFactory;
//import edu.illinois.ncsa.bouncycastle.crypto.tls.TlsBlockCipher;
//import edu.illinois.ncsa.bouncycastle.crypto.tls.TlsCipher;
//import edu.illinois.ncsa.bouncycastle.crypto.tls.TlsClientContext;
//import edu.illinois.ncsa.bouncycastle.crypto.tls.TlsFatalAlert;
//
//import java.io.IOException;
//
//public class GlobusTlsCipherFactory extends DefaultTlsCipherFactory {
//    protected TlsBlockCipher tlsBlockCipher;
//    protected Digest digest;
//
//    public TlsBlockCipher getTlsBlockCipher() {
//        return tlsBlockCipher;
//    }
//
//    public Digest getDigest() {
//        return digest;
//    }
//
//    public TlsCipher createCipher(TlsClientContext context,
//                                     int encAlg, int digestAlg)
//            throws IOException {
//        TlsCipher cipher = super.createCipher(context, encAlg, digestAlg);
//        if (cipher instanceof TlsBlockCipher) {
//            tlsBlockCipher = (TlsBlockCipher) cipher;
//        } else {
//            throw new TlsFatalAlert(AlertDescription.internal_error);
//        }
//
//        return cipher;
//    }
//
//    protected Digest createDigest(int digestAlgorithm) throws IOException {
//        digest = super.createDigest(digestAlgorithm);
//        return digest;
//    }
//}
