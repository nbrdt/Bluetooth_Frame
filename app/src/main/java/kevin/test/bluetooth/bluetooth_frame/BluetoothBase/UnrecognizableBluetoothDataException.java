package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

import java.io.IOException;

/**
 * @author kevin
 * @version 0.0 from 25.02.2017 in Bluetooth_Frame
 **/
public class UnrecognizableBluetoothDataException extends IOException {
    public UnrecognizableBluetoothDataException(String message) {
        super(message);
    }

    public UnrecognizableBluetoothDataException(String message, Throwable cause) {
        super(message, cause);
    }
}