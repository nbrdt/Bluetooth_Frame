package nfk.bluetooth.arduino.wetterverarbeitung;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.ArduinoBluetoothClient;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.BluetoothDataProvider;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.BluetoothDataSet;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.UnrecognizableBluetoothDataException;
import nfk.bluetooth.arduino.wetterverarbeitung.Views.DiagramFragment;

import static nfk.bluetooth.arduino.wetterverarbeitung.Views.DiagramFragment.sectionNumberToPosition;

/**
 * @author kevin
 * @version 0.0 from 09.03.2017 in Bluetooth_Frame
 **/

public class DiagramHandler extends Handler implements DiagramFragment.RefreshListener, ArduinoBluetoothClient.OnReceiveListener {
    private Vector<BluetoothDataSet> m_bluetoothData;
    private LinkedList<Entry> m_temperatureValues;
    private LinkedList<Entry> m_soilValues;
    private LinkedList<Entry> m_rainValues;
    private DiagramActivity.SectionsPagerAdapter m_SectionsPagerAdapter;
    private BluetoothDataProvider m_dataProvider;
    private DiagramCallbacks m_callback;
    private HandlerThread worker; //Reference, so that the Thread isn't destroyed by the Garbage Collection
    private int m_maxBack;
    private int m_viewedPosition;

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
        prepareLists(0);
    }

    @Override
    public void onPreReceive() {

    }

    @Override
    public void onPostReceive() {
        Message msg = this.obtainMessage(MESSAGE_UPDATE_FRAGMENT);
        this.sendMessage(msg);
    }

    @Override
    public void onRefreshRequest(DiagramFragment requester) {  //This method provides the Fragment with Data as needed
        Log.i(LOG_TAG, "Refreshing Diagram");
        m_viewedPosition = m_callback.getCurrentFragmentPosition();
        refreshData();
        processData();
        int sectionNumber = requester.getSectionNumber();
        String title = m_SectionsPagerAdapter.getPageTitle(sectionNumberToPosition(sectionNumber)).toString();
        switch (title) {
            case (DiagramFragment.DIAGRAM_NAME_RAIN): {
                Log.d(LOG_TAG, "Setting Rain Strength values on position: " + m_viewedPosition);
                requester.resetValues(m_rainValues);
                break;
            }
            case (DiagramFragment.DIAGRAM_NAME_SOIL): {
                Log.d(LOG_TAG, "Setting Soil Moisture values on position: " + m_viewedPosition);
                requester.resetValues(m_soilValues);
                break;
            }
            default: {
                Log.d(LOG_TAG, "Setting Temperature values on position: " + m_viewedPosition);
                requester.resetValues(m_temperatureValues);
                break;
            }
        }
    }

    public void notifyOnStart() {
        try {
            m_bluetoothData = (Vector<BluetoothDataSet>) m_dataProvider.readData();
        } catch (UnrecognizableBluetoothDataException e) {
            Log.e(LOG_TAG, "Data could not be read", e);
        }
        prepareLists(m_bluetoothData.size());
    }

    public void notifyOnStop() {
        m_dataProvider.writeData(m_bluetoothData);
        m_bluetoothData.clear();
        clearDataLists();
    }

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

    private void updateFragment() {
        final DiagramFragment fragment = m_SectionsPagerAdapter.getSavedFragmentFromPosition(m_viewedPosition);
        if (fragment != null) {
            fragment.updateDiagram();
        } else {
            Log.w(LOG_TAG, "Could not update Diagram Fragment, because Fragment is not available");
        }
    }

    private void processData() {
        if (m_maxBack > 0) {
            Date current = Calendar.getInstance().getTime();
            LinkedList<BluetoothDataSet> temp = new LinkedList<>();
            for (int i = (m_bluetoothData.size() - 1); (i >= 0); i--) {  //backward for loop...
                BluetoothDataSet data = m_bluetoothData.get(i);
                if ((data.getTimeStamp().getTime()) >= (current.getTime() - m_maxBack)) {  //he only does something if the current time is in the specified time Period
                    temp.addFirst(data);
                } else {
                    break;  //just to increase Performance
                }
            }
            addEntries(temp);
        } else {
            addEntries(m_bluetoothData);
        }
    }

    private void addEntries(@NonNull List<BluetoothDataSet> dataSets) {
        prepareLists(dataSets.size());
        if (!dataSets.isEmpty()) {
            BigDecimal xEntryMin = new BigDecimal(dataSets.get(0).getTimeStamp().getTime()); //gets The Minimum XValue
            BigDecimal xEntryMax = new BigDecimal(dataSets.get(dataSets.size() - 1).getTimeStamp().getTime());  //gets The Maximum XValue
            BigDecimal xEntryRange = xEntryMax.subtract(xEntryMin);  //gets The Range in between which is needed to invert The Axis
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

    private void prepareLists(int size) {
        clearDataLists();
        m_temperatureValues = new LinkedList<>();
        m_soilValues = new LinkedList<>();
        m_rainValues = new LinkedList<>();
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
    }

    public interface DiagramCallbacks {
        public int getCurrentFragmentPosition();

        public List<BluetoothDataSet> getReceivedData();
    }
}