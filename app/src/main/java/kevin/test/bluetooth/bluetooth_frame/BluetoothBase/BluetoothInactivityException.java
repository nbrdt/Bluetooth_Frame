package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

/**
 * @author KI
 * @version 1.0
 */

public class BluetoothInactivityException extends Exception {
    public BluetoothInactivityException () {
        super("Bluetooth is inactive");
    }

}
