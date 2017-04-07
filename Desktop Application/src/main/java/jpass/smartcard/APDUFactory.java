/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpass.smartcard;

import javacard.framework.ISO7816;
import javacard.framework.Util;

/**
 *
 * @author macbook
 */
public class APDUFactory {
    public static byte[] verifyPin(byte[] pinBytes) {
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + pinBytes.length];
        apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x11;
        apdu[CardMngr.OFFSET_P1] = (byte) 0x01;
        apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
        apdu[CardMngr.OFFSET_LC] = (byte)pinBytes.length;

        Util.arrayCopyNonAtomic(pinBytes, (short) 0, apdu, ISO7816.OFFSET_CDATA, (byte)pinBytes.length);
        return apdu;
    }
    
    public static byte[] setPin(byte[] pinBytes) {
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + pinBytes.length];
        apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x12;
        apdu[CardMngr.OFFSET_P1] = (byte) 0x01;
        apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
        apdu[CardMngr.OFFSET_LC] = (byte)pinBytes.length;

        Util.arrayCopyNonAtomic(pinBytes, (short) 0, apdu, ISO7816.OFFSET_CDATA, (byte)pinBytes.length);
        return apdu;
    }
}
