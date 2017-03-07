package nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase;

/**An exception to be thrown, if there is no Bluetooth available on this phone.
 * @author KI
 * @version 1.0
 */

public class BluetoothMissingException extends RuntimeException {
    /*
     @param String message
         The kind of Bluetooth Hardware missing
      */
    public BluetoothMissingException (String message) {
        super("Missing:"+ message);
    }
}
