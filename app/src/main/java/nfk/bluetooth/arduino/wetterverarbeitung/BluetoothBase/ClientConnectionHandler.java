package nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * @author KI
 * @version 1.1b
 */

final class ClientConnectionHandler implements BluetoothConstants{
    private BluetoothListener m_Listener;
    private BluetoothDevice m_knownDevice;
    private ConnectedThread m_btConnectedThread;
    private ConnectThread m_btConnectThread;
    private volatile int m_connectionState;
    private volatile boolean logEnabled;
    private UUID m_AppUUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //somehow this is the only working UUID
    private final static String LOG_TAG = "ClientConnectionHandler";
    private String m_charset;

    /**
     * This Constructor initialises a new unconnected Client Connection Handler Object with a pre-defined Bluetooth Listener.
     * The Bluetooth Listener will be treated as if invoking the setListener Method after Object Creationn.
     * @param listener The BluetoothListener Object to be invoked on Bluetooth Events
     * @param charset The Charset to be used, one case of charset being unknown to JVM, UTF-8 will be used
     */
    public ClientConnectionHandler(BluetoothListener listener, String charset) {
        m_Listener = listener;
        m_btConnectedThread = null;
        m_btConnectThread = null;
        m_connectionState = CONNECTION_STATE_UNCONNECTED;
        m_charset = charset;
        logEnabled = true;
    }

    /**
     * This Constructor initialises a new unconnected Client Connection Handler Object with a pre-defined Bluetooth Listener.
     * The Bluetooth Listener will be treated as if invoking the setListener Method after Object Creation.
     * Will use UTF-8 as default.
     * @param listener The BluetoothListener Object to be invoked on Bluetooth Events
     */
    ClientConnectionHandler(BluetoothListener listener) {
        this(listener,"UTF-8");
    }

    /**
     *
     * This Constructor initialises a new Unconnected Connection Handler, with the given charset and without an predefined listener
     */
    ClientConnectionHandler(String charset) {
        this(null,charset);
    }

    /**
     *
     * This Constructor initialises a new Unconnected Connection Handler.
     * Will use UTF-8 as default.
     */
    ClientConnectionHandler() {
        this("UTF-8");
    }

    /**
     * Prepares this ClientConnectionHandler for any connection.
     * Any running Connection will be cancelled and the ConnectionHandler will prepared for the next connection.
     * @throws BluetoothConnectionStateException if he had to disconnect and this failed
     * @throws BluetoothUnnoticedException if there is no registerd BluetoothListener
     */
    private void prepare() throws BluetoothConnectionStateException {
        this.checkListener();
        if (getConnectionState() == CONNECTION_STATE_CONNECTED) {
            this.disconnect();
        }
        this.start(); //restarts properly
    }

    void destroy() throws BluetoothConnectionStateException {
        prepare(); // if he wants to destroy himself, he first needs to reset him so far, that he could enter a new Connection
        m_Listener = null;
        m_charset = null;
        m_knownDevice = null;
    }

    /**
     * Returns the current ConnectionState of this ClientConnectionHandler
     * @return the current ConnectionState as an int
     */
    synchronized int getConnectionState() {
        return this.m_connectionState;
    }

    synchronized void setLogEnabled(boolean enabled) {
        logEnabled = enabled;
    }

    boolean isLogEnabled() {
        return logEnabled;
    }


    /**
     * Will set the BluetoothListenerCallback to the given Listener
     * Bluetooth Listener will be called on any defined Events.
     * Note: there has to be at least one kind o Bluetooth Listener on Connect, otherwise an BluetoothUnnoticedException will be thrown.
     * @param Listener BluetoothListener to be registered
     * @throws NullPointerException in the Event of the Listener being an null Object reference
     */
    void setListener(BluetoothListener Listener) {
        if (Listener !=null) {
            m_Listener = Listener;
        }
        else {
            throw new NullPointerException("Tried to set Listener to  a null Object Reference");
        }
    }

