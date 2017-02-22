package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

/**
 * Created by kevin on 19.01.2017.
 */

public interface BluetoothConstants {
    public static final String ADDRESS_SEPARATOR =":;";
    public static final String ARDUINO_CHARSET = "US-ASCII";

    public static final int CONNECTION_STATE_CONNECTED = 1;
    public static final int CONNECTION_STATE_CONNECTING = 0;
    public static final int CONNECTION_STATE_UNCONNECTED = -1;
    public static final int CONNECTION_STATE_STOPPED = -2;

    public static final int MESSAGE_BUFFER_SIZE = 1024;
}

