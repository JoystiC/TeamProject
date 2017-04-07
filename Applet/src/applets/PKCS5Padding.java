package applets;

/*
 *  This code was modified (ported) to JavaCard plaftorm by Valentyn Kuznietsov on 10.03.2017, 
 *   with intentions to save some time and not implement PKCS padding myself. Unfortunately, AES with padding is natively supported on JavaCard 3.0+.
 *  Source: http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b14/com/sun/crypto/provider/PKCS5Padding.java#PKCS5Padding.padWithLen%28byte%5B%5D%2Cint%2Cint%29
 *
*/

/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
/**
 * This class implements padding as specified in the PKCS#5 standard.
 *
 * @author Gigi Ankeny
 */


public class PKCS5Padding {

    private final short blockSize;

    PKCS5Padding(short blockSize) {
        this.blockSize = blockSize;
    }

    /**
     * Adds the given number of padding bytes to the data input.
     * The value of the padding bytes is determined
     * by the specific padding mechanism that implements this
     * shorterface.
     *
     * @param in the input buffer with the data to pad
     * @param off the offset in <code>in</code> where the padding bytes
     * are appended
     * @param len the number of padding bytes to add

     */
    public void padWithLen(byte[] in, short off, short len)
        
    {
        if (in == null)
            return;

        if ((short)(off + len) > (short)in.length) {
            return;
        }

        byte paddingOctet = (byte) (len & 0xff);
        for (short i = 0; i < len; i++) {
            in[(short)(i + off)] = paddingOctet;
        }
        return;
    }

    /**
     * Returns the index where the padding starts.
     *
     * <p>Given a buffer with padded data, this method returns the
     * index where the padding starts.
     *
     * @param in the buffer with the padded data
     * @param off the offset in <code>in</code> where the padded data starts
     * @param len the length of the padded data
     *
     * @return the index where the padding starts, or -1 if the input is
     * not properly padded
     */
    public short unpad(byte[] in, short off, short len) {
        if ((in == null) ||
            (len == 0)) { // this can happen if input is really a padded buffer
            return 0;
        }

        byte lastByte = in[(short)(off + len - 1)];
        short padValue = (short) ((short)lastByte & 0x0ff);
        if ((padValue < 0x01)
            || (padValue > blockSize)) {
            return -1;
        }

        short start = (short) (off + len - ((short)lastByte & 0x0ff));
        if (start < off) {
            return -1;
        }

        for (short i = 0; i < ((short)lastByte & 0x0ff); i++) {
            if (in[(short)(start+i)] != lastByte) {
                return -1;
            }
        }

        return start;
    }

    /**
     * Determines how long the padding will be for a given input length.
     *
     * @param len the length of the data to pad
     *
     * @return the length of the padding
     */
    public short padLength(short len) {
        short paddingOctet = (short) (blockSize - (len % blockSize));
        return paddingOctet;
    }
}
