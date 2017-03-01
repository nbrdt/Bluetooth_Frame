package kevin.test.bluetooth.bluetooth_frame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothConnectionStateException;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothDataProvider;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothDataSet;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.NFK_ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.UnrecognizableBluetoothDataException;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramManager;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramSettings;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramViewSettings;

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
    private ActionBar m_actionBar;

    private List<DiagramSettings> m_diagrams;
    private List<BluetoothDataSet> m_bluetoothData = new LinkedList<>();
    private List<BluetoothDataSet> m_dataReceived = new LinkedList<>();
    private ArrayList<Integer> m_temperatureValues = new ArrayList<>();
    private ArrayList<Integer> m_humidityValues = new ArrayList<>();
    private ArrayList<Integer> m_soilValues = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        m_dataProvider = new BluetoothDataProvider(getApplicationContext());
        String addresse = getIntent().getExtras().getString("addresse");

        Toast.makeText(getApplicationContext(), addresse, Toast.LENGTH_SHORT).show();


        if (addresse != null && !addresse.isEmpty()) {
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(Connected.this);
            try {
                client = NFK_ArduinoBluetoothClient.getClient(Integer.parseInt(preference.getString(SettingsActivity.KEY_CONNECTION_RECEIVERATE, "1000")));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Could not resolve receive Rate. You may only enter Numbers in the Settings", Toast.LENGTH_LONG).show();
                try {
                    synchronized (this) {
                        this.wait(2000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                    }
                } catch (InterruptedException e1) {
                    Log.e(LOG_TAG, "Showing error was interrupted", e1);
                }
                finish();
            }
            if (client != null) {
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
                            DiagramViewSettings viewSettings = DiagramViewSettings.getDefaultSettings();
                            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(Connected.this);  // ab hier können die Farb einstellungen eingebaut werden
                            viewSettings.setGraphColor(Integer.parseInt(preference.getString(SettingsActivity.KEY_VIEW_CURSORCOLOR, "-65536")));
                            m_globalSettings = new DiagramSettings(viewSettings, null, null, height, width, Integer.MIN_VALUE, Integer.MAX_VALUE);
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
                    Toolbar usedToolbar = (Toolbar) findViewById(R.id.connected_toolbar);
                    setSupportActionBar(usedToolbar);
                    m_actionBar = getSupportActionBar();

                } catch (BluetoothConnectionStateException e) {
                    Log.e(LOG_TAG, "connection Error", e);
                    Toast.makeText(getApplicationContext(), "could not connect Client", Toast.LENGTH_LONG);
                    try {
                        synchronized (this) {
                            this.wait(2000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                        }
                    } catch (InterruptedException e1) {
                        Log.e(LOG_TAG, "Showing error was interrupted", e1);
                    }
                    finish();
                }
            }

        } else {
            Toast.makeText(getApplicationContext(), "Could not read Target Address", Toast.LENGTH_SHORT).show();
            try {
                synchronized (this) {
                    this.wait(1000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                }
            } catch (InterruptedException e1) {
                Log.e(LOG_TAG, "Showing error was interrupted", e1);
            }
            finish();
        }
    }


    public void changeButtonClicked() {
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

    public void disconnectButtonClicked() {
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            m_bluetoothData = m_dataProvider.readData();
        } catch (UnrecognizableBluetoothDataException e) {
            Log.e(LOG_TAG, "Data could not be read", e);
        }
        m_temperatureValues = new ArrayList<>();
        m_humidityValues = new ArrayList<>();
        m_soilValues = new ArrayList<>();
        for (BluetoothDataSet data :
                m_bluetoothData) {
            m_temperatureValues.add(data.getTemperature().intValue());
            m_humidityValues.add(data.getHumidity().intValue());
            m_soilValues.add(data.getSoilMoisture().intValue());
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actionbar_menu, menu);
        inflater.inflate(R.menu.connected_actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.main_actionbar_menu_item_settings): {
                Intent startSettings = new Intent(this, SettingsActivity.class);
                startActivity(startSettings);
                return true;
            }
            case (R.id.connected_actionbar_menu_item_changeDiagram): {
                this.changeButtonClicked();
                return true;
            }
            case (R.id.connected_actionbar_menu_item_disconnect): {
                this.disconnectButtonClicked();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        m_dataProvider.writeData(m_bluetoothData);
        m_dataReceived.clear(); //just to give some space back to the System in case, the user leaves but reenters, so that the user can have some more space
        m_bluetoothData.clear();

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
        if (client != null) {
            client.destroy();
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public ArrayList<Integer> onRefreshRequest(DiagramSettings fragmentDescription) {
        refreshData();
        LinkedList<BluetoothDataSet> temp = new LinkedList<>();
        Date current = Calendar.getInstance().getTime();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int maxBack = Integer.parseInt(pref.getString(SettingsActivity.KEY_DATA_SHOWVALUES, "10000"));
        for (int i = (m_bluetoothData.size() - 1); (i >= 0); i--) {  //backward for loop...
            BluetoothDataSet data = m_bluetoothData.get(i);
            if ((data.getTimeStamp().getTime()) >= (current.getTime() - maxBack)) {  //he only does something if the current time is in the specified time Period
                temp.addFirst(data);
            } else {
                break;  //just to increase Performance
            }
        }
        prepareLists(temp.size());
        for (BluetoothDataSet data :
                temp) {
            m_temperatureValues.add(data.getTemperature().intValue());
            m_humidityValues.add(data.getHumidity().intValue());
            m_soilValues.add(data.getSoilMoisture().intValue());
        }
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
        List<BluetoothDataSet> received = client.getReceivedData();
        if (received != null) {
            client.clearReceivedData();
            for (BluetoothDataSet data :
                    received) {
                m_bluetoothData.add(data);
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

    private void prepareLists(int size) {
        if (m_temperatureValues != null) {
            m_temperatureValues.clear();
        }
        if (m_humidityValues != null) {
            m_humidityValues.clear();
        }
        if (m_soilValues != null) {
            m_soilValues.clear();
        }
        m_temperatureValues = new ArrayList<>(size);
        m_humidityValues = new ArrayList<>(size);
        m_soilValues = new ArrayList<>(size);
    }
}
