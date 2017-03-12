package nfk.bluetooth.arduino.wetterverarbeitung;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.ArduinoBluetoothClient;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.BluetoothDataProvider;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.BluetoothDataSet;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.UnrecognizableBluetoothDataException;
import nfk.bluetooth.arduino.wetterverarbeitung.Views.DiagramFragment;

import static nfk.bluetooth.arduino.wetterverarbeitung.Views.DiagramFragment.sectionNumberToPosition;

/**
 * @author KI
 * @version 1.2
 **/

public class DiagramHandler extends Handler implements DiagramFragment.RefreshListener, ArduinoBluetoothClient.OnReceiveListener {
    private Vector<BluetoothDataSet> m_bluetoothData;  //Vector is Thread safe...
    private LinkedList<Entry> m_temperatureValues;  //uses Linked List's so that adding values is performed relative fast
    private LinkedList<Entry> m_soilValues;
    private LinkedList<Entry> m_rainValues;
    private LinkedList<Entry> m_lightValues;
    private DiagramActivity.SectionsPagerAdapter m_SectionsPagerAdapter;
    private BluetoothDataProvider m_dataProvider;
    private DiagramCallbacks m_callback;
    private HandlerThread worker; //Reference, so that the Thread isn't destroyed by the Garbage Collection
    private int m_maxBack;
    private int m_viewedPosition;
    private volatile boolean logEnabled;

    private static final String LOG_TAG = "Diagram Handler";
    public static final int MESSAGE_UPDATE_FRAGMENT = 1;

    public DiagramHandler(HandlerThread worker, DiagramActivity host, int maxBack) {
        super(worker.getLooper());
        this.m_maxBack = maxBack;
        this.worker = worker;
        m_bluetoothData = new Vector<>();
        m_SectionsPagerAdapter = host.getSectionsPagerAdapter();
        m_dataProvider = new BluetoothDataProvider(host.getApplicationContext());
        m_callback = host;
        setLogEnabled(true);
        prepareLists(0);
    }

    public DiagramHandler(HandlerThread worker, DiagramActivity host) {
        this(worker, host, Integer.parseInt(SettingsActivity.PREF_DEFAULTVALUE_DATA_SHOWVALUES));
    }

    @Override
    public void onPreReceive() {

    }

    @Override
    public void onPostReceive() {
        Message msg = this.obtainMessage(MESSAGE_UPDATE_FRAGMENT);
        this.sendMessage(msg);
    }

    /**
     * Called when an Fragment was notified, that it should be updated and is now prepared for new Data.
     *
     * @param requester The Fragment sending the request.
     */
    @Override
    public void onRefreshRequest(DiagramFragment requester) {  //This method provides the Fragment with Data as needed
        if (isLogEnabled()) Log.i(LOG_TAG, "Refreshing Diagram");
        refreshData();
        processData();
        int sectionNumber = requester.getSectionNumber();
        String title = m_SectionsPagerAdapter.getPageTitle(sectionNumberToPosition(sectionNumber)).toString();
        switch (title) {
            case (DiagramFragment.DIAGRAM_NAME_RAIN): {
                if (isLogEnabled())
                    Log.d(LOG_TAG, "Setting Rain Strength values on position: " + m_viewedPosition);
                requester.resetValues(copyValues(m_rainValues));  //values need to be copied before returning, otherwise the reference from the outside would hamper Thread-Safety
                break;
            }
            case (DiagramFragment.DIAGRAM_NAME_SOIL): {
                if (isLogEnabled())
                    Log.d(LOG_TAG, "Setting Soil Moisture values on position: " + m_viewedPosition);
                requester.resetValues(copyValues(m_soilValues)); //values need to be copied before returning, otherwise the reference from the outside would hamper Thread-Safety
                break;
            }
            case (DiagramFragment.DIAGRAM_NAME_LIGHT): {
                if (isLogEnabled())
                    Log.d(LOG_TAG, "Setting Light values on position: " + m_viewedPosition);
                requester.resetValues(copyValues(m_lightValues)); //values need to be copied before returning, otherwise the reference from the outside would hamper Thread-Safety
                break;
            }
            case (DiagramFragment.DIAGRAM_NAME_TEMP): {
                if (isLogEnabled())
                    Log.d(LOG_TAG, "Setting Temperature values on position: " + m_viewedPosition);
                requester.resetValues(copyValues(m_temperatureValues)); //values need to be copied before returning, otherwise the reference from the outside would hamper Thread-Safety
                break;
            }
            default: {
                Log.w(LOG_TAG, "Could not identify Diagram. Setting Temperature values on position: " + m_viewedPosition);
                requester.resetValues(copyValues(m_temperatureValues));
                break;
            }
        }
    }

    /**
     * Notifies this DiagramHandler of the ActivityLifecycle-Callback onStart.
     * Will load Bluetooth data.
     */
    public void notifyOnStart() {
        try {
            m_bluetoothData = (Vector<BluetoothDataSet>) m_dataProvider.readData();
        } catch (UnrecognizableBluetoothDataException e) {
            Log.e(LOG_TAG, "Data could not be read", e);
        }
        prepareLists(m_bluetoothData.size());
    }

    /**
     * Notifies this DiagramHandler of the ActivityLifecycle-Callback onStop.
     * Will save Bluetooth data, and clear memory of unused Lists.
     */
    public void notifyOnStop() {
        m_dataProvider.writeData(m_bluetoothData);
        m_bluetoothData.clear();
        clearDataLists();
    }

