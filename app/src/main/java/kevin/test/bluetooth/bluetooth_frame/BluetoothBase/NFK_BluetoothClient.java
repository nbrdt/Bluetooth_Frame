package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author KI
 * @version 1.1a
 */

public class NFK_BluetoothClient implements BluetoothConstants, BluetoothClient {
    protected List<BluetoothClient.onDisconnectedListener> m_disconnectedListeners;
    protected List<BluetoothClient.onConnectedListener> m_connectedListeners;
    private BluetoothAdapter m_myBluetooth;
    private Set<BluetoothDevice> m_pairedDevices;
    private List<String> m_addressesAndNames;
    private static final String LOG_TAG = "BluetoothClient";
    protected ClientConnectionHandler m_ConnectionHandler;
    private volatile boolean m_succeed;
    private BluetoothListener m_listener;

    protected NFK_BluetoothClient(String charset, BluetoothListener listener) {
        m_myBluetooth = null;
        m_pairedDevices = null;
        m_addressesAndNames = null;
        m_succeed = false;
        m_listener = listener;
        m_ConnectionHandler = new ClientConnectionHandler(m_listener, charset);
        m_connectedListeners = new LinkedList<>();
        m_disconnectedListeners = new LinkedList<>();
    }

    protected NFK_BluetoothClient(String charset) {
        this(charset, null);
        setListener(new ConnectionListener());
    }

    protected NFK_BluetoothClient() {
        this("UTF-8");
    }

    protected void setListener(BluetoothListener listener) {
        if (listener != null) {
            m_listener = listener;
            m_ConnectionHandler.setListener(m_listener);
        } else {
            throw new NullPointerException("tried to set Null Listener");
        }
    }

    protected BluetoothAdapter getBluetoothAdapter() throws BluetoothInactivityException {
        BluetoothAdapter Adapter;
        if (m_myBluetooth != null) {
            return m_myBluetooth;
        } else {
            Adapter = BluetoothAdapter.getDefaultAdapter();
            if (Adapter == null) {
                throw new BluetoothMissingException("Bluetooth Hardware");
            } else if (!Adapter.isEnabled()) {
                throw new BluetoothInactivityException(); //Bluetooth is inactive, UI has to activate it
            }
            m_myBluetooth = Adapter;
        }
        return Adapter;
    }

    /**
     * Returns the AddressesAndNames of all Paired Bluetooth Devices.
     * The List will be refreshed if there aren't any known AddressesAndNames yet (this Method hasn't)
     *
     * @return String Vector containing Name and Address of all paired Bluetooth Devices
     * separated as defined in the Bluetooth Constants interface
     * @throws BluetoothInactivityException in the Event of Bluetooth being unavailable
     * @throws BluetoothMissingException    if there are no available Devices
     **/
    @Override
    public List<String> getAddressesAndNames(boolean forceReset) throws BluetoothInactivityException {
        if (forceReset || (m_addressesAndNames == null) || (m_addressesAndNames.size() <= 0)) {
            if (m_myBluetooth == null) {
                getBluetoothAdapter();  //throws BluetoothInactivityException
            }
            m_pairedDevices = m_myBluetooth.getBondedDevices();
            m_addressesAndNames = new LinkedList<>();
            int size = m_pairedDevices.size();
            if (size > 0) {
                for (BluetoothDevice bt : m_pairedDevices) {
                    m_addressesAndNames.add(bt.getName() + ADDRESS_SEPARATOR + bt.getAddress());
                }
            } else {
                throw new BluetoothMissingException("Paired Devices");
            }
            return m_addressesAndNames;
        }
        Log.w(LOG_TAG, "no Reset done, Data might be outdated and incomplete"); //Warn any programmer of possible unknown Devices
        return m_addressesAndNames;
    }


    /**
     * Will connect to the given Address, which has been retrieved by getAddressesAndNames
     *
     * @throws BluetoothUnnoticedException       if there is no available BluetoothMessageReceiveListener or no available BluetoothListener
     * @throws BluetoothConnectionStateException if the ConnectAttempt failed
     * @throws IllegalArgumentException          if the Input Address doesn't match any paired Bluetooth device
     **/
    @Override
    public void connectBT(String AddressAndName, int tries)
            throws BluetoothConnectionStateException {
        try {
            getAddressesAndNames(false);
        } catch (BluetoothInactivityException e) {
            Log.e(LOG_TAG, "Bluetooth needs to be activated, to establish a Connection", e);
            throw new BluetoothConnectionStateException(getConnectionState(),
                    "Client could not be connected - because of Bluetooth being inactive", e);
        }
        Log.d(LOG_TAG, "starting check for device");
        BluetoothDevice matchingDevice;
        String[] separated = AddressAndName.split(ADDRESS_SEPARATOR);
        Log.i(LOG_TAG, "found:" + separated[0]);
        matchingDevice = m_myBluetooth.getRemoteDevice(separated[1]);
        if (matchingDevice == null) {
            Log.e(LOG_TAG, "could not identify matching Bluetooth device");
            throw new IllegalArgumentException("Input Address doesn't match any paired device");
        }
        Log.d(LOG_TAG, "found matching Bluetooth device with Name: " + matchingDevice.getName());
        Log.v(LOG_TAG, "Starting connection");
        while (tries > 0 && !performConnectAttempt(matchingDevice)) { //throws Bluetooth Unnoticed Exception
            tries--;
        }
        if (m_ConnectionHandler.getConnectionState() != BluetoothConstants.CONNECTION_STATE_CONNECTED) {
            throw new BluetoothConnectionStateException("Client could not be connected");
        }
        dispatchConnectEvents(matchingDevice, m_succeed, false);
    }