    /**
     * Will connect to the given BluetoothDevice and send an onConnect Event.
     * The ConnectionState will be set to Connecting, while the Bluetooth Socket attempts to connect.
     * As soon as the connection failed or was established, it will call the appropriate method
     * @param btDevice the BluetoothDevice to be connected to this ConnectionHandler
     * @param btUsedAdapter the BluetoothAdapter which has been used to get the BluetoothDevice
     * @throws BluetoothUnnoticedException if there is no registered message receive Event listener
     */
    synchronized void connect(BluetoothDevice btDevice, BluetoothAdapter btUsedAdapter) {
        m_knownDevice = btDevice;
        this.start();
        if (isLogEnabled()) Log.v(LOG_TAG, "sending connect Event");
        checkListener();
        m_Listener.onConnect();
        if (isLogEnabled()) Log.v(LOG_TAG, "starting Connect Thread");
        m_btConnectThread = new ConnectThread(btDevice,btUsedAdapter);
        if (isLogEnabled()) Log.v(LOG_TAG, "successfully initialised Connect Thread, starting");
        if (isLogEnabled()) Log.v(LOG_TAG, "changing connection state to Connecting");
        m_connectionState = CONNECTION_STATE_CONNECTING;
        m_btConnectThread.start();
        if (isLogEnabled()) Log.v(LOG_TAG, "successfully started Connect Thread");
    }

    /**
     * Will disconnect the ClientConnectionHandler from the Connected device by stopping it.
     * Will send an onConnectionTermination Event.
     * @throws BluetoothConnectionStateException in the Case of the Connection State being unconnected
     */
    void disconnect() throws BluetoothConnectionStateException {
        if (m_connectionState == CONNECTION_STATE_CONNECTED) {
            m_connectionState = CONNECTION_STATE_UNCONNECTED;
            if (isLogEnabled()) Log.d(LOG_TAG, "Sending Event");
            checkListener();
            m_Listener.onConnectionTermination();
            if (isLogEnabled()) Log.i(LOG_TAG, "finished sending Event");
            if (isLogEnabled()) Log.v(LOG_TAG, "Stopping all threads");
            this.stop();
        } else {
            throw new BluetoothConnectionStateException("tried to disconnect an unconnected Client");
        }
    }

    void write(String toSend) throws BluetoothConnectionStateException {
        if (m_connectionState != CONNECTION_STATE_CONNECTED
                || m_btConnectedThread == null) {
            throw new BluetoothConnectionStateException(
                    "there seems to be no connected Bluetoothdevice");
        } else {
            if (isLogEnabled()) Log.v(LOG_TAG, "requested to Send:" + toSend);
            if (isLogEnabled()) Log.v(LOG_TAG, "sending on Send Event");
            checkListener();
            m_Listener.onSend();
            if (isLogEnabled()) Log.v(LOG_TAG, "finished sending Events, sending Message");
            m_btConnectedThread.write(toSend, false);
        }
    }

    BufferedReader getInputStream() throws BluetoothConnectionStateException {
        if (getConnectionState() == CONNECTION_STATE_CONNECTED) {
            return m_btConnectedThread.getInputStream();
        } else {
            throw new BluetoothConnectionStateException(
                    "no InputStream available, because there is no connected Device available");
        }
    }

    BufferedWriter getOutputStream() throws BluetoothConnectionStateException {
        if (getConnectionState() == CONNECTION_STATE_CONNECTED) {
            return m_btConnectedThread.getOutputStream();
        } else {
            throw new BluetoothConnectionStateException(
                    "no OutputStream available, because there is no connected Device available");
        }
    }

    int read(char[] buffer) throws BluetoothConnectionStateException {
        if (m_btConnectThread != null && getConnectionState() == CONNECTION_STATE_CONNECTED) {
            return m_btConnectedThread.read(buffer);
        } else {
            throw new BluetoothConnectionStateException("could not read from an unconnected client");
        }
    }

    /**
     * This Method is called in the case that it has been possible to establish an connection.
     * It will create a new Bluetooth Connected Thread from the given Socket, which is able listen
     * for any incoming Bluetooth Messages from the Connected Device and send Bluetooth Messages to
     * the Connected Device.
     * Will send a onConnectionEstablish Event with the Connected Bluetooth Device.
     * @param btSocket The connected Socket, which shall be supervised
     * @param btDevice The Connected Bluetooth Device which will be given to any connection Established Listener
     */
    private void connectionEstablished (BluetoothSocket btSocket, BluetoothDevice btDevice) {
        if (isLogEnabled()) Log.d(LOG_TAG, "connection has been established");
        if (isLogEnabled()) Log.v(LOG_TAG, "sending connection established Event");
        checkListener();
        m_Listener.onConnectionEstablish(btDevice);
        this.start();
        if (isLogEnabled()) Log.v(LOG_TAG, "Starting new connected Thread");
        m_btConnectedThread = new ConnectedThread(btSocket);
        m_btConnectedThread.start();
        if (isLogEnabled()) Log.v(LOG_TAG, "changing connection state");
        m_connectionState = CONNECTION_STATE_CONNECTED;
        if (isLogEnabled()) Log.d(LOG_TAG, "successfully started Message receive Listening");
    }

