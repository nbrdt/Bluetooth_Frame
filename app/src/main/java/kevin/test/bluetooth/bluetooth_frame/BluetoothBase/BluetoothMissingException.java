package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

/**An exception to be thrown, if there is no Bluetooth available on this phone.
 * @author Kevin Iselborn
 * @version 1.1b
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
