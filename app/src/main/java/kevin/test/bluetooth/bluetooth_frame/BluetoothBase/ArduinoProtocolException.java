package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

/**
 * @author KI
 * @version 1.0
 **/
public class ArduinoProtocolException extends RuntimeException {
    public ArduinoProtocolException(String message) {
        super(message);
    }

    public ArduinoProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