    /**
     * This Method is called in the case that it has been impossible to establish an Connection.
     * It will stop the Running ConnectionHandler.
     * Will send a onConnectionFailure Event.
     * @param closed whether or not the BluetoothSocket could be closed or not
     */
    private void connectionFailed (boolean closed) { //TODO react to unclosed Sockets
        Log.e(LOG_TAG,"Some unknown Error has occurred, trying to establish connection to given device failed");
        if (isLogEnabled()) Log.v(LOG_TAG, "setting State to unconnected");
        m_connectionState = CONNECTION_STATE_UNCONNECTED;
        if (isLogEnabled()) Log.v(LOG_TAG, "sending connection Failure Event");
        checkListener();
        m_Listener.onConnectFailure(closed);
        if (isLogEnabled()) Log.d(LOG_TAG, "stopping Connection Handler");
        this.stop();
    }

    /**
     * This Method is called in the Case, that the Connected Thread notices, that the Connection has been Lost.
     * Will send a onConnectionLoss Event and will stop the ConnectionHandler
     */
    private void connectionLost() {
        Log.e(LOG_TAG,"Some unknown Error has occurred, Connection has been Lost");
        m_connectionState = CONNECTION_STATE_UNCONNECTED;
        if (isLogEnabled()) Log.v(LOG_TAG, "sending Connection Lost Event");
        checkListener();
        m_Listener.onConnectionLoss();
        this.cancelAll();
        if (isLogEnabled()) Log.d(LOG_TAG, "stopping Connection Handler");
        this.stop();
    }

    /**
     * Prepares ConnectionHandler for the next Connection.
     * All Threads and Connections will be cancelled.
     * Reacts as if calling stop with the single difference, that the Connection State won't be set to stopped, but will remain at it's current state.
     * Will send a onStart Event.
     */
    private synchronized void start() {
        if (isLogEnabled()) Log.v(LOG_TAG, "sending connectionHandler start Event");
        checkListener();
        m_Listener.onStart();
        this.cancelAll();
        if (isLogEnabled()) Log.v(LOG_TAG, "everything has been refreshed, ready to use");
    }

    /**
     * Prepares ConnectionHandler for the next Connection.
     * All Threads and Connections will be cancelled.
     * Reacts as if calling start with the single difference, that the Connection State will be set to stopped, it therefore sifnals
     * that some kind of Action has to be taken, until the client may be reconnected.
     * Will send a onStop Event.
     */
    private synchronized void stop() {
        if (isLogEnabled()) Log.v(LOG_TAG, "sending connectionHandler stop Event");
        checkListener();
        m_Listener.onStop();
        cancelAll();
        if (isLogEnabled())
            Log.v(LOG_TAG, "all Threads have been cancelled, resetting connection State");
        m_connectionState = CONNECTION_STATE_STOPPED;
        if (isLogEnabled()) Log.d(LOG_TAG, "Connection Handler has been actively stopped");
    }

    /**
     * Checks, whether there is a registered BluetoothListener, or not.
     *
     * @throws BluetoothUnnoticedException if there is no registered BluetoothListener
     */
    private void checkListener() {
        if (m_Listener == null)
            throw new BluetoothUnnoticedException("Can't send Events to an unexisting Listener");
    }

    private void cancelAll() {
        if (isLogEnabled()) Log.d(LOG_TAG, "attempting to cancel all Threads");
        if (this.m_btConnectedThread != null) { // checks, whether there is an active Connection Holding thread or not
            if (isLogEnabled()) Log.v(LOG_TAG, "attempting to cancel connection Holding Thread");
            this.m_btConnectedThread.cancel();
            this.m_btConnectedThread.terminate();
            this.m_btConnectedThread = null;
            if (isLogEnabled()) Log.i(LOG_TAG, "cancelled connection Holding Thread");
        }
        if (this.m_btConnectThread!=null) {  // checks, whether there is an active Connecting thread or not
            if (isLogEnabled()) Log.v(LOG_TAG, "attempting to cancel connect Thread");
            this.m_btConnectThread.cancel();
            this.m_btConnectThread.terminate();
            this.m_btConnectThread = null;
            if (isLogEnabled()) Log.i(LOG_TAG, "cancelled connect Thread");
        }
        if (isLogEnabled())
            Log.d(LOG_TAG, "all Threads held by this ConnectionHandler are now inactive");
    }

