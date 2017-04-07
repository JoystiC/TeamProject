package applets;

import javacard.framework.*;
import javacard.security.*;


public class JPassApplet extends javacard.framework.Applet
{
    private static byte DEFAULT_USER_PIN[] = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
       
    final static short ARRAY_LENGTH                  = (short) 0xff;
    
    // MAIN INSTRUCTION CLASS
    final static byte CLA_SIMPLEAPPLET                = (byte) 0xB0;
    
    final static byte INS_VERIFYPIN                  = (byte) 0x11;
    final static byte INS_SETPIN                     = (byte) 0x12;
    
    final static short SW_OBJECT_NOT_AVAILABLE       = (short) 0x6711;
    final static short SW_BAD_PIN                    = (short) 0x6900;
    final static short SW_INIT_ERROR                 = (short) 0x6911;
    
    final static short SW_Exception                     = (short) 0xff01;
    final static short SW_ArrayIndexOutOfBoundsException = (short) 0xff02;
    final static short SW_ArithmeticException           = (short) 0xff03;
    final static short SW_ArrayStoreException           = (short) 0xff04;
    final static short SW_NullPointerException          = (short) 0xff05;
    final static short SW_NegativeArraySizeException    = (short) 0xff06;
    final static short SW_CryptoException_prefix        = (short) 0xf100;
    final static short SW_SystemException_prefix        = (short) 0xf200;
    final static short SW_PINException_prefix           = (short) 0xf300;
    final static short SW_TransactionException_prefix   = (short) 0xf400;
    final static short SW_CardRuntimeException_prefix   = (short) 0xf500;   
    
    // PIN support
    private   RandomData     m_secureRandom = null;
    private   OwnerPIN       m_pin = null;
    
    // TEMPORARRY ARRAY IN RAM
    private   byte        m_ramArray[] = null;
    // PERSISTENT ARRAY IN EEPROM
    private   byte        m_dataArray[] = null;

    protected JPassApplet(byte[] buffer, short offset, byte length)
    {
        // data offset is used for application specific parameter.
        // initialization with default offset (AID offset).
        short dataOffset = offset;

        if(length > 9) {
            dataOffset += (short)( 1 + buffer[offset]);
            dataOffset += (short)( 1 + buffer[dataOffset]);
            dataOffset++;

            // Allocate arrays
            m_dataArray = new byte[ARRAY_LENGTH];
            Util.arrayFillNonAtomic(m_dataArray, (short) 0, ARRAY_LENGTH, (byte) 0);
            m_ramArray = JCSystem.makeTransientByteArray((short) 260, JCSystem.CLEAR_ON_DESELECT);
            
            // Setup PIN
            m_pin = new OwnerPIN((byte) 5, (byte) 4);
            m_pin.update(DEFAULT_USER_PIN, (byte) 0, (byte) 4);
            
            // Setup random generation
            m_secureRandom = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        }
        register();
    }

    /**
     * Method installing the applet.
     * @param bArray the array constaining installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the data parameter in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        // applet  instance creation 
        new JPassApplet (bArray, bOffset, bLength);
    }

    /**
     * Select method returns true if applet selection is supported.
     * @return boolean status of selection.
     */
    public boolean select()
    {
        return true;
    }

    /**
     * Deselect method called by the system in the deselection process.
     */
    public void deselect()
    {
        return;
    }

    /**
     * Method processing an incoming APDU.
     * @see APDU
     * @param apdu the incoming APDU
     * @exception ISOException with the response bytes defined by ISO 7816-4
     */
    public void process(APDU apdu) throws ISOException
    {
        // get the APDU buffer
        byte[] apduBuffer = apdu.getBuffer();

        // ignore the applet select command dispached to the process
        if (selectingApplet())
            return;

        try {
                        // APDU instruction parser
            if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET) {
                byte ins = apduBuffer[ISO7816.OFFSET_INS];
                if(ins == INS_VERIFYPIN) {
                    if((short)m_pin.getTriesRemaining() > (short)0) {
                       VerifyPIN(apdu); 
                    } else {
                        ISOException.throwIt(SW_BAD_PIN);
                    }
                } else {
                    if(m_pin.isValidated()) {
                        switch(ins) {
                            case INS_SETPIN: SetPIN(apdu); break;
                            // TODO: add cases to handle other commands

                            default :  ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ; break;
                        }      
                    } else {
                        ISOException.throwIt(SW_BAD_PIN);
                    }
                }
            }
        else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);
        } catch (ISOException e) {
            throw e; // Our exception from code, just re-emit
        } catch (ArrayIndexOutOfBoundsException e) {
            ISOException.throwIt(SW_ArrayIndexOutOfBoundsException);
        } catch (ArithmeticException e) {
            ISOException.throwIt(SW_ArithmeticException);
        } catch (ArrayStoreException e) {
            ISOException.throwIt(SW_ArrayStoreException);
        } catch (NullPointerException e) {
            ISOException.throwIt(SW_NullPointerException);
        } catch (NegativeArraySizeException e) {
            ISOException.throwIt(SW_NegativeArraySizeException);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (SW_CryptoException_prefix | e.getReason()));
        } catch (SystemException e) {
            ISOException.throwIt((short) (SW_SystemException_prefix | e.getReason()));
        } catch (PINException e) {
            ISOException.throwIt((short) (SW_PINException_prefix | e.getReason()));
        } catch (TransactionException e) {
            ISOException.throwIt((short) (SW_TransactionException_prefix | e.getReason()));
        } catch (CardRuntimeException e) {
            ISOException.throwIt((short) (SW_CardRuntimeException_prefix | e.getReason()));
        } catch (Exception e) {
            ISOException.throwIt(SW_Exception);
        }
    }
    
    // VERIFY PIN
     void VerifyPIN(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      if (m_pin.check(apdubuf, ISO7816.OFFSET_CDATA, (byte) dataLen) == false) { 
          ISOException.throwIt(SW_BAD_PIN);
      } else {
          m_pin.reset();
          m_pin.check(apdubuf, ISO7816.OFFSET_CDATA, (byte) dataLen);
      }
    }

     // SET PIN
     void SetPIN(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      /*
       * Even though that this command would not be called if card is locked, we still add one more check if it was unlocked.
      */
      if (m_pin.isValidated())
           m_pin.update(apdubuf, ISO7816.OFFSET_CDATA, (byte) dataLen);
      else 
          ISOException.throwIt(SW_BAD_PIN);
    }
}





