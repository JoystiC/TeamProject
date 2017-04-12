package jpass.smartcard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javacard.security.CryptoException;
import javacard.security.RandomData;
import javax.smartcardio.ResponseAPDU;

public class APDU {
    public void playWithCard() {
        
        JavaCard card = null;
        try {
            card = new JavaCard(true); // true - simulator, false - card
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
            return;
        }
        
        try {
            playWithPINandUnlockCard(card);
        
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
        
        card.tearDown();
        
    }

    private static void playWithPINandUnlockCard(JavaCard card) throws Exception {
        // Try to set some PIN without unlocking a card
        System.out.println("Changing PIN on locked card");
        byte SOME_PIN[] = {(byte) 0x10, (byte) 0x20, (byte) 0x30, (byte) 0x40};
        card.sendAPDU( APDUFactory.setPin(SOME_PIN) );
        
        // Unlock card with pin
        System.out.println("Unlocking card!!!");
        byte DEFAULT_USER_PIN[] = {(byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30};
        card.sendAPDU( APDUFactory.verifyPin(DEFAULT_USER_PIN) );
        
        // Change pin
        System.out.println("Changing PIN on unlocked card!!");
        byte NEW_PIN[] = {(byte) 0x10, (byte) 0x20, (byte) 0x30, (byte) 0x40};
        card.sendAPDU( APDUFactory.setPin(NEW_PIN) );
        
        // Unlock card again
        System.out.println("Unlocking card!!");
        card.sendAPDU( APDUFactory.verifyPin(NEW_PIN) );
    }
}