    /**
     * Disconnects the Connected device.
     *
     * @throws BluetoothConnectionStateException
     */
    @Override
    public void disconnect() throws BluetoothConnectionStateException {
        m_ConnectionHandler.disconnect();
        dispatchDisconnectEvents(false);
    }

    /**
     * Sends the given String to the Connected BluetoothClient.
     *
     * @param toWrite the String Object to be written out on the Input Stream
     * @throws BluetoothConnectionStateException if it was impossible to read, for example there was no connected BluetoothDevice
     */
    @Override
    public void write(String toWrite) throws BluetoothConnectionStateException {
        m_ConnectionHandler.write(toWrite);
    }

    /**
     * Receives Data from the InputStream and writes it in the given char array
     *
     * @param buffer a Char array in which the Data should be read
     * @return the number of received Chars
     * @throws BluetoothConnectionStateException if it was impossible to read, for example there was no connected BluetoothDevice
     */
    @Override
    public int read(char[] buffer) throws BluetoothConnectionStateException {
        return m_ConnectionHandler.read(buffer);
    }

    /**
     * Destructs the BluetoothClient
     *
     * @return whether or not the BluetoothClient was deconstructed safely
     * @see BluetoothClient
     */
    @Override
    public boolean destroy() {
        Log.i(LOG_TAG, "terminating BluetoothClient");
        if (m_addressesAndNames != null) {
            m_addressesAndNames.clear();
            m_addressesAndNames = null;
        }
        m_pairedDevices = null;
        m_listener = null;
        m_myBluetooth = null;
        boolean unConnected = true;
        if (m_ConnectionHandler.getConnectionState() == CONNECTION_STATE_CONNECTED) {
            try {
                disconnect();
            } catch (BluetoothConnectionStateException e) {
                Log.e(LOG_TAG, "unable to disconnect, destroying reference anyway", e);
                unConnected = false;
            }
        }
        m_ConnectionHandler = null;
        return unConnected;
    }

    @Override
    public void addOnConnectListener(@NonNull onConnectedListener listener) {
        m_connectedListeners.add(listener);
    }

    @Override
    public void addOnDisconnectedListener(@NonNull onDisconnectedListener listener) {
        m_disconnectedListeners.add(listener);
    }

    protected void dispatchConnectEvents(BluetoothDevice btDevice, boolean success, boolean required) {
        if (required) {
            if (m_connectedListeners.size() <= 0) {
                throw new BluetoothUnnoticedException("Required Event isn't noticed");
            }
            for (BluetoothClient.onConnectedListener listener :
                    m_connectedListeners) {
                listener.onConnected(btDevice, success);
            }
        } else {
            for (BluetoothClient.onConnectedListener listener :
                    m_connectedListeners) {
                listener.onConnected(btDevice, success);
            }
        }
    }

    protected void dispatchDisconnectEvents(boolean required) {
        if (required) {
            if (m_connectedListeners.size() <= 0) {
                throw new BluetoothUnnoticedException("Required Event isn't noticed");
            }
            for (BluetoothClient.onDisconnectedListener listener :
                    m_disconnectedListeners) {
                listener.onDisconnected();
            }
        } else {
            for (BluetoothClient.onDisconnectedListener listener :
                    m_disconnectedListeners) {
                listener.onDisconnected();
            }
        }
    }

    /**
     * This Method connects the BluetoothClient to the given Bluetooth whilst pausing the the calling Thread
     *
     * @param toConnect the BluetoothDevice to which the Client should connect
     * @return whether or not the ConnectAttempt succeeded
     * @throws BluetoothUnnoticedException if there is no registered BluetoothListener
     */
    private synchronized boolean performConnectAttempt(BluetoothDevice toConnect) {
        m_succeed = false;
        m_ConnectionHandler.connect(toConnect, m_myBluetooth); //throws BluetoothUnnoticedException
        while (m_ConnectionHandler.getConnectionState() == BluetoothConstants.CONNECTION_STATE_CONNECTING) { //waiting for connection to establish
            ;
        }
        return m_succeed;
    }

    /**
     * Returns the current ConnectionState
     *
     * @return the actual Connection State, as defined in BluetoothConstants
     * @see BluetoothClient
     **/
    @Override
    public int getConnectionState() {
        return m_ConnectionHandler.getConnectionState();
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return super.toString() + "; m_myBluetooth:" + m_myBluetooth.getName();
    }


    protected class ConnectionListener implements BluetoothListener {
        protected ConnectionListener() {

        }

        @Override
        public void onStart() {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void onConnect() {

        }

        @Override
        public void onSend() {

        }

        @Override
        public void onConnectionEstablish(BluetoothDevice connectedDevice) {
            m_succeed = true;
        }

        @Override
        public void onConnectFailure(boolean closed) {
            m_succeed = false;
        }

        @Override
        public void onConnectionLoss() {

        }

        @Override
        public void onConnectionTermination() {

        }
    }
}
