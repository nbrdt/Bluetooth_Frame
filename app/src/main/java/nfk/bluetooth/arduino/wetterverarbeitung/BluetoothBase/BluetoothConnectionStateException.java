package nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase;

/**
 * @author KI
 * @version 1.1 from 21.02.2017
 **/

public class BluetoothConnectionStateException extends Exception {
    private final Integer connectionState;

    public BluetoothConnectionStateException(Integer connectionState, String message, Throwable cause) {
        super(message, cause);
        this.connectionState = connectionState;
    }

    public BluetoothConnectionStateException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public BluetoothConnectionStateException(Integer connectionState, String message) {
        super(message);
        this.connectionState = connectionState;
    }

    public BluetoothConnectionStateException(String message) {
        this(null, message);
    }

    public Integer getConnectionState() {
        return connectionState;
    }
}