package kevin.test.bluetooth.bluetooth_frame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothConnectionStateException;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothDataProvider;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.DataSet;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.NFK_ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.UnrecognizableBluetoothDataException;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramManager;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramSettings;

public class Connected extends AppCompatActivity implements DiagramManager.DataProvider {
    private static final String LOG_TAG = "Connected Activity";
    private static final String FRAGMENT_TAG_TEMPERATURE = "Temperature";
    private static final String FRAGMENT_TAG_HUMIDITY = "Humidity";
    private static final String FRAGMENT_TAG_SOILMOISTURE = "Soil Moisture";

    private ArduinoBluetoothClient client;
    private Button refreshButton;
    private DiagramSettings m_globalSettings;
    private DiagramManager m_diagramManager;
    private BluetoothDataProvider m_dataProvider;

    private List<DiagramSettings> m_diagrams;
    private List<DataSet> m_dataFromFile = new LinkedList<>();
    private List<DataSet> m_dataReceived = new LinkedList<>();
    private ArrayList<Integer> m_temperatureValues = new ArrayList<>();
    private ArrayList<Integer> m_humidityValues = new ArrayList<>();
    private ArrayList<Integer> m_soilValues = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        m_dataProvider = new BluetoothDataProvider(getApplicationContext());
        String addresse = getIntent().getExtras().getString("addresse");

        Toast.makeText(getApplicationContext(), addresse, Toast.LENGTH_SHORT).show();


        if (addresse != null && !addresse.isEmpty()) {

             client = NFK_ArduinoBluetoothClient.getClient();

            try {
                client.connectBT(addresse, 1);

                final LinearLayout l = (LinearLayout) findViewById(R.id.activity_connected);
                l.setBackgroundColor(Color.LTGRAY);
                final Connected host = this;
                l.post(new Runnable() {  //the Fragments can only be created, after Layout height can be measured
                    @Override
                    public void run() {
                        int width = l.getWidth();
                        int height = (int) Math.round(width * 0.6);
                        Log.i(LOG_TAG, "using following Fragment hosting Properties:" +
                                " width:" + width +
                                " height: " + height);
                        m_globalSettings = new DiagramSettings(null, null, height, width, Integer.MIN_VALUE, Integer.MAX_VALUE);
                        m_diagrams = new ArrayList<>(3);
                        m_diagrams.add(new DiagramSettings(
                                m_globalSettings.getViewSettings(),
                                FRAGMENT_TAG_TEMPERATURE,
                                "°C",
                                m_globalSettings.getHeight(),
                                m_globalSettings.getWidth(),
                                -25,
                                100));
                        m_diagrams.add(new DiagramSettings(
                                m_globalSettings.getViewSettings(),
                                FRAGMENT_TAG_HUMIDITY,
                                "%",
                                m_globalSettings.getHeight(),
                                m_globalSettings.getWidth(),
                                0,
                                100));
                        m_diagrams.add(new DiagramSettings(
                                m_globalSettings.getViewSettings(),
                                FRAGMENT_TAG_SOILMOISTURE,
                                "%",
                                m_globalSettings.getHeight(),
                                m_globalSettings.getWidth(),
                                0,
                                100));
                        m_diagramManager = new DiagramManager(host, l, m_diagrams, host);
                        m_diagramManager.showDiagram(FRAGMENT_TAG_TEMPERATURE);
                        Log.i(LOG_TAG, "Diagram Manager has been created");
                    }
                });


            } catch (BluetoothConnectionStateException e) {
                Log.e(LOG_TAG, "connection Error", e);
                Toast.makeText(getApplicationContext(), "could not connect Client", Toast.LENGTH_LONG);
                finish();
            }


        } else {
            Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }


    public void changeButtonClicked(View v) {
        if (m_diagramManager.getShown() != null) {
            if (m_diagramManager.getShown().getName().equalsIgnoreCase(FRAGMENT_TAG_TEMPERATURE)) {
                m_diagramManager.showDiagram(FRAGMENT_TAG_HUMIDITY);
            } else if (m_diagramManager.getShown().getName().equalsIgnoreCase(FRAGMENT_TAG_HUMIDITY)) {
                m_diagramManager.showDiagram(FRAGMENT_TAG_SOILMOISTURE);
            } else {
                m_diagramManager.showDiagram(FRAGMENT_TAG_TEMPERATURE);
            }
        } else {
            m_diagramManager.showDiagram(FRAGMENT_TAG_TEMPERATURE);
        }
    }

    public void disconnectButtonClicked(View v) {
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            m_dataFromFile = m_dataProvider.readData();
        } catch (UnrecognizableBluetoothDataException e) {
            Log.e(LOG_TAG, "Data could not be read", e);
        }
        m_dataReceived = new LinkedList<>();  //there cannot be any received Data...
        m_temperatureValues = new ArrayList<>(m_dataFromFile.size());
        m_humidityValues = new ArrayList<>(m_dataFromFile.size());
        m_soilValues = new ArrayList<>(m_dataFromFile.size());
        for (DataSet data :
                m_dataFromFile) {
            m_temperatureValues.add(data.getTemperature().intValue());
            m_humidityValues.add(data.getHumidity().intValue());
            m_soilValues.add(data.getSoilMoisture().intValue());
        }
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     * <p>
     * <p>Keep in mind that onResume is not the best indicator that your activity
     * is visible to the user; a system window such as the keyguard may be in
     * front.  Use {@link #onWindowFocusChanged} to know for certain that your
     * activity is visible to the user (for example, to resume a game).
     * <p>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onRestoreInstanceState
     * @see #onRestart
     * @see #onPostResume
     * @see #onPause
     */
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
        for (DataSet data :
                m_dataReceived) {
            m_dataFromFile.add(data);
        }
        m_dataProvider.writeData(m_dataFromFile, false);
        m_dataReceived.clear(); //just to give some space back to the System in case, the user leaves but reenters, so that the user can have some more space
        m_dataFromFile.clear();
    }

    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the {@link #isFinishing} method.
     * <p>
     * <p><em>Note: do not count on this method being called as a place for
     * saving data! For example, if an activity is editing data in a content
     * provider, those edits should be committed in either {@link #onPause} or
     * {@link #onSaveInstanceState}, not here.</em> This method is usually implemented to
     * free resources like threads that are associated with an activity, so
     * that a destroyed activity does not leave such things around while the
     * rest of its application is still running.  There are situations where
     * the system will simply kill the activity's hosting process without
     * calling this method (or any others) in it, so it should not be used to
     * do things that are intended to remain around after the process goes
     * away.
     * <p>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onPause
     * @see #onStop
     * @see #finish
     * @see #isFinishing
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.destroy();
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        if (!m_diagramManager.remove()) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public ArrayList<Integer> onRefreshRequest(DiagramSettings fragmentDescription) {
        refreshData();
        switch (fragmentDescription.getName()) {
            case (FRAGMENT_TAG_TEMPERATURE): {
                return copyValues(m_temperatureValues);
            }
            case (FRAGMENT_TAG_HUMIDITY): {
                return copyValues(m_humidityValues);
            }
            case (FRAGMENT_TAG_SOILMOISTURE): {
                return copyValues(m_soilValues);
            }
        }
        return null;
    }


    private void refreshData() {
        List<DataSet> received = client.getReceivedData();
        if (received != null) {
            client.clearReceivedData();
            for (DataSet data :
                    received) {
                m_dataReceived.add(data);
                m_temperatureValues.add(data.getTemperature().intValue());
                m_humidityValues.add(data.getHumidity().intValue());
                m_soilValues.add(data.getSoilMoisture().intValue());
            }
        }
    }

    private ArrayList<Integer> copyValues(ArrayList<Integer> toCopyFrom) {
        ArrayList<Integer> copy = new ArrayList<>(toCopyFrom.size());
        for (Integer i :
                toCopyFrom) {
            copy.add(i);
        }
        return copy;
    }
}
