/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpass.smartcard;

import javax.smartcardio.ResponseAPDU;
import applets.JPassApplet;

/**
 *
 * @author macbook
 */
public class JavaCard {
    CardMngr cardManager = new CardMngr();
    
    private static byte APPLET_AID[] = {(byte) 0x73, (byte) 0x69, (byte) 0x6D, (byte) 0x70, (byte) 0x6C, 
        (byte) 0x65, (byte) 0x61, (byte) 0x70, (byte) 0x70, (byte) 0x6C, (byte) 0x65, (byte) 0x74};
    private static byte SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, 
        (byte) 0x73, (byte) 0x69, (byte) 0x6D, (byte) 0x70, (byte) 0x6C,
        (byte) 0x65, (byte) 0x61, (byte) 0x70, (byte) 0x70, (byte) 0x6C, (byte) 0x65, (byte) 0x74};
    
    boolean isSimulator = true;
    
    JavaCard(boolean isSimulator) throws Exception {
        this.isSimulator = isSimulator;
        if(!this.setupCard()) {
            throw(new Exception("Error while initalizing card"));
        }
    }
    
    /*
     *  Call this when you are done with the card.
    */    
    public void tearDown() {
        this.cleanup(this.isSimulator);
    }
    
    public ResponseAPDU sendAPDU(byte[] apdu) throws Exception {
        return this.sendAPDU(apdu, this.isSimulator);
    }
        
    private boolean setupCard() {
        return this.setupCard(this.isSimulator);
    }
    
    private boolean setupCard(boolean isSimulator) {
        if(isSimulator){
            byte[] installData = new byte[10];
            cardManager.prepareLocalSimulatorApplet(APPLET_AID, installData, JPassApplet.class);
            return true;
        } else {
            try {
                boolean status = cardManager.ConnectToCard();
                cardManager.sendAPDU(SELECT_SIMPLEAPPLET);
                return status;      
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    private void cleanup(boolean isSimulator) {
        try {
            cardManager.DisconnectFromCard();
        } catch (Exception ex) { }
    }
    
    private ResponseAPDU sendAPDU(byte[] apdu, boolean isSimulator) throws Exception {
        if(isSimulator) {
            return new ResponseAPDU(cardManager.sendAPDUSimulator(apdu)); 
        } else {
            return cardManager.sendAPDU(apdu);
        }
    }
}
