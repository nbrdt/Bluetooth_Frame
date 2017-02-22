package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

/**
 * Created by kevin on 14.01.2017.
 */

public class BluetoothInactivityException extends Exception {
    public BluetoothInactivityException () {
        super("Bluetooth is inactive");
    }

}
