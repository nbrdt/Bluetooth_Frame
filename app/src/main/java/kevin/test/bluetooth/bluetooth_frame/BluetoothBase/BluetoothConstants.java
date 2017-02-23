package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

/**
 * @author KI
 * @version 1.1
 */

public interface BluetoothConstants {
    public static final String ADDRESS_SEPARATOR =":;";
    public static final String ARDUINO_CHARSET = "US-ASCII";

    public static final int CONNECTION_STATE_CONNECTED = 1;  //connected to some kind of Bluetoothdevice
    public static final int CONNECTION_STATE_CONNECTING = 0;  //attempting to create a connection
    public static final int CONNECTION_STATE_UNCONNECTED = -1;  //not yet connected
    public static final int CONNECTION_STATE_STOPPED = -2;  //actively disconnected or other kind of error

    public static final int MESSAGE_BUFFER_SIZE = 1024;  //This is enough for roughly 300 Messages
}

