package nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase;

/**
 * @author KI
 * @version 1.0
 */

public class BluetoothInactivityException extends Exception {
    public BluetoothInactivityException () {
        super("Bluetooth is inactive");
    }

}