    /**
     * Called, when Settings have changed, which are used by this DiagramHandler.
     * @param maxBack The Maximum Value in ms, of which Diagram values should be drawn.
     */
    public void notifySettingsChanged(int maxBack) {
        this.m_maxBack = maxBack;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_UPDATE_FRAGMENT: {
                updateFragment();
                break;
            }
            default: {
                super.handleMessage(msg);
            }
        }
    }

    public HandlerThread getWorker() {
        return this.worker;
    }

    public synchronized void setLogEnabled(boolean enabled) {
        logEnabled = enabled;
        if (m_dataProvider != null) m_dataProvider.setLogEnabled(enabled);
    }

    public synchronized boolean isLogEnabled() {
        return logEnabled;
    }

    /**
     * Sends the updateDiagram Event to the current Fragment so that it can prepare itself for new Data.
     * This Event is send from the Thread calling this Method, resulting in all Update Operations being made on the calling Thread from this Method.
     */
    private void updateFragment() {
        m_viewedPosition = m_callback.getCurrentFragmentPosition();
        final DiagramFragment fragment = m_SectionsPagerAdapter.getSavedFragmentFromPosition(m_viewedPosition);
        if (fragment != null) {
            fragment.updateDiagram();
        } else {
            Log.w(LOG_TAG, "Could not update Diagram Fragment, because Fragment is not available at Position");
        }
    }

    /**
     * processes BluetoothData, so that only the Data is added, which is within the Boundaries of maxBack.
     */
    private void processData() {
        Date current = Calendar.getInstance().getTime();
        if (m_maxBack > 0) {
            LinkedList<BluetoothDataSet> temp = new LinkedList<>();
            for (int i = (m_bluetoothData.size() - 1); (i >= 0); i--) {  //backward for loop...
                BluetoothDataSet data = m_bluetoothData.get(i);
                if ((data.getTimeStamp().getTime()) >= (current.getTime() - m_maxBack)) {  //he only does something if the current time is in the specified time Period
                    temp.addFirst(data);
                } else {
                    break;  //just to increase Performance
                }
            }
            addEntries(temp, current);
        } else {
            addEntries(m_bluetoothData, current);
        }
    }

    /**
     * Adds the Diagram Entry's to the Lists.
     * Clips the XValues, so that the newest Value is the closest to zero.
     * @param dataSets The List of BluetoothDataSets, from which to evaluate the Entry Lists
     * @param current The Current Time, to be used as the zero xValue.
     */
    private void addEntries(@NonNull List<BluetoothDataSet> dataSets, Date current) {
        prepareLists(dataSets.size());
        if (!dataSets.isEmpty()) {
            BigDecimal xEntryMin = new BigDecimal(dataSets.get(0).getTimeStamp().getTime()); //gets The Minimum XValue
            BigDecimal xEntryMax = new BigDecimal(current.getTime());  //gets The Maximum XValue (which is the current Time, so that the current Time is displayed as the 0 point)
            BigDecimal xEntryRange = xEntryMax.subtract(xEntryMin);  //gets The Range in between which is needed to invert The Axis
            m_viewedPosition = m_callback.getCurrentFragmentPosition();
            DiagramFragment fragment = m_SectionsPagerAdapter.getSavedFragmentFromPosition(m_viewedPosition);
            if (fragment != null) {
                fragment.resetFormat(xEntryRange);  //xEntryRange supplies the MaxValue...
            }
            for (BluetoothDataSet data :  //adding everything to the Lists
                    dataSets) {
                BigDecimal xValue = (new BigDecimal(data.getTimeStamp().getTime())).subtract(xEntryMin);
                xValue = xValue.multiply(new BigDecimal(-1)); //versetzt die Werte unter das minimum
                xValue = xValue.add(xEntryRange);   //versetzt sie wieder um den maximal Abstand nach oben
                m_temperatureValues.addFirst(new Entry(xValue.floatValue(), data.getTemperature().floatValue()));
                m_soilValues.addFirst(new Entry(xValue.floatValue(), data.getRainStrength().floatValue()));
                m_rainValues.addFirst(new Entry(xValue.floatValue(), data.getSoilMoisture().floatValue()));
                m_lightValues.addFirst(new Entry(xValue.floatValue(), data.getBrightness().floatValue()));
            }
        }
    }

    private void refreshData() {
        List<BluetoothDataSet> received = m_callback.getReceivedData();
        if (received != null) {
            for (BluetoothDataSet data :
                    received) {
                m_bluetoothData.add(data);
            }
        }
    }

    private ArrayList<Entry> copyValues(List<Entry> toCopy) {
        ArrayList<Entry> copy = new ArrayList<>(toCopy.size());  //uses Arrays, so that the values can be accessed as fast as possible
        for (Entry e :
                toCopy) {
            copy.add(e);
        }
        return copy;
    }

    private void prepareLists(int size) {
        clearDataLists();
        m_temperatureValues = new LinkedList<>();
        m_soilValues = new LinkedList<>();
        m_rainValues = new LinkedList<>();
        m_lightValues = new LinkedList<>();
    }

    private void clearDataLists() {
        if (m_temperatureValues != null) {
            m_temperatureValues.clear();
        }
        if (m_soilValues != null) {
            m_soilValues.clear();
        }
        if (m_rainValues != null) {
            m_rainValues.clear();
        }
        if (m_lightValues != null) {
            m_lightValues.clear();
        }
    }

    public interface DiagramCallbacks {
        public int getCurrentFragmentPosition();
        public List<BluetoothDataSet> getReceivedData();
    }
}
