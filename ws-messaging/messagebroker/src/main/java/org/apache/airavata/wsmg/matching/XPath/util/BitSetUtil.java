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

package org.apache.airavata.wsmg.matching.XPath.util;

import java.util.BitSet;

public class BitSetUtil {
    // Returns a bitset containing the values in bytes.
    // The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    // Returns a byte array of at least length 1.
    // The most significant bit in the result is guaranteed not to be a 1
    // (since BitSet does not support sign extension).
    // The byte-ordering of the result is big-endian which means the most significant bit is in element 0.
    // The bit at index 0 of the bit set is assumed to be the least significant bit.
    public static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    public static void main(String[] args) {
        // Create the bitset
        BitSet bits = new BitSet();

        // Set a bit on
        bits.set(2); // 100 = decimal 4
        bits.set(127);
        long start = System.nanoTime();
        byte[] byteArray = toByteArray(bits);
        long end0 = System.nanoTime();
        String base64 = org.apache.axis2.util.Base64.encode(byteArray);
        long end1 = System.nanoTime();
        byte[] byteArrayNew = org.apache.axis2.util.Base64.decode(base64);
        BitSet bitsNew = fromByteArray(byteArrayNew);
        long end2 = System.nanoTime();
        System.out.println("Base64String=" + base64);
        System.out.println("bitSet=" + bitsNew.toString());
        System.out.println("encodingTime=" + (end1 - start) + " decodingTime=" + (end2 - end1) + " encodingTime0="
                + (end0 - start));
        bits.get(0);
        bits.get(2);

        // Clear a bit
        bits.clear(1);

        // Setting a range of bits
        BitSet bits2 = new BitSet();
        bits2.set(1, 4); // 1110
        bits2.set(127);

        long start2 = System.nanoTime();

        // And'ing two bitsets
        bits.and(bits2); // 0100
        long end = System.nanoTime();
        long total = (end - start2);
        System.out.println("Total=" + total);
        // Xor'ing two bitsets
        bits.xor(bits2); // 1010

        // Flip all bits in the bitset
        bits.flip(0, bits.length()); // 0101

        // Andnot'ing two bitsets
        bits.andNot(bits2); // 0001

        // Or'ing two bitsets
        bits.or(bits2); // 1111

        // byte[] byteArray=toByteArray(bits);
        // String base64=org.apache.axis2.util.Base64.encode(byteArray);
        // System.out.println("Base64String="+base64);

    }
}
