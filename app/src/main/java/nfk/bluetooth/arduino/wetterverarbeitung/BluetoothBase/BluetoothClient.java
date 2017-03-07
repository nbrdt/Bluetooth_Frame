package nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * This interface provides Basic functionality any Bluetooth-Master should provide to Connect to any kind of Client.
 * First you retrieve a List of Addresses and Names and choose the Device to Connect.
 * After you choose the Device to Connect, you can use it's complete Address and Name to Connect to the RemoteDeivce.
 * The Methods Write and Read provide basic functionality for Communication with an RemoteDevice.
 *
 * @author KI
 * @version 1.1
 */
public interface BluetoothClient {
    /**
     * This Method will retrieve the Bluetooth MAC Addresses and Names of all paired devices.
     * The Address will be followed by an AddressSeparator as defined in the BluetoothConstants Interface, and after this the Device Name.
     *
     * @param forceReset whether or not the List is needed to be actualised. If false, the List will still be created if it doesn't exist yet.
     * @return A List of Strings, in which each Element contains the Name Bluetooth and MAC-Address of one paired Device.
     * @throws BluetoothInactivityException If Bluetooth was discovered inactive, so that it was impossible to retrieve the connected devices, and the user is supposed to turn it on.
     */
    public List<String> getAddressesAndNames(boolean forceReset) throws BluetoothInactivityException;

    /**
     * This Method will attempt to connect to the given address and name as retrieved by getAddressesAndNames.
     * The value tries will be ignored if it is smaller than one, but otherwise it should mark how often the BluetoothClient will attempt to connect
     * to the given device before throwing an BluetoothConnectionStateException. This Method pauses the Main Thread whilst running, so that
     * it can either leave the Client in an connected state or return. This may lead to heavy runtime requirements when running with a high amount of
     * tries, so its recommended, to either start this from another Thread or to leave the tries value to one.
     *
     * @param AddressAndName The Bluetooth MAC Address and corresponding name, as retrieved by getAddressesAndNames
     * @param tries          the Number of times to repeat failing Connect attempts until throwing an BluetoothConnectionStateException
     * @throws BluetoothConnectionStateException if there has been any kind of problem Connecting to the given Address
     */
    public void connectBT(String AddressAndName, int tries) throws BluetoothConnectionStateException;


    //public void tryConnectBT (String AddressAndName) throws BluetoothUnnoticedException;

    /**
     * This Method will disconnect the BluetoothClient from the currently connected Bluetooth Device.
     * It runs, like connectBT on the same Thread in which it was called and leads to heavy runtime requirements.
     * It's therefore recommended to call this Method from an Background thread, although you won't be able to notice,
     * whether or not the BluetoothDevice was disconnected without watching the ConnectionState.
     * @throws BluetoothConnectionStateException This Exception is thrown in the case, that the Client could not be connected
     */
    public void disconnect() throws BluetoothConnectionStateException;

    /**
     * Sends the given String to the BluetoothDevice.
     * WARNING: Subclasses might not implement this Method and throw an UnsupportedOperationException
     * @param toWrite The String to be written on the OutputStream
     * @throws BluetoothConnectionStateException If there happened some kind of Error, whilst sending the Message
     * @throws UnsupportedOperationException if the subclass does not implement this Method
     * @see java.io.Writer write (String str)
     */
    public void write(String toWrite) throws BluetoothConnectionStateException;

    /**
     * Calls java.io.Reader's read Method on the Input Stream.
     * WARNING: Subclasses might not implement this Method and throw an UnsupportedOperationException
     * @param buffer a Char array in which the Data should be read
     * @return the number of received bytes
     * @throws BluetoothConnectionStateException if it was impossible to read any Data, for example if there was no connected BluetoothDevice
     * @throws UnsupportedOperationException if the subclass does not implement this Method
     * @see java.io.Reader read(char[] cbuf)
     */
    public int read (char[] buffer) throws BluetoothConnectionStateException;

    /**
     * Returns the current ConnectionState. Used for example to check whether or not a Client is connected.
     *
     * @return the current ConnectionState of this BluetoothClient, as defined in the BluetoothConstants Interface
     */
    public int getConnectionState();

    /**
     * Destructs the BluetoothClient. Clears for example any running connection.
     *
     * @return whether or not the Client was destructed safely
     */
    public boolean destroy();

    /**
     * Adds an onConnectedListener to an internal List, to be called in the Event of an Connect Event
     *
     * @param listener The listener to be added
     */
    public void addOnConnectListener(onConnectedListener listener);

    public interface onConnectedListener {
        public void onConnected(BluetoothDevice btDevice, boolean success);
    }

    /**
     * Adds an addOnDisconnectedListener to an internal List, to be called in the Event of an Connect Event
     *
     * @param listener The listener to be added
     */
    public void addOnDisconnectedListener(onDisconnectedListener listener);

    public interface onDisconnectedListener {
        public void onDisconnected();
    }
}
