package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

/**
 * Created by kevin on 20.01.2017.
 */

public class BluetoothUnnoticedException extends RuntimeException {
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public BluetoothUnnoticedException(String message) {
        super(message);
    }
}
