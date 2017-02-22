package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

import android.bluetooth.*;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Kevin Iselborn
 * @version 1.0a
 *
 */

public class NFK_BluetoothClient implements BluetoothConstants, BluetoothClient{

    private BluetoothAdapter m_myBluetooth;
    private Set<BluetoothDevice> m_pairedDevices;
    private List<String> m_addressesAndNames;
    private static final String LOG_TAG = "BluetoothClient";
    ClientConnectionHandler m_ConnectionHandler;
    private boolean m_succeed;
    private BluetoothListener m_listener;

    protected  NFK_BluetoothClient (String charset, BluetoothListener listener) {
        m_myBluetooth = null;
        m_pairedDevices = null;
        m_addressesAndNames = null;
        m_succeed = false;
        m_listener = listener;
        m_ConnectionHandler = new ClientConnectionHandler(m_listener,charset);
    }

    protected NFK_BluetoothClient (String charset) {
        this(charset,null);
        setListener(new ConnectionListener());
    }

    protected NFK_BluetoothClient () {
        this("UTF-8");
    }

    protected void setListener(BluetoothListener listener) {
        if (listener != null) {
            m_listener=listener;
            m_ConnectionHandler.setListener(m_listener);
        }
        else {
            throw new NullPointerException("tried to set Null Listener");
        }
    }

    protected BluetoothAdapter getBluetoothAdapter () throws BluetoothMissingException, BluetoothInactivityException {
        BluetoothAdapter Adapter;
        if (m_myBluetooth !=null) {
           return m_myBluetooth;
        }
        else {
            Adapter = BluetoothAdapter.getDefaultAdapter();
            if (Adapter == null) {
                throw new BluetoothMissingException("Bluetooth Hardware");
            } else if (!Adapter.isEnabled()) {
                //Ask to the user turn the bluetooth on
                throw new BluetoothInactivityException();
            }
            m_myBluetooth = Adapter;
        }
        return Adapter;
    }

   /*
    * @return A String Vector containing Name and Address of all paired Bluetooth Devices
    *  separated as defined in the Bluetooth Constants interface
    **/
    @Override
    public List<String> getAddressesAndNames(boolean forceReset) throws BluetoothInactivityException {
        if (forceReset || (m_addressesAndNames==null) ) {
            if (m_myBluetooth==null) {
                getBluetoothAdapter();
            }
            m_pairedDevices = m_myBluetooth.getBondedDevices();
            m_addressesAndNames = new LinkedList<String>();
            int size = m_pairedDevices.size();
            if (size>0) {
                for (BluetoothDevice bt : m_pairedDevices) {
                    m_addressesAndNames.add(bt.getName()+ADDRESS_SEPARATOR+bt.getAddress());
                }
            }
            else {
                throw new BluetoothMissingException("Paired Devices");
            }
            return m_addressesAndNames;
        }
        Log.w("NFK_BluetoothClient", "no Reset done, Data might be outdated and incomplete");
        return m_addressesAndNames;
    }


    /**
     * Will connect to the given Address, which has been retrieved by getAddressesAndNames
     *@throws BluetoothUnnoticedException when
     *  there is no available BluetoothMessageReceiveListener
     *  or no available BluetoothListener
     * @throws BluetoothConnectionStateException when
     *  the ConnctAttemp failed
     * @throws IllegalArgumentException when, the Input Address, doesn't match
     *  any paired Bluetooth device
     **/
    @Override
    public void connectBT (String AddressAndName, int tries)
            throws BluetoothConnectionStateException {
        try {
            getAddressesAndNames(false);
        } catch (BluetoothInactivityException e) {
            Log.e(LOG_TAG, "Bluetooth needs to be activated, to establish a Connection", e);
            throw new BluetoothConnectionStateException(
                    "Client could not be connected - because of Bluetooth being inactive");
        }
        Log.d(LOG_TAG,"starting check for device");
        int size = m_pairedDevices.size();
        BluetoothDevice matchingDevice = null;
        String[] separated = AddressAndName.split(ADDRESS_SEPARATOR);
        Log.i(LOG_TAG,"found:"+separated[0]);
        matchingDevice = m_myBluetooth.getRemoteDevice(separated[1]);
        if (matchingDevice==null) {
            Log.e(LOG_TAG,"could not identify matching Bluetooth device");
            throw new IllegalArgumentException("Input Address doesn't match any paired device");
        }
        Log.d(LOG_TAG,"found matching Bluetooth device with Name: "+matchingDevice.getName());
        Log.v(LOG_TAG,"Starting connection");
        while ( tries>0 && !startConnectAttempt(matchingDevice)) { //throws
            tries--;
        }
        if (m_ConnectionHandler.getConnectionState()!=BluetoothConstants.CONNECTION_STATE_CONNECTED) {
            throw new BluetoothConnectionStateException("Client could not be connected");
        }

    }

    /**
     * Disconnects the Connected device.
     * @throws BluetoothConnectionStateException
     * @throws BluetoothUnnoticedException
     */
    @Override
    public void disconnect() throws BluetoothConnectionStateException {
        m_ConnectionHandler.disconnect();
    }
    @Override
    public void write (String toWrite) throws BluetoothConnectionStateException {
        m_ConnectionHandler.write(toWrite);
    }

    @Override
    public int read(char[] buffer) throws BluetoothConnectionStateException {
        return m_ConnectionHandler.read(buffer);
    }

    private boolean startConnectAttempt(BluetoothDevice toConnect) {
        m_succeed = false;
        m_ConnectionHandler.connect(toConnect,m_myBluetooth);
        while (m_ConnectionHandler.getConnectionState()==BluetoothConstants.CONNECTION_STATE_CONNECTING) { //waiting for connection to establish
            ;
        }
        return m_succeed;
    }

    /*
     *@return returns the int represantation of the actual Connection State, as defined in Bluetooth Constants
    **/
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
        return super.toString()+"; m_myBluetooth:"+ m_myBluetooth.getName();
    }

    protected class ConnectionListener implements BluetoothListener{
        protected ConnectionListener () {
            ;
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