    /**This Thread will attempt to create a Connected BluetoothSocket to the Device,
     * which is given to the Constructor
     * @version 1.1b
     * @author Kevin Iselborn
     */
    private class ConnectThread extends Thread {
        private BluetoothDevice pm_btDevice;
        private BluetoothSocket pm_btSocket;
        private BluetoothAdapter pm_btAdapter;
        private static final String LOG_TAG = "Connect Thread";
        /**
         *
         */
        ConnectThread(BluetoothDevice toConnect, BluetoothAdapter usedAdapter, String name) {
            super(name);
            pm_btDevice = toConnect;
            pm_btAdapter = usedAdapter;
            attempSocketCreation(false);
        }

        ConnectThread(BluetoothDevice toConnect, BluetoothAdapter usedAdapter) {
            this(toConnect, usedAdapter, "Bluetooth " + LOG_TAG);
        }

        private void attempSocketCreation(boolean tried)  {
            if (pm_btDevice==null) {
                Log.w(LOG_TAG,"had to use known Bluetooth Device. Might not correspond with Device to Connect");
                pm_btDevice = m_knownDevice;
            }
            BluetoothSocket attemptingSocket = null;
            if (isLogEnabled())
                Log.i(LOG_TAG, "attempting Bluetooth Client Socket creating to Device: " + pm_btDevice.getName());
            try {
            attemptingSocket = pm_btDevice.createInsecureRfcommSocketToServiceRecord(m_AppUUID);
            }
            catch (IOException io) {
                if (!tried) {
                    if (isLogEnabled()) Log.i(LOG_TAG, "creation Failed once, retrying");
                    attempSocketCreation(true);  // about 1 of 10 times Socket creation just fails without reason -> retrying
                }
                else {
                    Log.e(LOG_TAG,"creation Failed twice, retry would be pointless -"+" you should rather try establishing a possible connection",io);
                    connectionFailed(cancel());
                }
            }
            pm_btSocket = attemptingSocket;
            if (isLogEnabled())
                Log.d(LOG_TAG, pm_btSocket != null ? "successfully created Bluetooth Socket" : "failed to create Bluetooth Socket");
        }

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see #start()
         * @see #stop()
         */
        @Override
        public void run() {
            if (isLogEnabled()) Log.i(LOG_TAG, "Beginning Connection");
            setName("Client Connect Thread");
            //the following checks are just checks, wheter initilisation is complete or not
            if (pm_btAdapter==null) {
                Log.w(LOG_TAG,"using default Adapter");
                pm_btAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            if (pm_btSocket == null) {
                Log.w(LOG_TAG,"Had to use Default Bluetooth Socket");
                attempSocketCreation(false);
            }
            if (pm_btAdapter.isDiscovering()){
                pm_btAdapter.cancelDiscovery();
                if (isLogEnabled())
                    Log.i(LOG_TAG, "Successfully cancelled Bluetooth Device Discovery");
            }
            try {  //starting Connect try
                if (isLogEnabled()) Log.i(LOG_TAG, "attempting to connect");
                pm_btSocket.connect();
            } catch (IOException connectExcp) {
                Log.e(LOG_TAG,"Connection establishing failed", connectExcp);
                boolean closable = cancel();
                connectionFailed(closable);
                return;
            }
            synchronized (ClientConnectionHandler.this) {
                m_btConnectThread = null;
            }
            connectionEstablished(pm_btSocket, pm_btDevice);
        }

        /**
         * Will close and shut down the BluetoothSocket, but will not terminate references
         * @return whether it cancelled successfully, or not
         */
        boolean cancel() {
            Log.d(LOG_TAG,"closing Socket");
            try {
                pm_btSocket.close();
            } catch (IOException closeExcp) {
                Log.e(LOG_TAG,"Unable to close Bluetooth Socket",closeExcp);
                return false;
            }
            return true;
        }

        /**
         * destructs the Object
         */
        void terminate() {
            pm_btDevice = null;
            pm_btSocket = null;
            pm_btAdapter = null;
        }
    }

    /**This Thread will use a given Connected Socket to listen for incoming Messages and will send given bytestreams to the Connected BluetoothDeivce
     * @author Kevin Iselborn
     * @version 1.0b
     */
    private class ConnectedThread extends Thread {
        private BluetoothSocket pm_btSocket;
        private BufferedReader pm_InputSt;
        private BufferedWriter pm_OutputSt;
        private boolean created;
        private static final String LOG_TAG = "Connection Handler";


        private ConnectedThread(BluetoothSocket pm_btSocket, String name) {
            super(name);
            this.pm_btSocket = pm_btSocket;
            created = true;
            if (isLogEnabled()) Log.v(LOG_TAG, "attempting I/O creation");
            try {
                this.pm_InputSt = new BufferedReader(new InputStreamReader( pm_btSocket.getInputStream(),m_charset),MESSAGE_BUFFER_SIZE);
                this.pm_OutputSt = new BufferedWriter(new OutputStreamWriter(pm_btSocket.getOutputStream(),m_charset),10);
            } catch (UnsupportedEncodingException encodeExcp) {
                Log.e(LOG_TAG,"Tried to assign unsupported charset, using default: UTF-8");
                try {
                    this.pm_InputSt = new BufferedReader(new InputStreamReader( pm_btSocket.getInputStream(),m_charset),MESSAGE_BUFFER_SIZE);
                    this.pm_OutputSt = new BufferedWriter(new OutputStreamWriter(pm_btSocket.getOutputStream(),m_charset),10);
                } catch (IOException e) {
                    Log.e(LOG_TAG,"Unable to create I/O - Streams. Socket is probably not created",e);
                    created = false;
                }
            } catch (IOException e) {
                Log.e(LOG_TAG,"Unable to create I/O - Streams. Socket is probably not created",e);
                created = false;
            }
            if (isLogEnabled())
                Log.d(LOG_TAG, created ? "Successfully created I/O - Streams" : "I/O Stream creation failed");
        }

        ConnectedThread(BluetoothSocket pm_btSocket) {
            this(pm_btSocket, "Bluetooth " + LOG_TAG);
        }

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see #start()
         * @see #stop()
         */
        @Override
        public void run() {
            if (created) {
                if (isLogEnabled()) Log.i(LOG_TAG, "Beginning Connection Listening");
                while (m_connectionState == CONNECTION_STATE_CONNECTED) { //
                    ;
                }
                if (isLogEnabled())
                    Log.i(LOG_TAG, "noticed Connection loss, alerting StreamListeners and stopping");  //TODO alerting StreamListener
            }
        }

        public void write(String toWrite, boolean force) throws BluetoothConnectionStateException {
            try {
                pm_OutputSt.write(toWrite);
                if (force) {
                    pm_OutputSt.flush();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Exception while trying to send message, ignoring message:" + toWrite, e);
                throw new BluetoothConnectionStateException(getConnectionState(), "could not send Message: " + toWrite, e);
            }
        }

        int read (char[] buffer) throws BluetoothConnectionStateException {
            try {
                return pm_InputSt.read(buffer);
            } catch (IOException e) {
                throw new BluetoothConnectionStateException(getConnectionState(), "could not read Stream. Socket might be closed or read might be disabled.", e);
            }
        }

        public BufferedReader getInputStream () {
            return pm_InputSt;
        }

        public BufferedWriter getOutputStream () {
            return pm_OutputSt;
        }

        public boolean cancel () {
            if (isLogEnabled()) Log.d(LOG_TAG, "closing Socket and I/O Streams");
            try {
                pm_btSocket.close();
                pm_btSocket = null;
                if (pm_InputSt != null) {
                    pm_InputSt.close();
                    pm_InputSt = null;
                }
                if (pm_OutputSt != null) {
                    pm_OutputSt.close();
                    pm_OutputSt = null;
                }
            } catch (IOException closeExcp) {
                Log.e(LOG_TAG,"Unable to close I/O-Streams or Bluetooth Socket",closeExcp);
                return false;
            }
            return true;
        }
        
        public void terminate () {
            pm_OutputSt = null;
            pm_InputSt  = null;
            pm_btSocket = null;
        }
    }
}



